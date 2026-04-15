# 07. Notification — Kafka Consumer + FCM + 알림함

브랜치: `feature/notification-service` | 선행: 02 | 예상 PR 사이즈: ~400줄

---

## 도메인

- [ ] `Notification` 모델 (userId, type, payload, readAt?, createdAt)
- [ ] `NotificationType` enum (GREETING_RECEIVED, GREETING_ACCEPTED, MESSAGE_RECEIVED, WALK_EXPIRED)
- [ ] `markRead(now)`, `isRead()`

## 포트 + Application

- [ ] `CreateNotificationUseCase` — 저장 + FCM 발송 시도 (실패해도 저장은 유지)
- [ ] `ListNotificationsUseCase` — cursor 페이지네이션
- [ ] `MarkReadUseCase`, `MarkAllReadUseCase`
- [ ] `NotificationRepositoryPort`, `PushSenderPort`, `IdentityPort` (getUser for pushToken)

## Infrastructure

- [ ] `NotificationEntity` (JPA, @Tsid, JSONB payload, schema=notification)
- [ ] `NotificationSpringDataRepository` (cursor 쿼리), `NotificationRepositoryAdapter`
- [ ] Kafka Consumer 5개: greeting.created, greeting.accepted, greeting.expired, message.sent, walk.expired
- [ ] Idempotent consumer (eventId 중복 체크)
- [ ] `FcmPushSenderAdapter` (Expo Push API 경유)
- [ ] `IdentityGrpcClient` (getUser → pushToken)
- [ ] `NotificationGrpcService` — 3 RPC (List, MarkRead, MarkAllRead)

## 테스트

- [ ] Unit: Notification markRead, 중복 이벤트 무시
- [ ] Integration: Kafka consumer 5 토픽 수신 → DB 저장
- [ ] Integration: cursor 페이지네이션, markAllRead
- [ ] gRPC: 3 RPC

## 수락 기준

- [ ] 5개 Kafka 토픽 정상 소비
- [ ] FCM 실패 시 인앱 알림 fallback
- [ ] idempotent consumer 동작
- [ ] `./gradlew :services:notification:test` 통과
