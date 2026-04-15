# 08. Social — 메시지 + CRON 만료

브랜치: `feature/social-messages-cron` | 선행: 06 | 예상 PR 사이즈: ~300줄

---

## 도메인

- [ ] `Message` 모델 (greetingId, senderUserId, body 1~140자, createdAt)
- [ ] `Greeting` 확장 — `canSendMessage(now)` (ACCEPTED && now < expiresAt)
- [ ] 예외: `GreetingNotAcceptedException`, `MessageTooLongException`

## 포트 + Application

- [ ] `SendMessageUseCase` — ACCEPTED + 30분 내 확인 + 저장 + Kafka message.sent
- [ ] `ListMessagesUseCase` — greetingId별 목록
- [ ] `ExpireGreetingsUseCase` — PENDING 5분 + ACCEPTED 30분 만료 + Kafka greeting.expired
- [ ] `MessageRepositoryPort`
- [ ] `WalksPort` (getWalkGridCell, 위치 공유용)

## Infrastructure

- [ ] `MessageEntity` (JPA, @Tsid, schema=social)
- [ ] `MessageSpringDataRepository`, `MessageRepositoryAdapter`
- [ ] `SocialEventPublisher` 확장 — message.sent, greeting.expired 추가
- [ ] `WalksGrpcClient` (getWalkGridCell)
- [ ] `SocialGrpcService` 확장 — SendMessage, ListMessages (2 RPC 추가)
- [ ] `GreetingExpiryScheduler` (`@Scheduled(fixedRate = 60_000)`)

## 테스트

- [ ] Unit: canSendMessage (ACCEPTED 내/만료 후), 140자 초과 거부
- [ ] Unit: PENDING 5분 + ACCEPTED 30분 만료 로직
- [ ] Integration: Message CRUD + Kafka 이벤트
- [ ] gRPC: SendMessage, ListMessages

## 수락 기준

- [ ] ACCEPTED 전 메시지 전송 불가
- [ ] 30분 만료 후 메시지 전송 불가
- [ ] CRON 만료 동작 (PENDING 5분, ACCEPTED 30분)
- [ ] Kafka greeting.expired, message.sent 발행
- [ ] `./gradlew :services:social:test` 통과
