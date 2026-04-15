# 05. Notification Service

Lane F | 브랜치: `feature/notification-service` | 선행: 01-identity-service
참조: `plan-docs/backend-requirements.md` §5, `proto/notification/v1/notification.proto`

---

## 1. 프로젝트 구조 세팅

- [ ] **1.1** 클린 아키텍처 패키지 생성
- [ ] **1.2** `common:grpc-client` + `common:kafka-common` 의존성 추가
- [ ] **1.3** TSID 설정 + Kafka Consumer + gRPC client (identity) 설정 확인

## 2. 도메인 모델

- [ ] **2.1** `Notification` 도메인 모델
  - id, userId, type(NotificationType), payload(Map), readAt?, createdAt
  - `markRead(now)` — readAt 설정
  - `isRead()` — readAt != null
- [ ] **2.2** `NotificationType` enum
  - GREETING_RECEIVED, GREETING_ACCEPTED, MESSAGE_RECEIVED, WALK_EXPIRED

## 3. 포트 정의

### 인바운드 포트

- [ ] **3.1** `ListNotificationsUseCase` — cursor 기반 페이지네이션
- [ ] **3.2** `MarkReadUseCase` — 단건 읽음
- [ ] **3.3** `MarkAllReadUseCase` — 전체 읽음
- [ ] **3.4** `CreateNotificationUseCase` — Kafka consumer에서 호출

### 아웃바운드 포트

- [ ] **3.5** `NotificationRepositoryPort` — save, findByUserId(cursor), markRead, markAllRead
- [ ] **3.6** `PushSenderPort` — sendPush(userId, title, body) — FCM 발송
- [ ] **3.7** `IdentityPort` — getUser (push token 조회)

## 4. Application 레이어

- [ ] **4.1** `CreateNotificationCommandHandler`
  - Notification 저장
  - identity.getUser(userId) → pushToken 조회
  - pushToken 있으면 FCM 발송 시도
  - 발송 실패해도 인앱 알림은 저장됨 (fallback)
- [ ] **4.2** `ListNotificationsQueryHandler` — cursor(createdAt DESC) + limit
- [ ] **4.3** `MarkReadCommandHandler` — 본인 알림만
- [ ] **4.4** `MarkAllReadCommandHandler`

## 5. Infrastructure 레이어

### JPA + Repository

- [ ] **5.1** `NotificationEntity` (JPA, @Tsid, JSONB payload, schema=notification)
- [ ] **5.2** `NotificationSpringDataRepository` — cursor 쿼리, markAllRead
- [ ] **5.3** `NotificationRepositoryAdapter`
- [ ] **5.4** `NotificationMapper`

### Kafka Consumer

- [ ] **5.5** `GreetingCreatedConsumer` — `greeting.created` 토픽
  - payload: `{senderNickname}님이 인사를 보냈어요`
  - → CreateNotificationUseCase (receiver에게)
- [ ] **5.6** `GreetingAcceptedConsumer` — `greeting.accepted` 토픽
  - payload: `{receiverNickname}님과 상호 인사 완료! 30분간 위치 공유`
  - → CreateNotificationUseCase (sender에게)
- [ ] **5.7** `GreetingExpiredConsumer` — `greeting.expired` 토픽
  - → CreateNotificationUseCase (인앱 알림만, FCM 선택)
- [ ] **5.8** `MessageSentConsumer` — `message.sent` 토픽
  - → CreateNotificationUseCase (receiver에게)
- [ ] **5.9** `WalkExpiredConsumer` — `walk.expired` 토픽
  - → CreateNotificationUseCase (인앱 알림만)
- [ ] **5.10** Kafka consumer idempotent 처리 (eventId 중복 체크)

### FCM Adapter

- [ ] **5.11** `FcmPushSenderAdapter` (PushSenderPort 구현)
  - Expo push token → Expo Push API (또는 FCM 직접)
  - 실패 시 로그만 (인앱 알림으로 fallback)
  - Phase 1에서는 Expo Push API 경유 권장 (expo-server-sdk)

### gRPC Client

- [ ] **5.12** `IdentityGrpcClient` (IdentityPort 구현) — getUser (pushToken 포함)

### gRPC Server

- [ ] **5.13** `NotificationGrpcService` — proto의 `NotificationService` 구현 (3 RPC)

### Config

- [ ] **5.14** `NotificationConfig` — Bean 와이어링 + Kafka consumer config

## 6. 테스트

### Unit

- [ ] **6.1** `Notification` 도메인 — markRead, isRead
- [ ] **6.2** `CreateNotificationCommandHandler` — 저장 + pushToken 있을 때/없을 때
- [ ] **6.3** `ListNotificationsQueryHandler` — cursor 페이지네이션
- [ ] **6.4** Kafka consumer 메시지 파싱 + 중복 처리

### Integration (Testcontainers)

- [ ] **6.5** `NotificationRepositoryAdapter` — save, cursor 조회, markRead, markAllRead
- [ ] **6.6** Kafka consumer 통합 — 이벤트 발행 → consumer 수신 → DB 저장 확인
- [ ] **6.7** gRPC 서버 통합 테스트 (3 RPC)

## 수락 기준

- [ ] 5개 Kafka 토픽 정상 소비
- [ ] FCM 발송 실패 시 인앱 알림 저장됨 (fallback)
- [ ] cursor 기반 페이지네이션 동작
- [ ] idempotent consumer — 동일 eventId 중복 처리 안 함
- [ ] `./gradlew :services:notification:test` 전체 통과
