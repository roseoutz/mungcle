# 04. Walks — 산책 시작/종료/nearby

브랜치: `feature/walks-core` | 선행: 02 | 예상 PR 사이즈: ~450줄

---

## 도메인

- [ ] `Walk` 모델 (id, dogId, userId, type, gridCell, status, startedAt, endsAt)
- [ ] `WalkType` enum (OPEN/SOLO), `WalkStatus` enum (ACTIVE/ENDED)
- [ ] `isExpired(now)`, `isOpen()`, `end(now)`
- [ ] 예외: `WalkAlreadyActiveException`, `WalkNotFoundException`, `WalkAlreadyEndedException`

## 포트 + Application

- [ ] `StartWalkUseCase` — dogId 중복 ACTIVE 체크, Walk 생성 (endsAt = now+60분)
- [ ] `StopWalkUseCase` — 본인 확인, end(now)
- [ ] `GetNearbyWalksUseCase` — 3x3 grid, OPEN만, 차단 제외, 본인 제외, gridDistance
- [ ] `GetMyActiveWalksUseCase`
- [ ] `GetWalkGridCellUseCase` — walkId → gridCell (위치 공유용)
- [ ] `WalkRepositoryPort`, `IdentityPort` (gRPC client: getBlockedUserIds)

## Infrastructure

- [ ] `WalkEntity` (JPA, @Tsid, 복합 인덱스, schema=walks)
- [ ] `WalkSpringDataRepository` (gridCell+status 쿼리)
- [ ] `WalkRepositoryAdapter`, `WalkMapper`
- [ ] `IdentityGrpcClient` (IdentityPort 구현)
- [ ] `WalksGrpcService` — StartWalk, StopWalk, GetNearbyWalks, GetMyActiveWalks, GetWalkGridCell (5 RPC)

## 테스트

- [ ] Unit: GridCell 경계값 (적도/음수/0,0), adjacentCells, gridDistance
- [ ] Unit: StartWalk 중복 거부, StopWalk 타인/이미종료 거부
- [ ] Unit: nearby SOLO 제외, 본인 제외, 차단 제외
- [ ] Integration: WalkRepository 복합 인덱스 쿼리
- [ ] gRPC: 5 RPC 테스트

## 수락 기준

- [ ] 한 Dog당 동시 ACTIVE Walk 1개만
- [ ] nearby에서 SOLO 제외, 차단 양방향 필터
- [ ] gridDistance(0/1/2)만 반환, 정확 거리 노출 금지
- [ ] GPS 좌표가 DB/로그에 없음
- [ ] `./gradlew :services:walks:test` 통과
