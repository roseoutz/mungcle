# 06. Social — 인사 생성/응답

브랜치: `feature/social-greetings` | 선행: 04 | 예상 PR 사이즈: ~400줄

---

## 도메인

- [ ] `Greeting` 모델 (sender/receiver UserId+DogId, receiverWalkId, status, expiresAt)
- [ ] `GreetingStatus` enum (PENDING/ACCEPTED/EXPIRED)
- [ ] `accept(now)` → ACCEPTED + expiresAt=now+30분, `expire()`, `isPending()`, `isAccepted()`
- [ ] 예외: `GreetingDuplicateException`, `GreetingNotFoundException`, `GreetingExpiredException`, `ForbiddenBlockedException`

## 포트 + Application

- [ ] `CreateGreetingUseCase` — 차단 체크(identity gRPC) + 중복 체크 + PENDING 생성(5분 만료) + Kafka
- [ ] `RespondGreetingUseCase` — accept/reject + Kafka
- [ ] `GetGreetingUseCase` — sender/receiver만 접근
- [ ] `ListGreetingsUseCase` — status/direction 필터
- [ ] `GreetingRepositoryPort`, `SocialEventPublisherPort` (greeting.created, greeting.accepted)
- [ ] `IdentityPort` (isBlocked)

## Infrastructure

- [ ] `GreetingEntity` (JPA, @Tsid, unique constraint, schema=social)
- [ ] `GreetingSpringDataRepository`, `GreetingRepositoryAdapter`, `GreetingMapper`
- [ ] `SocialEventPublisher` (Kafka: greeting.created, greeting.accepted)
- [ ] `IdentityGrpcClient` (isBlocked)
- [ ] `SocialGrpcService` — CreateGreeting, RespondGreeting, GetGreeting, ListGreetings (4 RPC)

## 테스트

- [ ] Unit: Greeting 상태 전이 (PENDING→ACCEPTED, PENDING→EXPIRED)
- [ ] Unit: 차단됨 → 생성 거부, 중복 → 거부, 이미 EXPIRED → 응답 거부
- [ ] Integration: GreetingRepository unique constraint
- [ ] Integration: Kafka greeting.created/accepted 이벤트
- [ ] gRPC: 4 RPC 테스트

## 수락 기준

- [ ] 동일 sender→receiver→walk 중복 불가
- [ ] 차단 관계에서 생성 불가
- [ ] Kafka 이벤트 2종 정상 발행
- [ ] `./gradlew :services:social:test` 통과
