# 05. Walks — 시간대 패턴 + CRON 만료 + Kafka

브랜치: `feature/walks-patterns-cron` | 선행: 04 | 예상 PR 사이즈: ~300줄

---

## 도메인

- [ ] `WalkPattern` 모델 (gridCell, hourOfDay, dogId, walkCount, lastWalkedAt)

## 포트 + Application

- [ ] `GetNearbyPatternsUseCase` — ±1시간, 3x3 grid, 14일, ACTIVE 제외, top 10
- [ ] `ExpireWalksUseCase` — ACTIVE Walk 중 endsAt < now → ENDED + Kafka 발행
- [ ] `WalkPatternRepositoryPort` (findByGridCellsAndHour, upsert)
- [ ] `WalkEventPublisherPort` (publishWalkExpired)
- [ ] StartWalk에서 WalkPattern upsert 로직 추가

## Infrastructure

- [ ] `WalkPatternEntity` (JPA, unique constraint)
- [ ] `WalkPatternSpringDataRepository`, `WalkPatternRepositoryAdapter`
- [ ] `WalkExpiredEventPublisher` (Kafka producer)
- [ ] `WalksGrpcService` 확장 — GetNearbyPatterns RPC 추가
- [ ] `WalkExpiryScheduler` (`@Scheduled(fixedRate = 60_000)`)

## 테스트

- [ ] Unit: ±1시간 필터, 14일 경과 제외, ACTIVE 중복 제외, top 10
- [ ] Unit: ExpireWalks 만료 대상 찾기 + 이벤트 발행 검증
- [ ] Integration: WalkPattern upsert + 집계 쿼리
- [ ] Integration: Kafka walk.expired 이벤트 발행 + 수신 (Testcontainers)

## 수락 기준

- [ ] CRON 60분 만료 동작 (시간 주입 테스트)
- [ ] 패턴 조회에서 현재 ACTIVE Walk Dog 중복 없음
- [ ] Kafka `walk.expired` 이벤트 정상 발행
- [ ] `./gradlew :services:walks:test` 통과
