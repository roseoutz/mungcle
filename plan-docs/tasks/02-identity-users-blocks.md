# 02. Identity — Users + Blocks + Reports

브랜치: `feature/identity-users-blocks` | 선행: 01 | 예상 PR 사이즈: ~400줄

---

## 도메인

- [ ] `User` 확장 — neighborhood?, profilePhotoPath?, flaggedForReview, deletedAt?
- [ ] `User.softDelete()` — 이메일/카카오ID 익명화
- [ ] `Block` 도메인 모델 (blockerId, blockedId) + 자기 차단 검증
- [ ] `Report` 도메인 모델 (reporterId, reportedId, reason 1~500자)
- [ ] 도메인 예외: `UserNotFoundException`, `BlockSelfException`, `ReportSelfException`

## 포트

- [ ] `GetUserUseCase`, `GetUsersByIdsUseCase`, `UpdateUserUseCase`, `DeleteUserUseCase`
- [ ] `CreateBlockUseCase`, `DeleteBlockUseCase`, `ListBlocksUseCase`
- [ ] `GetBlockedUserIdsUseCase`, `IsBlockedUseCase`
- [ ] `CreateReportUseCase`
- [ ] `BlockRepositoryPort`, `ReportRepositoryPort`

## Application

- [ ] `GetUser/GetUsersByIds QueryHandler`
- [ ] `UpdateUserCommandHandler` — 부분 수정
- [ ] `DeleteUserCommandHandler` — softDelete + 개인정보 익명화
- [ ] `CreateBlockCommandHandler` — idempotent (중복 시 200)
- [ ] `DeleteBlockCommandHandler`
- [ ] `ListBlocksQueryHandler`, `GetBlockedUserIdsQueryHandler` (양방향)
- [ ] `IsBlockedQueryHandler`
- [ ] `CreateReportCommandHandler` — 3회 누적 → flaggedForReview

## Infrastructure

- [ ] `BlockEntity`, `ReportEntity` (JPA, @Tsid)
- [ ] `BlockSpringDataRepository`, `ReportSpringDataRepository`
- [ ] `BlockRepositoryAdapter`, `ReportRepositoryAdapter`
- [ ] `IdentityGrpcService` 확장 — 나머지 11 RPC 추가

## 테스트

- [ ] Unit: softDelete 익명화, 양방향 차단 조회, 3회 신고 flagged
- [ ] Integration: Block unique constraint, Report count 집계 (Testcontainers)
- [ ] gRPC: GetUser, CreateBlock, GetBlockedUserIds, IsBlocked, CreateReport

## 수락 기준

- [ ] 탈퇴 후 이메일/카카오ID 무효화
- [ ] 차단 양방향 조회 동작 (A→B 차단 시 양쪽 모두 목록에)
- [ ] 중복 차단 시 에러 아닌 200
- [ ] 3회 신고 누적 → flaggedForReview=true
- [ ] `./gradlew :services:identity:test` 통과
