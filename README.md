# 멍클 (Mungcle)

반려동물 신뢰 기반 산책 커뮤니티 앱.
상대 개의 성향/안전성을 미리 확인하고, 자연스럽게 산책에 합류하는 경험.

## 기술 스택

| Layer | Technology |
|-------|------------|
| Frontend | Expo (React Native) |
| Backend | Kotlin + Spring Boot 3.5 (MSA, 6개 서비스) |
| 서비스간 통신 | gRPC (동기) + Apache Kafka (비동기) |
| ORM / Migration | JPA (Hibernate) + Flyway |
| DB + Storage | Supabase (PostgreSQL + Storage) |
| Push | FCM (expo-notifications) |
| Auth | 카카오 OAuth + 이메일 fallback |
| Observability | OpenTelemetry + Grafana + Tempo + Prometheus |
| Build | Gradle multi-module 모노레포 |
| Container | Docker Compose |

상세: `plan-docs/eng-review.md`, `ai/decisions.md` (ADR-010~022)

## 로컬 실행

### 사전 준비

- Java 21+ (Amazon Corretto 권장)
- Docker & Docker Compose
- Node.js 18+ (프론트엔드)
- Expo CLI (`npm install -g expo-cli`)
- buf CLI (`brew install bufbuild/buf/buf`) — proto codegen

### 인프라 기동

```bash
# PostgreSQL + Kafka + Observability 스택
docker compose up -d

# Kafka UI (디버깅용, 선택)
docker compose --profile debug up -d kafka-ui
```

- PostgreSQL: `localhost:5432` (mungcle / postgres / postgres)
- Kafka: `localhost:9092`
- Grafana: `http://localhost:3000` (admin / admin)

### Proto 코드 생성

```bash
cd proto && buf generate
```

### 백엔드 서비스

```bash
# 전체 빌드
./gradlew build

# 개별 서비스 실행 (예: identity)
./gradlew :services:identity:bootRun

# 개별 서비스 테스트
./gradlew :services:identity:test
```

서비스별 포트:
| 서비스 | gRPC 포트 | 역할 |
|--------|-----------|------|
| api-gateway | REST :4000 | BFF (클라이언트 진입점) |
| identity | :50051 | 인증, 유저, 차단, 신고 |
| pet-profile | :50052 | 개 프로필 |
| walks | :50053 | 산책 상태, 시간대 패턴 |
| social | :50054 | 인사, 메시지 |
| notification | :50055 | FCM, 인앱 알림 |

### 프론트엔드 (Expo)

```bash
cd frontend
npm install
npx expo start
```

## 프로젝트 구조

```
mungcle/
├── CLAUDE.md                  # AI 코딩 가이드 (진입점)
├── build.gradle.kts           # 루트 Gradle
├── settings.gradle.kts        # 모노레포 모듈 정의
├── docker-compose.yml         # 인프라 + 서비스
├── proto/                     # gRPC Proto 정의 (buf)
├── common/
│   ├── domain-common/         # GridCell VO, TsidConfig
│   └── kafka-common/          # 이벤트 DTO, 토픽 상수
├── services/
│   ├── api-gateway/           # Spring MVC BFF (REST → gRPC)
│   ├── identity/              # 인증 + 유저 + 차단/신고
│   ├── pet-profile/           # 개 프로필
│   ├── walks/                 # 산책 + 패턴
│   ├── social/                # 인사 + 메시지
│   └── notification/          # FCM + 알림함
├── frontend/                  # Expo React Native 앱
├── infra/                     # Observability 설정
├── plan-docs/                 # 설계/리뷰 문서
├── ai/                        # 코딩 가이드
└── .claude/rules/             # 파일 유형별 자동 규칙
```

## Phase 1 기능 (MVP)

1. 개 프로필 카드 (성향, 사회성 등급, 예방접종)
2. 산책 상태 토글 ("같이 걸어도 좋아요")
3. 주변 탐색 + 인사하기 + 간단 메시지
4. 차단/신고 시스템
5. 산책 시간대 패턴 (빈 화면 방지)

상세: `plan-docs/ceo-plan.md`

## 문서

- 설계/기능: `plan-docs/`
- 코딩 규칙: `ai/`
- 아키텍처 결정: `ai/decisions.md`
