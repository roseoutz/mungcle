# Getting Started

멍클(Mungcle) 로컬 개발 환경 구성 및 실행 가이드.

## 사전 요구사항

| 도구 | 버전 | 비고 |
|------|------|------|
| JDK | 21+ | `java -version`으로 확인 |
| Docker & Docker Compose | 최신 | PostgreSQL, Kafka, 관측성 스택 실행용 |
| Node.js | 18+ | 프론트엔드 빌드용 |
| npm | 9+ | Node.js와 함께 설치됨 |

## 1. 인프라 기동

프로젝트 루트에서 Docker Compose로 인프라를 시작합니다.

```bash
# 기본 인프라 (PostgreSQL + Kafka + 관측성)
docker-compose up -d

# Kafka UI도 함께 띄우려면 (디버깅용)
docker-compose --profile debug up -d

# 전체 서비스까지 Docker로 실행하려면
docker-compose --profile app up -d
```

### 인프라 포트 맵

| 서비스 | 포트 | 용도 |
|--------|------|------|
| PostgreSQL | 5432 | DB (`mungcle` / `postgres:postgres`) |
| Kafka | 9092 | 이벤트 브로커 |
| Kafka UI | 8080 | Kafka 토픽 확인 (debug 프로필) |
| Grafana | 3000 | 대시보드 (`admin:admin`) |
| Prometheus | 9090 | 메트릭 수집 |
| Tempo | 3200 | 분산 트레이싱 |
| OTel Collector | 4317/4318 | OpenTelemetry gRPC/HTTP |

## 2. 백엔드 실행

### 전체 빌드

```bash
./gradlew build
```

### 서비스별 실행

서비스 간 의존 관계가 있으므로 **identity를 먼저** 실행합니다.

```bash
# 1) identity (인증 - 다른 서비스가 의존)
./gradlew :services:identity:bootRun

# 2) pet-profile
./gradlew :services:pet-profile:bootRun

# 3) walks (identity, pet-profile에 의존)
./gradlew :services:walks:bootRun

# 4) social (identity, pet-profile, walks에 의존)
./gradlew :services:social:bootRun

# 5) notification (identity에 의존, Kafka consumer)
./gradlew :services:notification:bootRun

# 6) api-gateway (모든 서비스에 의존)
./gradlew :services:api-gateway:bootRun
```

### 서비스 포트 맵

| 서비스 | HTTP | gRPC | 스키마 |
|--------|------|------|--------|
| api-gateway | 4000 | - | - |
| identity | 8081 | 50051 | `identity` |
| pet-profile | 8082 | 50052 | `pet_profile` |
| walks | 8083 | 50053 | `walks` |
| social | 8084 | 50054 | `social` |
| notification | 8085 | 50055 | `notification` |

### 환경 변수 (기본값 있음)

모든 서비스는 기본값이 설정되어 있어 로컬에서 추가 설정 없이 실행 가능합니다.

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_HOST` | `localhost` | PostgreSQL 호스트 |
| `DB_PORT` | `5432` | PostgreSQL 포트 |
| `DB_NAME` | `mungcle` | 데이터베이스 이름 |
| `DB_USERNAME` | `postgres` | DB 사용자 |
| `DB_PASSWORD` | `postgres` | DB 비밀번호 |
| `KAFKA_BOOTSTRAP` | `localhost:9092` | Kafka 브로커 (walks, social, notification) |
| `JWT_SECRET` | `mungcle-dev-secret-key-change-in-production` | JWT 서명 키 |
| `OTEL_HOST` | `localhost` | OTel Collector 호스트 |

## 3. 프론트엔드 실행

```bash
cd frontend
npm install
```

### 모바일 (Expo Go)

```bash
npx expo start
# QR 코드를 Expo Go 앱으로 스캔
```

### 웹 모드

```bash
npx expo start --web
# 또는
npm run web
```

브라우저에서 `http://localhost:8081`로 접속됩니다.

### 웹 정적 빌드

```bash
npx expo export --platform web
# dist/ 디렉토리에 빌드 결과물 생성
```

## 4. 테스트

### 백엔드

```bash
# 전체
./gradlew test

# 서비스별
./gradlew :services:identity:test
./gradlew :services:pet-profile:test
./gradlew :services:walks:test
./gradlew :services:social:test
./gradlew :services:notification:test
./gradlew :services:api-gateway:test
```

### 프론트엔드

```bash
cd frontend
npm test
```

## 5. API 접근

api-gateway(`localhost:4000`)가 REST API를 노출합니다.

```bash
# 헬스 체크
curl http://localhost:4000/actuator/health

# 이메일 회원가입 예시
curl -X POST http://localhost:4000/api/auth/register/email \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","nickname":"테스터"}'
```

## 6. 관측성 대시보드

인프라 기동 후 아래 URL로 접근할 수 있습니다.

| 도구 | URL | 용도 |
|------|-----|------|
| Grafana | http://localhost:3000 | 메트릭 대시보드, 트레이스 조회 |
| Prometheus | http://localhost:9090 | 메트릭 직접 쿼리 |
| Tempo | http://localhost:3200 | 분산 트레이싱 백엔드 |

Grafana 기본 계정: `admin` / `admin`

## 트러블슈팅

### PostgreSQL 연결 실패

```bash
# Docker 컨테이너 상태 확인
docker-compose ps
# postgres 컨테이너가 healthy인지 확인
```

### Kafka 연결 실패

walks, social, notification 서비스는 Kafka가 필요합니다. Docker Compose로 Kafka를 먼저 기동하세요.

### Flyway 마이그레이션 실패

서비스 최초 실행 시 자동으로 스키마가 생성됩니다. 이미 적용된 마이그레이션이 변경되었다면 DB를 초기화하세요:

```bash
docker-compose down -v  # 볼륨 포함 삭제
docker-compose up -d    # 재기동
```

### 프론트엔드 웹 모드 실행 안 됨

`react-dom`이 설치되어 있는지 확인:

```bash
cd frontend
npm ls react-dom
# 없으면: npm install
```
