# 04. Social Service

Lane E | 브랜치: `feature/social-service` | 선행: 01-identity-service, 03-walks-service
참조: `plan-docs/backend-requirements.md` §4, `proto/social/v1/social.proto`

---

## 1. 프로젝트 구조 세팅

- [ ] **1.1** 클린 아키텍처 패키지 생성
- [ ] **1.2** `common:grpc-client` + `common:kafka-common` 의존성 추가
- [ ] **1.3** TSID 설정 + Kafka Producer + gRPC client (identity, pet-profile, walks) 설정 확인

## 2. 도메인 모델

- [ ] **2.1** `Greeting` 도메인 모델
  - id, senderUserId, receiverUserId, senderDogId, receiverDogId?, receiverWalkId
  - status(PENDING/ACCEPTED/EXPIRED), createdAt, respondedAt?, expiresAt
  - `accept(now)` — PENDING → ACCEPTED, respondedAt 설정, expiresAt = now + 30분
  - `expire()` — → EXPIRED
  - `isPending()`, `isAccepted()`, `isExpired(now)`
  - `canSendMessage(now)` — ACCEPTED && now < expiresAt
- [ ] **2.2** `GreetingStatus` enum (PENDING, ACCEPTED, EXPIRED)
- [ ] **2.3** `Message` 도메인 모델
  - id, greetingId, senderUserId, body(1~140자), createdAt
- [ ] **2.4** 도메인 예외
  - `GreetingDuplicateException`, `GreetingNotFoundException`, `GreetingExpiredException`
  - `GreetingNotAcceptedException`, `MessageTooLongException`, `ForbiddenBlockedException`

## 3. 포트 정의

### 인바운드 포트

- [ ] **3.1** `CreateGreetingUseCase` — 인사 생성 (차단 체크, 중복 체크, expiresAt = now + 5분)
- [ ] **3.2** `RespondGreetingUseCase` — 수락/거절 (수락 시 ACCEPTED + 30분 만료)
- [ ] **3.3** `GetGreetingUseCase` — 상세 조회 (양쪽만 접근 가능)
- [ ] **3.4** `ListGreetingsUseCase` — 내가 보낸/받은 목록 (status 필터)
- [ ] **3.5** `SendMessageUseCase` — 메시지 전송 (ACCEPTED + 30분 내만)
- [ ] **3.6** `ListMessagesUseCase` — 메시지 목록
- [ ] **3.7** `ExpireGreetingsUseCase` — CRON: PENDING 5분 + ACCEPTED 30분 만료

### 아웃바운드 포트

- [ ] **3.8** `GreetingRepositoryPort` — save, findById, findBySenderOrReceiver, findExpiredPending, findExpiredAccepted, existsBySenderReceiverWalk
- [ ] **3.9** `MessageRepositoryPort` — save, findByGreetingId
- [ ] **3.10** `SocialEventPublisherPort` — publishGreetingCreated, publishGreetingAccepted, publishGreetingExpired, publishMessageSent
- [ ] **3.11** `IdentityPort` — isBlocked (gRPC client)
- [ ] **3.12** `WalksPort` — getWalkGridCell (gRPC client, 위치 공유용)

## 4. Application 레이어

- [ ] **4.1** `CreateGreetingCommandHandler`
  - identity.isBlocked(sender, receiver) → 차단 시 ForbiddenBlockedException
  - existsBySenderReceiverWalk → 중복 시 GreetingDuplicateException
  - Greeting(PENDING, expiresAt = now + 5분) 생성
  - Kafka `greeting.created` 발행
- [ ] **4.2** `RespondGreetingCommandHandler`
  - findById → PENDING 상태 확인
  - accept=true: greeting.accept(now), expiresAt = now + 30분 → Kafka `greeting.accepted`
  - accept=false: greeting.expire() → Kafka `greeting.expired`
