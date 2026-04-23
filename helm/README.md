# Mungcle Helm Chart

멍클(Mungcle) MSA 플랫폼을 Kubernetes에 배포하기 위한 Helm 차트입니다.

## 사전 요구 사항

| 도구 | 버전 | 용도 |
|------|------|------|
| [minikube](https://minikube.sigs.k8s.io/) | v1.32+ | 로컬 Kubernetes 클러스터 |
| [kubectl](https://kubernetes.io/docs/tasks/tools/) | v1.28+ | 클러스터 제어 CLI |
| [helm](https://helm.sh/) | v3.14+ | 차트 패키지 매니저 |
| [Docker](https://www.docker.com/) | 최신 | 이미지 빌드 |

## 빠른 시작 (minikube)

### 1. minikube 클러스터 시작

```bash
minikube start --cpus 4 --memory 6144
minikube addons enable ingress
```

### 2. 서비스 이미지 빌드 (minikube Docker 데몬 사용)

```bash
eval $(minikube docker-env)

for svc in identity pet-profile walks social notification api-gateway; do
  docker build --build-arg SERVICE_NAME=$svc \
    -f services/Dockerfile \
    -t mungcle/$svc:latest \
    .
done
```

### 3. Helm 차트 설치

```bash
helm upgrade --install mungcle ./helm/mungcle \
  --create-namespace \
  --namespace mungcle \
  --wait
```

### 4. /etc/hosts 설정 (로컬 도메인)

```bash
echo "$(minikube ip) mungcle.local" | sudo tee -a /etc/hosts
```

### 5. API Gateway 접근

```bash
curl http://mungcle.local/health
```

## 유용한 명령어

```bash
# 배포 상태 확인
kubectl get all -n mungcle

# 특정 서비스 로그 확인
kubectl logs -n mungcle deployment/identity -f

# Helm 릴리스 상태
helm status mungcle -n mungcle

# 차트 제거
helm uninstall mungcle -n mungcle
```

## EKS 배포로 전환

`values-eks.yaml`은 EKS 환경에 맞는 오버라이드 값을 담고 있습니다.

**사전 작업:**
1. `values-eks.yaml`에서 ECR registry URL, 도메인, ACM 인증서 ARN을 실제 값으로 교체
2. RDS PostgreSQL 및 MSK Kafka 엔드포인트를 ConfigMap/Secret에 설정
3. [AWS Load Balancer Controller](https://kubernetes-sigs.github.io/aws-load-balancer-controller/) 설치

```bash
# EKS 배포
helm upgrade --install mungcle ./helm/mungcle \
  -f helm/mungcle/values.yaml \
  -f helm/mungcle/values-eks.yaml \
  --namespace mungcle \
  --create-namespace \
  --wait
```

EKS에서는 `postgres.enabled: false`, `kafka.enabled: false`로 설정되어 있어 인프라는 AWS 매니지드 서비스(RDS, MSK)를 사용합니다.

## 차트 구조

```
helm/mungcle/
  Chart.yaml              # 차트 메타데이터
  values.yaml             # 기본값 (minikube)
  values-eks.yaml         # EKS 오버라이드
  templates/
    _helpers.tpl          # 공통 헬퍼 함수
    namespace.yaml        # mungcle 네임스페이스
    configmap.yaml        # 공유 환경 변수 (DB, Kafka, OTel)
    secret.yaml           # DB 비밀번호, JWT 시크릿
    # 애플리케이션 서비스 (gRPC 헬스체크)
    identity-{deployment,service}.yaml
    pet-profile-{deployment,service}.yaml
    walks-{deployment,service}.yaml
    social-{deployment,service}.yaml
    notification-{deployment,service}.yaml
    # API Gateway (HTTP 헬스체크 + HPA)
    api-gateway-{deployment,service}.yaml
    # 인프라 (StatefulSet)
    postgres-{statefulset,service}.yaml
    kafka-{statefulset,service}.yaml
    # 라우팅
    ingress.yaml          # ingress-nginx → api-gateway
```

## 시크릿 관리

> **주의:** `values.yaml`의 기본 시크릿 값은 개발 전용입니다. 프로덕션에서는 반드시 변경하세요.

프로덕션 권장 방식:
- **EKS:** [AWS Secrets Manager + External Secrets Operator](https://external-secrets.io/)
- **일반:** `--set secrets.postgresPassword=<value> --set secrets.jwtSecret=<value>`

```bash
helm upgrade --install mungcle ./helm/mungcle \
  --set secrets.postgresPassword=$(openssl rand -base64 32) \
  --set secrets.jwtSecret=$(openssl rand -base64 64)
```
