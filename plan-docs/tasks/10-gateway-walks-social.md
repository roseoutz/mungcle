# 10. API Gateway — Walks + Social + Notifications BFF

브랜치: `feature/gateway-walks-social` | 선행: 04~08 | 예상 PR 사이즈: ~400줄

---

## gRPC Client Wrapper

- [ ] `WalksClient`, `SocialClient`, `NotificationClient` — gRPC stub 래핑

## REST 컨트롤러

### Walks
- [ ] `POST /api/walks/start` — GridCell 스냅(lat/lng) + walks.startWalk(gridCell)
- [ ] `POST /api/walks/:id/stop`
- [ ] `GET /api/walks/nearby` — **BFF aggregation** (identity 차단→walks nearby→pet-profile dogs→identity users, coroutine 병렬)
- [ ] `GET /api/walks/me/active`
- [ ] `GET /api/walk-patterns/nearby` — BFF aggregation

### Social
- [ ] `POST /api/greetings`, `POST /api/greetings/:id/respond`
- [ ] `GET /api/greetings`, `GET /api/greetings/:id` (+ pet-profile 카드 조합)
- [ ] `POST /api/greetings/:id/messages`, `GET /api/greetings/:id/messages`

### Notifications
- [ ] `GET /api/notifications`, `POST /api/notifications/:id/read`, `POST /api/notifications/read-all`

## DTO + Validation

- [ ] Walk/Greeting/Message/Notification 요청/응답 DTO
- [ ] `NearbyWalkCardResponse` (Dog + Owner + gridDistance 조합)
- [ ] `StartWalkRequest` (lat/lng → gateway에서 gridCell 변환)

## BFF Aggregation

- [ ] nearby 호출: `coroutineScope { async {} }` 로 pet-profile + identity 병렬
- [ ] GPS lat/lng → gridCell 변환은 gateway에서만. 내부 서비스에 lat/lng 전파 금지.

## 테스트

- [ ] Unit: GridCell 스냅 (gateway 측)
- [ ] Integration: MockMvc — walks start/stop/nearby, greetings 생성/응답/메시지
- [ ] BFF 병렬 호출 검증 (coroutine async)

## 수락 기준

- [ ] nearby BFF가 3+ gRPC 조합 후 단일 REST 응답
- [ ] GPS 좌표가 내부 서비스에 전달되지 않음 (gridCell만)
- [ ] 모든 REST 엔드포인트가 backend-requirements.md §6과 1:1 매핑
- [ ] `./gradlew :services:api-gateway:test` 통과
