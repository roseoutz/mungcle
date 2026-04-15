# 03. Walks Service

Lane D | 브랜치: `feature/walks-service` | 선행: 01-identity-service
참조: `plan-docs/backend-requirements.md` §3, §7, `proto/walks/v1/walks.proto`

---

## 1. 프로젝트 구조 세팅

- [ ] **1.1** 클린 아키텍처 패키지 생성
- [ ] **1.2** `common:grpc-client` + `common:kafka-common` 의존성 추가
- [ ] **1.3** TSID 자동 설정 Bean
- [ ] **1.4** Kafka Producer 설정 확인 (application.yml)
- [ ] **1.5** gRPC client 설정 확인 (identity, pet-profile)

## 2. 도메인 모델

- [ ] **2.1** `Walk` 도메인 모델
  - id, dogId, userId, type(OPEN/SOLO), gridCell(GridCell VO), status(ACTIVE/ENDED), startedAt, endsAt, endedAt?
  - `isExpired(now)` — endsAt 이후인지
  - `isOpen()` — type=OPEN && status=ACTIVE
  - `end(now)` — status → ENDED, endedAt 설정
- [ ] **2.2** `WalkType` enum (OPEN, SOLO)
- [ ] **2.3** `WalkStatus` enum (ACTIVE, ENDED)
- [ ] **2.4** `WalkPattern` 도메인 모델
  - gridCell, hourOfDay, dogId, walkCount, lastWalkedAt
- [ ] **2.5** 도메인 예외
  - `WalkAlreadyActiveException`, `WalkNotFoundException`, `WalkAlreadyEndedException`

## 3. 포트 정의

### 인바운드 포트

- [ ] **3.1** `StartWalkUseCase` — 산책 시작 (dogId 중복 ACTIVE 체크, gridCell 스냅)
- [ ] **3.2** `StopWalkUseCase` — 산책 종료 (본인 확인)
- [ ] **3.3** `GetNearbyWalksUseCase` — 3x3 grid, OPEN만, 차단 제외, 본인 제외
- [ ] **3.4** `GetMyActiveWalksUseCase` — 내 진행 중 Walk 목록
- [ ] **3.5** `GetWalkGridCellUseCase` — walkId → gridCell (위치 공유용)
- [ ] **3.6** `GetNearbyPatternsUseCase` — 현재 시간 ±1시간, 3x3, 14일 집계, top 10
- [ ] **3.7** `ExpireWalksUseCase` — CRON: 60분 초과 ACTIVE Walk → ENDED

### 아웃바운드 포트

- [ ] **3.8** `WalkRepositoryPort` — save, findById, findActiveByDogId, findActiveOpenByGridCells, findByUserId(active), updateStatus
- [ ] **3.9** `WalkPatternRepositoryPort` — findByGridCellsAndHour, upsert
- [ ] **3.10** `WalkEventPublisherPort` — publishWalkExpired (Kafka)
- [ ] **3.11** `IdentityPort` — getBlockedUserIds (gRPC client)

## 4. Application 레이어

- [ ] **4.1** `StartWalkCommandHandler`
  - dogId로 ACTIVE Walk 조회 → 있으면 `WalkAlreadyActiveException`
  - GridCell.fromCoordinates(lat, lng) — **gateway에서 받은 gridCell 사용 (lat/lng 아님, ADR-005)**
  - Walk(ACTIVE, endsAt = now + 60분) 생성 + 저장
  - WalkPattern upsert (gridCell + hourOfDay + dogId)
- [ ] **4.2** `StopWalkCommandHandler`
  - findById → userId 확인 → walk.end(now)
  - 이미 ENDED면 `WalkAlreadyEndedException`
- [ ] **4.3** `GetNearbyWalksQueryHandler`
  - GridCell.adjacentCells(gridCell) → 9개 셀
  - findActiveOpenByGridCells → 본인 userId 제외
  - blockedUserIds로 필터링
  - gridDistance 계산
