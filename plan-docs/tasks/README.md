# 태스크 총괄

## 구현 순서 (Lane 의존성)

```
Lane A: 00-foundation (스카폴딩)                    ✅ 완료
     ↓
Lane B: 01-identity-service                         ← 다음
     ↓              ↓              ↓
Lane C:          02-pet-profile  Lane D: 03-walks   Lane F: 05-notification
                      ↓              ↓
                 Lane E: 04-social (pet-profile + walks 완료 후)
                                     ↓
                 Lane G: 06-api-gateway (모든 서비스 gRPC 구현 후)
                                     ↓
                 Lane H: 07-frontend-app (gateway REST 안정화 후)
```

## 태스크 목록

| # | 파일 | Lane | 서비스 | 상태 | 선행 |
|---|------|------|--------|------|------|
| 00 | [00-foundation.md](00-foundation.md) | A | 전체 | ✅ 완료 | — |
| 01 | [01-identity-service.md](01-identity-service.md) | B | identity | 대기 | 00 |
| 02 | [02-pet-profile-service.md](02-pet-profile-service.md) | C | pet-profile | 대기 | 01 |
| 03 | [03-walks-service.md](03-walks-service.md) | D | walks | 대기 | 01 |
| 04 | [04-social-service.md](04-social-service.md) | E | social | 대기 | 01, 03 |
| 05 | [05-notification-service.md](05-notification-service.md) | F | notification | 대기 | 01 |
| 06 | [06-api-gateway.md](06-api-gateway.md) | G | api-gateway | 대기 | 01~05 |
| 07 | [07-frontend-app.md](07-frontend-app.md) | H | frontend | 대기 | 06 |

## 병렬 실행 가능 그룹

- **Phase 1:** 01 (identity) — 단독, 먼저
- **Phase 2:** 02 (pet-profile) + 03 (walks) + 05 (notification) — 01 완료 후 병렬
- **Phase 3:** 04 (social) — 03 완료 후
- **Phase 4:** 06 (api-gateway) — 02~05 gRPC 구현 완료 후
- **Phase 5:** 07 (frontend) — 06 REST API 안정화 후

## 브랜치 규칙

각 태스크는 `feature/<service-name>` 브랜치에서 작업 → develop PR.
태스크 내부 세부 항목은 하나의 feature 브랜치에서 순차 커밋.
