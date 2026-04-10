# 아키텍처 결정 기록 (ADR)

이미 검토하고 결정한 사항. AI가 대안을 다시 제안하지 않도록 기록.

## ADR-001: 위치 발견 → 신뢰/안전 중심으로 전환

- **결정:** 실시간 위치 기반 발견이 아니라 개의 신뢰 프로필이 핵심 가치
- **이유:** 독립 AI 리뷰어가 "지도의 점은 공원에서 직접 보는 것보다 정보가 적다. 발견이 아니라 접근 장벽이 진짜 문제"라고 도전. 창업자가 수용.
- **대안 검토:** 실시간 지도 기반 (프라이버시 리스크 + 밀도 문제), 카카오톡 오픈채팅 (제품 경험 아님)

## ADR-002: 웹 → Expo (React Native) 앱

- **결정:** 웹 SPA가 아닌 네이티브 앱
- **이유:** 푸시 알림이 핵심 기능(인사하기 알림). 웹에서는 앱을 열지 않으면 알림 수신 불가. PWA도 iOS 제약이 큼.
- **대안 검토:** React SPA + 카카오 알림톡 (추가 비용/복잡도), PWA (iOS 푸시 제약)

## ADR-003: ~~Prisma over TypeORM~~ → ADR-010에서 대체됨

- **결정:** ~~Prisma 사용~~ → **JPA (Hibernate) + Flyway + TSID로 전환**
- **이유:** ~~타입 안전성, 자동 마이그레이션, 직관적 스키마 정의. NestJS 공식 통합 지원.~~
- **대체:** ADR-010 참고. Kotlin + Spring Boot 전환에 따라 JPA/Hibernate로 변경.

## ADR-004: Supabase (DB + Storage)

- **결정:** PostgreSQL과 파일 스토리지를 Supabase로 통합
- **이유:** 무료 tier (500MB DB + 1GB Storage). 하나의 인프라에서 DB와 파일 관리.
- **대안 검토:** 별도 PostgreSQL + S3 (MVP에 과함), 로컬 파일 저장 (배포 시 유실)

## ADR-005: 200m 그리드 스냅 (GPS 좌표 미저장)

- **결정:** 정확한 GPS 좌표를 저장/전송하지 않고 200m 그리드 중심점으로 스냅
- **이유:** 프라이버시 보호. 스토킹 방지. 한국 사용자의 위치 공유 거부감.
- **구현:** `floor(lat / 0.002) * 0.002` — PostGIS 불필요
- **대안 검토:** 정확한 위치 (프라이버시 리스크), 동네 단위만 (너무 넓음)

## ADR-006: 클린/헥사고날 아키텍처 (Backend) — 유지, MSA 서비스별 적용

- **결정:** domain / application / infrastructure / presentation 4레이어 → **MSA 각 서비스 내부에 동일 적용**
- **이유:** 도메인 로직을 프레임워크 독립적으로 테스트 가능. 인프라 교체 용이.
- **변경:** 모놀리스 단일 구조에서 서비스별(identity, pet-profile, walks, social, notification) 각각 적용으로 확대. ADR-010 참고.

## ADR-007: Feature-based 아키텍처 (Frontend)

- **결정:** features/ 디렉토리에 기능별 폴더 분리
- **이유:** 스쿼드 단위 개발에 최적화. 폴더가 곧 팀 경계. 기능 간 격리.
- **대안 검토:** Type-based (규모 커지면 한계), Clean Architecture (React에서 과설계)

## ADR-008: Phase 1/2 분리

- **결정:** 9개 기능을 Phase 1(5개) + Phase 2(4개)로 분리
- **이유:** Outside voice가 "9개는 MVP가 아니다"라고 지적. 가설 검증에 필요한 최소 기능만 먼저.
- **Phase 1:** 개 프로필, 산책 상태, 탐색+인사+메시지, 차단/신고, 시간대 패턴
- **Phase 2:** 산책 로그, 산책 친구, 산책 예고(친구만), 장소 리뷰

## ADR-009: 커밋 메시지 한국어

- **결정:** 커밋 메시지는 한국어로 작성
- **이유:** 팀원 모두 한국어 사용자. 코드는 영어, 커밋/주석/문서는 한국어.

---

# MSA 전환 결정 (2026-04-10)