- [ ] **4.3** `GetGreetingQueryHandler` — sender 또는 receiver만 접근 가능
- [ ] **4.4** `ListGreetingsQueryHandler` — status/direction 필터
- [ ] **4.5** `SendMessageCommandHandler`
  - greeting 조회 → canSendMessage(now) 확인
  - ACCEPTED 아니면 GreetingNotAcceptedException
  - 만료면 GreetingExpiredException
  - body 길이 검증 (1~140)
  - Message 저장 → Kafka `message.sent`
- [ ] **4.6** `ListMessagesQueryHandler`
- [ ] **4.7** `ExpireGreetingsCommandHandler`
  - PENDING: expiresAt < now → EXPIRED → Kafka `greeting.expired` (type=PENDING)
  - ACCEPTED: expiresAt < now → EXPIRED → Kafka `greeting.expired` (type=ACCEPTED)

## 5. Infrastructure 레이어

### JPA + Repository

- [ ] **5.1** `GreetingEntity` (JPA, @Tsid, unique constraint, schema=social)
- [ ] **5.2** `MessageEntity` (JPA, @Tsid)
- [ ] **5.3** `GreetingSpringDataRepository` — 복합 쿼리 (sender/receiver, status, expiry)
- [ ] **5.4** `MessageSpringDataRepository`
- [ ] **5.5** `GreetingRepositoryAdapter`, `MessageRepositoryAdapter`
- [ ] **5.6** `GreetingMapper`, `MessageMapper`

### Kafka Producer

- [ ] **5.7** `SocialEventPublisher` (SocialEventPublisherPort 구현) — 4개 이벤트

### gRPC Client

- [ ] **5.8** `IdentityGrpcClient` (IdentityPort 구현) — isBlocked
- [ ] **5.9** `WalksGrpcClient` (WalksPort 구현) — getWalkGridCell

### gRPC Server

- [ ] **5.10** `SocialGrpcService` — proto의 `SocialService` 구현 (6 RPC)
- [ ] **5.11** gRPC 예외 처리 인터셉터

### Scheduler

- [ ] **5.12** `GreetingExpiryScheduler` — `@Scheduled(fixedRate = 60_000)` → ExpireGreetingsUseCase

### Config

- [ ] **5.13** `SocialConfig` — Bean 와이어링

## 6. 테스트

### Unit

- [ ] **6.1** `Greeting` 도메인 — accept, expire, isPending, canSendMessage, 상태 전이 검증
- [ ] **6.2** `CreateGreetingCommandHandler` — 정상, 차단됨, 중복
- [ ] **6.3** `RespondGreetingCommandHandler` — 수락, 거절, 이미 EXPIRED
- [ ] **6.4** `SendMessageCommandHandler` — 정상, ACCEPTED 아닌 상태, 만료 후, 140자 초과
- [ ] **6.5** `ExpireGreetingsCommandHandler` — PENDING 5분, ACCEPTED 30분 만료 + 이벤트

### Integration (Testcontainers)

- [ ] **6.6** `GreetingRepositoryAdapter` — CRUD + unique constraint + 만료 쿼리
- [ ] **6.7** `MessageRepositoryAdapter` — save + findByGreetingId
- [ ] **6.8** Kafka 이벤트 4종 발행 + 수신
- [ ] **6.9** gRPC 서버 통합 테스트 (6 RPC)

## 수락 기준

- [ ] 동일 sender→receiver→walk 중복 인사 불가 (ALREADY_EXISTS)
- [ ] 차단 관계에서 인사 생성 불가 (PERMISSION_DENIED)
- [ ] PENDING 5분 자동 만료 동작
- [ ] ACCEPTED 30분 자동 만료 후 메시지 전송 불가 (FAILED_PRECONDITION)
- [ ] ACCEPTED 전 메시지 전송 불가
- [ ] Kafka 이벤트 4종 정상 발행
- [ ] `./gradlew :services:social:test` 전체 통과
