# OCI Registry 사용 시
docker build -t [region].ocir.io/[tenancy]/k8s-dashboard:latest .
docker push [region].ocir.io/[tenancy]/k8s-dashboard:latest

# Docker 로그인
docker login ap-chuncheon-1.ocir.io
# [tenancy-namespace]/[OCI username]

# [Auth Token]

# [image]
ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:latest
# 이미지 빌드
docker build -t ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:latest .
# 이미지 푸시
docker push ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:latest

# OCI Registry는 Private이라 Secret 필요
kubectl create secret docker-registry ocir-secret \
--docker-server=ap-chuncheon-1.ocir.io \
--docker-username=[tenancy-namespace]/[OCI username] \
--docker-password='[Auth Token]' \
--docker-email=[이메일] \
-n k8s-dashboard

# Pod 상태 확인
kubectl get pods -n k8s-dashboard
# 로그 확인
kubectl logs -n k8s-dashboard [pod-name]
# 상세 이벤트 확인
kubectl describe pod -n k8s-dashboard [pod-name]
# Secret 확인
kubectl get secret ocir-secret -n k8s-dashboard
# Secret 확인
kubectl describe secret ocir-secret -n k8s-dashboard
# deployment.yaml에 imagePullSecrets 확인
kubectl get deployment k8s-dashboard -n k8s-dashboard -o yaml | grep -A3 imagePullSecrets

# HTTPRoute 상태 확인
kubectl describe httproute k8s-dashboard-route -n k8s-dashboard
# Service 확인
kubectl get svc -n k8s-dashboard
# Pod 로그 확인
kubectl logs -n k8s-dashboard [pod-name]
# 클러스터에서 Pod 재시작
kubectl rollout restart deployment k8s-dashboard -n k8s-dashboard

# v1.0.0 + latest 동시 태깅
docker build -t ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:v1.0.0 \
-t ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:latest .

# 둘 다 푸시
docker push ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:v1.0.0
docker push ap-chuncheon-1.ocir.io/[tenancy-namespace]/k8s-dashboard:latest

# 깃 태그
git tag v1.0.0

# 깃 푸시
git push origin main
git push origin v1.0.0