아래 ADR들은 NestJS 모놀리스에서 Kotlin MSA로의 아키텍처 전환 결정을 기록한다.

## ADR-010: NestJS 모놀리스 → Kotlin + Spring Boot MSA 전환

- **결정:** NestJS 모놀리스를 Kotlin + Spring Boot 3.5 기반 MSA로 전환
- **목적:** (a) MSA + gRPC 아키텍처 학습/포트폴리오 + (d) bounded context 기반 도메인 분리
- **이유:** 개발자의 주력 언어가 Kotlin. 아키텍처 패턴 학습이 목적이므로 주력 언어로 프레임워크 삽질 없이 집중하는 것이 합리적.
- **대안 검토:** NestJS 유지 (언어 학습 부담 추가), Go 혼합 폴리글랏 (산만해짐)
- **영향:** ADR-003(Prisma) 대체, ADR-006(클린 아키텍처) 유지 + 서비스별 적용

## ADR-011: 서비스 경계 — Bounded Context 5+1

- **결정:** 6개 서비스로 분리
  - `api-gateway` — REST BFF, JWT 검증, gRPC 라우팅/조합
  - `identity-service` — Auth, Users, Blocks, Reports
  - `pet-profile-service` — Dogs (신뢰 메타데이터)
  - `walks-service` — Walks, WalkPatterns
  - `social-service` — Greetings, Messages
  - `notification-service` — FCM, 인앱 알림
- **이유:** 5개 bounded context가 자연스러운 도메인 경계. 너무 잘게(8개) 쪼개면 운영 부담, 너무 크게(3개) 합치면 gRPC 학습 가치 감소.
- **대안 검토:** Fine-grained 8개 (과함), Coarse 3개 (형식적 MSA)

## ADR-012: DB 스키마 분리 — 물리 DB 1개, 논리 스키마 분리

- **결정:** Supabase PostgreSQL 1개 인스턴스, 서비스별 PostgreSQL 스키마(`identity`, `pet_profile`, `walks`, `social`, `notification`)로 논리 분리.
- **규칙:** Cross-schema JOIN 금지. 다른 서비스 데이터 필요 시 반드시 gRPC 호출.
- **이유:** 비용 0 (Supabase 무료 1개). gRPC 호출 강제로 MSA 규율 확보. 물리 분리 전환 시 스키마 단위로 이전 가능.
- **대안 검토:** 물리 DB 분리 (Supabase 무료 2개 제한), 공유 스키마 + 문서만 (규율 없음)

## ADR-013: 서비스간 동기 통신 — gRPC

- **결정:** 서비스간 동기 통신은 gRPC unary.
- **이유:** 강타입 proto 계약, Kotlin coroutine과 궁합 좋음, 학습 목적에 부합.
- **클라이언트↔게이트웨이:** REST/HTTP (Expo에서 gRPC 비현실적).
- **대안 검토:** REST 서비스간 통신 (타입 안전성 약함), gRPC-Web 클라이언트까지 (Expo 생산성 저하)

## ADR-014: API Gateway — Spring Boot MVC + gRPC Client (BFF)

- **결정:** Spring Cloud Gateway(SCG) 대신 Spring Boot MVC에 gRPC client stub을 직접 호출하는 BFF 패턴.
- **이유:** SCG는 WebFlux(Netty) 기반이라 Virtual Thread 불가. 멍클의 대부분 API는 여러 서비스를 조합(aggregation)해야 해서 컨트롤러에서 명시적으로 호출하는 게 자연스러움.
- **대안 검토:** SCG (VT 불가, gRPC 라우팅 미지원), Kong/Envoy (aggregation 어려움)

## ADR-015: 비동기 통신 — Apache Kafka

- **결정:** 비동기 이벤트 버스로 Apache Kafka 사용.
- **용도:** 알림 발송 (fire-and-forget), 패턴 집계 갱신, 만료 이벤트 전파 등 "결과를 안 기다려도 되는" 호출.
- **이유:** 실무 MSA 표준 이벤트 브로커. Docker Compose로 로컬 운영 가능. Spring Kafka 네이티브 지원.
- **대안 검토:** BullMQ/Redis (가볍지만 학습 가치 낮음), gRPC streaming (실무에서 드뭄), NATS (생태계 작음)

## ADR-016: Coroutine vs Virtual Thread 사용 기준