- [ ] **4.4** `GetMyActiveWalksQueryHandler`
- [ ] **4.5** `GetWalkGridCellQueryHandler`
- [ ] **4.6** `GetNearbyPatternsQueryHandler`
  - 현재 hour ±1 (예: 18시면 17, 18, 19)
  - adjacentCells(gridCell)
  - 14일 내 데이터만
  - 현재 ACTIVE OPEN Walk가 없는 Dog만 (중복 방지)
  - top 10 by walkCount DESC
- [ ] **4.7** `ExpireWalksCommandHandler`
  - ACTIVE Walk 중 endsAt < now → ENDED
  - 각각 Kafka `walk.expired` 이벤트 발행

## 5. Infrastructure 레이어

### JPA + Repository

- [ ] **5.1** `WalkEntity` (JPA, @Tsid, schema=walks)
- [ ] **5.2** `WalkPatternEntity` (JPA, unique constraint)
- [ ] **5.3** `WalkSpringDataRepository` — 복합 인덱스 쿼리 (@Query)
- [ ] **5.4** `WalkPatternSpringDataRepository`
- [ ] **5.5** `WalkRepositoryAdapter`, `WalkPatternRepositoryAdapter`
- [ ] **5.6** `WalkMapper`, `WalkPatternMapper`

### Kafka Producer

- [ ] **5.7** `WalkExpiredEventPublisher` (WalkEventPublisherPort 구현)

### gRPC Client

- [ ] **5.8** `IdentityGrpcClient` (IdentityPort 구현) — getBlockedUserIds

### gRPC Server

- [ ] **5.9** `WalksGrpcService` — proto의 `WalksService` 구현 (6 RPC)
- [ ] **5.10** gRPC 예외 처리 인터셉터

### Scheduler

- [ ] **5.11** `WalkExpiryScheduler` — `@Scheduled(fixedRate = 60_000)` → ExpireWalksUseCase

### Config

- [ ] **5.12** `WalksConfig` — Bean 와이어링 + Kafka config

## 6. 테스트

### Unit

- [ ] **6.1** `Walk` 도메인 모델 — isExpired, isOpen, end
- [ ] **6.2** `GridCell` — fromCoordinates 경계값 (적도, 음수, 0/0, 일반), adjacentCells, gridDistance
- [ ] **6.3** `StartWalkCommandHandler` — 정상 시작, 중복 ACTIVE 거부
- [ ] **6.4** `StopWalkCommandHandler` — 정상 종료, 이미 ENDED 거부, 타인 Walk 거부
- [ ] **6.5** `GetNearbyWalksQueryHandler` — 3x3 필터, SOLO 제외, 본인 제외, 차단 제외
- [ ] **6.6** `GetNearbyPatternsQueryHandler` — ±1시간, 14일, ACTIVE 제외, top 10
- [ ] **6.7** `ExpireWalksCommandHandler` — 만료 대상 찾기, Kafka 이벤트 발행

### Integration (Testcontainers)

- [ ] **6.8** `WalkRepositoryAdapter` — CRUD + 복합 인덱스 쿼리 성능
- [ ] **6.9** `WalkPatternRepositoryAdapter` — upsert, 집계 쿼리
- [ ] **6.10** Kafka 이벤트 발행 + 수신 확인
- [ ] **6.11** gRPC 서버 통합 테스트 (6 RPC)
- [ ] **6.12** gRPC client → identity-service 차단 조회 모킹 테스트

## 수락 기준

- [ ] 한 Dog당 동시 ACTIVE Walk 1개만
- [ ] CRON 60분 만료 동작 (시간 주입 가능 테스트)
- [ ] nearby에서 차단 양방향 필터 동작
- [ ] SOLO 타입은 nearby에서 제외
- [ ] gridDistance(0/1/2)만 반환, 정확한 거리(m) 노출 금지
- [ ] GPS 좌표가 DB/로그/응답에 없음
- [ ] Kafka `walk.expired` 이벤트 정상 발행
- [ ] `./gradlew :services:walks:test` 전체 통과
