package kr.imhyuk.k8sdashboard.controller;

import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1Pod;
import kr.imhyuk.k8sdashboard.service.K8sService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final K8sService k8sService;

    @GetMapping("/")
    public String dashboard() {
        return "dashboard";   // ← templates/dashboard.html 렌더링
    }

    // API 엔드포인트는 별도로
    @RestController
    @RequiredArgsConstructor
    @RequestMapping("/api")
    static class ApiController {
        private final K8sService k8sService;

        @GetMapping("/nodes")
        public List<V1Node> getNodes() throws Exception {
            return k8sService.getNodes();
        }

        @GetMapping("/pods")
        public List<V1Pod> getPods() throws Exception {
            return k8sService.getAllPods();
        }

        @GetMapping("/namespaces")
        public List<V1Namespace> getNamespaces() throws Exception {
            return k8sService.getNamespaces();
        }
    }
}