- **결정:** Blocking I/O → Virtual Thread, Non-blocking → Coroutine.
- **기준:**
  - VT: Spring MVC (api-gateway REST), JPA/JDBC, Kafka Consumer
  - Coroutine: gRPC 서버 핸들러, Kafka Producer, 서비스 내부 비동기 조합 (`async`/`awaitAll`)
- **이유:** JPA는 blocking JDBC라 coroutine과 안티패턴. gRPC-kotlin은 coroutine 네이티브. 각 기술의 자연스러운 모델을 따름.

## ADR-017: ORM / 마이그레이션 / ID 전략

- **결정:** JPA (Hibernate 6.3+) + Flyway + TSID
- **JPA:** Spring Boot 표준. Kotlin과 자연스러운 통합.
- **Flyway:** Spring Boot 네이티브 통합. 서비스별 `db/migration/` 분리. SQL 기반 직관적 버전 관리.
- **TSID:** 64-bit (`Long`/`BIGINT`). `hypersistence-utils`의 `@Tsid` 어노테이션으로 JPA 네이티브 지원. Node ID는 서비스 이름 기반 자동 할당 → MSA에서 충돌 구조적 불가능.
- **대안 검토:** Prisma (Kotlin 미지원), Exposed (JetBrains이지만 생태계 작음), Liquibase (Flyway보다 복잡), UUID (128-bit → 인덱스 비효율), ULID (JPA 통합 수동)

## ADR-018: 모노레포 + buf + grpc-kotlin

- **결정:** Gradle multi-module 모노레포 + buf (proto 관리) + grpc-kotlin (코드 생성).
- **구조:** `proto/` 공유 → `packages/grpc-client` 생성 → 모든 서비스가 참조.
- **이유:** 혼자 개발 시 proto 변경 + 서비스 코드 변경을 한 커밋으로. buf의 lint/breaking change 감지는 실무 MSA 핵심 도구.
- **대안 검토:** 폴리레포 + proto 서브모듈 (혼자 개발에 오버헤드), protoc 직접 (lint/breaking change 없음)

## ADR-019: Docker Compose 우선 배포

- **결정:** 개발/검증은 Docker Compose. 클라우드 배포는 Go/No-go 통과 후 결정.
- **이유:** (a) 학습 목적의 핵심은 아키텍처 패턴이지 클라우드 배포가 아님. docker-compose로 6개 서비스 풀스택 실행이 포트폴리오 가치 충분. TestFlight 테스트 시 ngrok 터널링.
- **후보:** Go/No-go 통과 후 Fly.io 또는 Railway.

## ADR-020: 관측성 — OpenTelemetry + Grafana 스택

- **결정:** OpenTelemetry + Grafana + Tempo + Prometheus.
- **구성:** otel-collector (trace/metric 수집), tempo (분산 트레이싱), prometheus (메트릭), grafana (대시보드). Docker Compose에 포함.
- **이유:** 분산 트레이싱은 MSA 핵심 역량. Spring Boot 3.5 Micrometer Tracing + OTel 자동 계측으로 코드 작업 최소. gRPC 호출 체인 시각화 가능.
- **대안 검토:** 로그만 (디버깅 고통), Zipkin만 (메트릭/대시보드 없음)

## ADR-021: 테스트 전략

- **결정:** JUnit 5 + MockK + Testcontainers + grpc-testing. Unit + Integration 우선, E2E는 나중에.
- **이유:** MockK은 Kotlin 네이티브 mocking. Testcontainers로 PostgreSQL/Kafka 실제 컨테이너 통합 테스트. 서비스별 독립 테스트 가능.
- **범위:** 서비스당 Unit (도메인 로직) + Integration (JPA + Kafka + gRPC). E2E (docker-compose 전체) 는 안정화 후 추가.

## ADR-022: CRON/스케줄러 — 도메인 서비스 자체 처리

- **결정:** 각 도메인 서비스가 자기 데이터의 만료/상태 전이를 직접 처리. 결과를 Kafka 이벤트로 notification-service에 전파.
- **이유:** bounded context 원칙에 충실 — 데이터 소유자가 상태 전이도 소유. 별도 scheduler-service는 모든 서비스를 알아야 해서 결합도 증가.
- **대안 검토:** 별도 scheduler-service (결합도↑), Kafka 지연 이벤트 (네이티브 미지원, 복잡)
