package kr.imhyuk.k8sdashboard.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1Pod;
import kr.imhyuk.k8sdashboard.service.K8sService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class K8sWebSocketHandler extends TextWebSocketHandler {

    private final K8sService k8sService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 연결된 세션 관리
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService scheduler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket 연결됨 : {}", session.getId());

        // 연결되면 즉시 한 번 전송
        sendClusterStatus(session);

        // 스케줄러가 없으면 시작
        if (scheduler == null || scheduler.isShutdown()) {
            startScheduler();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket 연결 종료 : {}", session.getId());

        // 세션 없으면 스케줄러 중지
        if (sessions.isEmpty() && scheduler != null) {
            scheduler.shutdown();
        }
    }

    // 5초마다 클러스터 상태 전송
    private void startScheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            sessions.forEach(this::sendClusterStatus);
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void sendClusterStatus(WebSocketSession session) {
        try {
            Map<String, Object> status = new HashMap<>();

            // 노드 정보
            List<Map<String, String>> nodes = new ArrayList<>();
            for (V1Node node : k8sService.getNodes()) {
                Map<String, String> nodeInfo = new HashMap<>();
                nodeInfo.put("name", node.getMetadata().getName());
                nodeInfo.put("status", getNodeStatus(node));
                nodes.add(nodeInfo);
            }
            status.put("nodes", nodes);

            // Pod 정보
            List<Map<String, String>> pods = new ArrayList<>();
            for (V1Pod pod : k8sService.getAllPods()) {
                Map<String, String> podInfo = new HashMap<>();
                podInfo.put("name", pod.getMetadata().getName());
                podInfo.put("namespace", pod.getMetadata().getNamespace());
                podInfo.put("status", pod.getStatus().getPhase());
                pods.add(podInfo);
            }
            status.put("pods", pods);

            // 네임스페이스 정보
            List<String> namespaces = new ArrayList<>();
            for (V1Namespace ns : k8sService.getNamespaces()) {
                namespaces.add(ns.getMetadata().getName());
            }
            status.put("namespaces", namespaces);

            // 전송
            String json = objectMapper.writeValueAsString(status);
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }

        } catch (Exception e) {
            log.error("클러스터 상태 전송 실패 : {}", e.getMessage());
        }
    }

    private String getNodeStatus(V1Node node) {
        return node.getStatus().getConditions().stream()
                .filter(c -> "Ready".equals(c.getType()))
                .findFirst()
                .map(c -> "True".equals(c.getStatus()) ? "Ready" : "NotReady")
                .orElse("Unknown");
    }
}