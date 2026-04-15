# 태스크 총괄

## 원칙

- **1 태스크 파일 = 1 PR** (feature 브랜치 → develop)
- PR 사이즈: 200~500줄 변경 목표. 한 번에 리뷰 가능한 단위.
- 수직 슬라이스: 도메인 → 포트 → UseCase → JPA → gRPC → 테스트를 한 PR에.

## 구현 순서 (의존성)

```
Phase 0: 00 (스카폴딩) ✅
     ↓
Phase 1: 01 (identity: auth)
     ↓
Phase 2: 02 (identity: users+blocks+reports)
     ↓ ─────────────────┬──────────────────┐
Phase 3: 03 (pet-profile) │ 04 (walks-core) │ 07 (notification)
                          ↓                  │
Phase 4:          05 (walks-patterns+cron)   │
                          ↓                  │
Phase 5: 06 (social: greetings)              │
     ↓                                       │
Phase 6: 08 (social: messages+cron)          │
     ↓ ─────────────────────────────────────┘
Phase 7: 09 (gateway: auth+users+dogs)
     ↓
Phase 8: 10 (gateway: walks+social+notifications)
     ↓
Phase 9: 11 (frontend: 초기화+인증+온보딩)
     ↓
Phase 10: 12 (frontend: 홈+산책+인사)
     ↓
Phase 11: 13 (frontend: 알림+설정+마무리)
```

## 태스크 목록

| # | 파일 | 서비스 | 범위 | 상태 | 선행 |
|---|------|--------|------|------|------|
| 00 | [00-foundation.md](00-foundation.md) | 전체 | 스카폴딩 | ✅ | — |
| 01 | [01-identity-auth.md](01-identity-auth.md) | identity | Auth (가입/로그인/JWT) | 대기 | 00 |
| 02 | [02-identity-users-blocks.md](02-identity-users-blocks.md) | identity | Users + Blocks + Reports | 대기 | 01 |
| 03 | [03-pet-profile.md](03-pet-profile.md) | pet-profile | Dogs CRUD 전체 | 대기 | 02 |
| 04 | [04-walks-core.md](04-walks-core.md) | walks | 산책 시작/종료/nearby | 대기 | 02 |
| 05 | [05-walks-patterns-cron.md](05-walks-patterns-cron.md) | walks | 시간대 패턴 + CRON 만료 + Kafka | 대기 | 04 |
| 06 | [06-social-greetings.md](06-social-greetings.md) | social | 인사 생성/응답 | 대기 | 04 |
| 07 | [07-notification.md](07-notification.md) | notification | Kafka Consumer + FCM + 알림함 | 대기 | 02 |
| 08 | [08-social-messages-cron.md](08-social-messages-cron.md) | social | 메시지 + CRON 만료 + Kafka | 대기 | 06 |
| 09 | [09-gateway-auth-dogs.md](09-gateway-auth-dogs.md) | api-gateway | JWT + Auth/Users/Dogs REST | 대기 | 01~03 |
| 10 | [10-gateway-walks-social.md](10-gateway-walks-social.md) | api-gateway | Walks/Social/Notifications BFF | 대기 | 04~08 |
| 11 | [11-frontend-init-auth.md](11-frontend-init-auth.md) | frontend | Expo 초기화 + 디자인 + 인증 + 온보딩 | 대기 | 09 |
| 12 | [12-frontend-home-walks.md](12-frontend-home-walks.md) | frontend | 홈 + 산책 + 인사 | 대기 | 10, 11 |
| 13 | [13-frontend-notifications-settings.md](13-frontend-notifications-settings.md) | frontend | 알림 + 설정 + 마무리 | 대기 | 12 |

## 병렬 실행 가능 그룹

- **Phase 3:** 03 + 04 + 07 (pet-profile, walks-core, notification) 병렬
- **Phase 7~8:** 09 + 10은 순차지만 11과는 병렬 가능 (REST 계약 확정 후)
