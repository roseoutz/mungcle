# 멍클 (Mungcle)

반려동물 신뢰 기반 산책 커뮤니티 앱.
상대 개의 성향/안전성을 미리 확인하고, 자연스럽게 산책에 합류하는 경험.

## 기술 스택

| Layer | Technology |
|-------|------------|
| Frontend | Expo (React Native) |
| Backend | Kotlin + Spring Boot 3.5 (MSA 6개 서비스) |
| API Gateway | Spring Boot MVC + gRPC Client (BFF) |
| 서비스간 통신 | gRPC (동기) + Kafka (비동기) |
| ORM | JPA (Hibernate 6.3+) |
| DB + Storage | Supabase (PostgreSQL + Storage) |
| Migration | Flyway |
| ID 전략 | TSID (hypersistence-utils) |
| Push | FCM (expo-notifications) |
| Auth | 카카오 OAuth + 이메일 fallback |
| Observability | OpenTelemetry + Grafana + Tempo + Prometheus |

## 서비스 구조

| 서비스 | 포트 | 책임 |
|--------|------|------|
| api-gateway | :4000 | REST 노출, JWT 검증, gRPC 조합 |
| identity | :50051 | 인증, 유저 관리, 차단, 신고 |
| pet-profile | :50052 | 개 프로필 CRUD |
| walks | :50053 | 산책 상태, nearby 조회 |
| social | :50054 | 인사, 메시지 |
| notification | :50055 | 인앱 알림 (Kafka consumer → gRPC) |

## 로컬 실행

### 사전 준비

- JDK 21+
- Docker (Kafka, PostgreSQL 등 인프라)
- Node.js 18+ (프론트엔드)
- Expo CLI (`npm install -g expo-cli`)

### 백엔드

```bash
# 인프라 기동
docker-compose up -d

# 전체 빌드
./gradlew build

# 개별 서비스 실행
./gradlew :services:identity:bootRun
./gradlew :services:pet-profile:bootRun
./gradlew :services:walks:bootRun
```

### 프론트엔드 (Expo)

```bash
cd frontend
npm install
npx expo start
```

### 테스트

```bash
# 전체 테스트
./gradlew test

# 서비스별 테스트
./gradlew :services:identity:test
./gradlew :services:pet-profile:test
./gradlew :services:walks:test
```

## 프로젝트 구조

```
mungcle/
├── CLAUDE.md              # AI 코딩 가이드 (진입점)
├── build.gradle.kts       # 루트 빌드 설정
├── settings.gradle.kts    # 멀티모듈 설정
├── common/                # 공통 모듈 (domain-common, grpc-client, kafka-common)
├── proto/                 # gRPC proto 정의
├── services/              # MSA 서비스들
│   ├── api-gateway/
│   ├── identity/
│   ├── pet-profile/
│   ├── walks/
│   ├── social/
│   └── notification/
├── frontend/              # Expo React Native 앱 (Feature-based)
├── plan-docs/             # 설계/리뷰 문서 ("무엇을" 만드는지)
├── ai/                    # 코딩 가이드 ("어떻게" 만드는지)
└── .claude/rules/         # 파일 유형별 자동 규칙
```

## Phase 1 기능 (MVP)

1. 개 프로필 카드 (성향, 사회성 등급, 예방접종)
2. 산책 상태 토글 ("같이 걸어도 좋아요")
3. 주변 탐색 + 인사하기 + 간단 메시지
4. 차단/신고 시스템
5. 산책 시간대 패턴 (빈 화면 방지)

상세: `plan-docs/ceo-plan.md`

## 문서

- **[Getting Started](docs/getting-started.md)** - 로컬 개발 환경 구성 및 실행 가이드
- 설계/기능: `plan-docs/`
- 코딩 규칙: `ai/`
- 아키텍처 결정: `ai/decisions.md`
