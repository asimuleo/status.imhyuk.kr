package kr.imhyuk.k8sdashboard.service;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class K8sService {

    private final CoreV1Api api;

    public K8sService() throws IOException {
        ApiClient client = Config.fromCluster();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        this.api = new CoreV1Api();
    }

    // 노드 목록 조회
    public List<V1Node> getNodes() throws ApiException {
        return api.listNode().execute().getItems();
    }

    // Pod 목록 조회 (전체 네임스페이스)
    public List<V1Pod> getAllPods() throws ApiException {
        return api.listPodForAllNamespaces().execute().getItems();
    }

    // 네임스페이스 목록 조회
    public List<V1Namespace> getNamespaces() throws ApiException {
        return api.listNamespace().execute().getItems();
    }
}