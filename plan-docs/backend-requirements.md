# Backend Functional Requirements — 멍클 Phase 1 (MSA)

Derived from: `ceo-plan.md`, `eng-review.md`, `ai/decisions.md` (ADR-010~022), `ai/domain-model.md`
Scope: **Phase 1 MVP** (5개 기능). MSA 6개 서비스 기준.
Stack: Kotlin + Spring Boot 3.5 + JPA + gRPC + Kafka + Docker Compose

---

## 0. 공통 / Cross-cutting

### 0.1 인증 / 권한

- api-gateway가 JWT 검증 (Spring Security Filter). 내부 서비스는 gRPC metadata로 `userId` 전달받음.
- 토큰 payload: `{ sub: userId(TSID), iat, exp }`. 만료 7일. Refresh는 Phase 1 범위 외.
- 익명 접근 가능: `POST /api/auth/kakao`, `POST /api/auth/email/register`, `POST /api/auth/email/login`, `GET /api/health`.
- 내부 gRPC 서비스간 통신: 별도 인증 없음 (private network). gRPC metadata에 `x-user-id` 전달.

### 0.2 위치 / 프라이버시 (불변)

- **GPS 좌표를 DB에 저장/전송 금지.** 클라이언트가 lat/lng를 보내면 서버(api-gateway 또는 walks-service)가 즉시 200m `GridCell`로 스냅. 원본은 응답/로그/DB 어디에도 남기지 않는다.
- Grid snap: `floor(lat / 0.002)` + `floor(lng / 0.002)` → `"$latBucket:$lngBucket"`
- `nearby` 쿼리: 3x3 (9셀) 범위 조회 (약 600m 반경 근사).

### 0.3 표준 에러

- api-gateway REST 응답:
  ```json
  { "statusCode": 409, "code": "WALK_ALREADY_ACTIVE", "message": "..." }
  ```
- 도메인 에러 코드:
  - `AUTH_INVALID_CREDENTIALS` (401), `AUTH_TOKEN_EXPIRED` (401)
  - `FORBIDDEN_BLOCKED` (403), `NOT_FOUND` (404)
  - `GREETING_DUPLICATE` (409), `GREETING_EXPIRED` (410)
  - `WALK_ALREADY_ACTIVE` (409), `WALK_ALREADY_ENDED` (410)
  - `VALIDATION_FAILED` (400), `EMAIL_TAKEN` (409)
- gRPC 내부: `Status.NOT_FOUND`, `Status.ALREADY_EXISTS` 등 gRPC 표준 코드 매핑.

### 0.4 CRON / 스케줄 (ADR-022)

각 도메인 서비스가 자기 만료를 직접 처리:
- **walks-service:** `@Scheduled(fixedRate = 60_000)` → Walk 60분 만료 → `ENDED` → Kafka `walk.expired`
- **social-service:** `@Scheduled(fixedRate = 60_000)` → Greeting PENDING 5분 만료, ACCEPTED 30분 만료 → Kafka `greeting.expired`

### 0.5 차단 필터 (불변)

- 모든 `nearby`, `walk-patterns`, `greetings` 쿼리는 **양방향 block**을 제외.
- 호출 흐름: 요청 서비스 → identity-service `getBlockedUserIds(userId)` gRPC → 결과로 필터링.

### 0.6 이미지 업로드

- Supabase Storage. 버킷: `dog-photos` (public read), `vaccination-photos` (authenticated read).
- 업로드: pre-signed URL 발급 → 클라이언트 직접 PUT.
- api-gateway에 `POST /api/uploads/sign` → `{ uploadUrl, path }`.
- 파일 크기: 5MB. MIME: `image/jpeg`, `image/png`, `image/webp`.

### 0.7 TSID 설정

- 모든 엔티티 ID: `@Tsid Long`. `hypersistence-utils-hibernate-63`.
- Node ID: 서비스 이름 해시 기반 자동 할당 (0~1023).
- 서비스별 `TSID_NODE` 환경변수 또는 서비스 이름 → hashCode 매핑.

---

## 1. identity-service

### 소유 엔티티
User, Block, Report

### 스키마
`identity`

### 1.1 Auth

**기능:**
- 카카오 OAuth 로그인 (한국 시장 최적).
- 이메일/비밀번호 fallback (카카오 심사 중 대응).
- 최초 로그인 시 User 자동 생성(upsert).

**gRPC 메서드 (identity.v1.IdentityService):**

| Method | 설명 | 호출자 |
|--------|------|--------|
| `AuthenticateKakao` | 카카오 토큰 → JWT + User | api-gateway |
| `RegisterEmail` | 이메일 가입 → JWT + User | api-gateway |
| `LoginEmail` | 이메일 로그인 → JWT + User | api-gateway |
| `ValidateToken` | JWT 검증 → userId | api-gateway |
| `UpdatePushToken` | FCM/Expo push token 등록 | api-gateway |

**규칙:**
- 비밀번호 bcrypt(cost 12).
- 카카오 동일 `kakaoId` 재로그인 시 기존 유저 반환.
- 이메일 소문자 정규화.

**수락 기준:**
- [ ] 카카오 동일 계정 재로그인 시 User 중복 생성 안 됨.
- [ ] 비밀번호는 응답/로그에 절대 노출되지 않음.

### 1.2 Users

**gRPC 메서드:**

| Method | 설명 | 호출자 |
|--------|------|--------|
| `GetUser` | userId로 유저 정보 조회 | api-gateway, 다른 서비스 |
| `UpdateUser` | 닉네임/동네/프로필 수정 | api-gateway |
| `DeleteUser` | 소프트 삭제 + 개인정보 익명화 | api-gateway |
| `GetUsersByIds` | bulk 조회 | walks, social (nearby 카드용) |

**규칙:**
- 닉네임 2~16자, 한글/영문/숫자/언더스코어.
- 탈퇴 후 해당 유저의 데이터는 다른 서비스에서도 제외 (Kafka `user.deleted` 이벤트 or lazy check).

### 1.3 Blocks

**gRPC 메서드:**

| Method | 설명 | 호출자 |
|--------|------|--------|
| `CreateBlock` | 차단 | api-gateway |
| `DeleteBlock` | 차단 해제 | api-gateway |
| `ListBlocks` | 내 차단 목록 | api-gateway |
| `GetBlockedUserIds` | userId의 양방향 차단 ID 목록 | walks, social |
| `IsBlocked` | 두 유저간 차단 여부 | social |

**규칙:**
- 자기 자신 차단 금지 (400).
- 차단 즉시 진행 중 Greeting은 social-service에 Kafka 이벤트로 통지 → EXPIRED.
- 동일 차단 중복 생성 시 idempotent (200).

### 1.4 Reports

**gRPC 메서드:**

| Method | 설명 | 호출자 |
|--------|------|--------|
| `CreateReport` | 신고 | api-gateway |

**규칙:**
- 같은 reporter→reported 중복 허용 (n회 수집).
- 3회 이상 누적 → `User.flaggedForReview = true`.
- `reason` 1~500자.

---

## 2. pet-profile-service

### 소유 엔티티
Dog

### 스키마
`pet_profile`

### gRPC 메서드 (pet_profile.v1.PetProfileService):

| Method | 설명 | 호출자 |
|--------|------|--------|
| `CreateDog` | 등록 (photo path 참조) | api-gateway |
| `GetDog` | 상세 | api-gateway, social |
| `GetDogsByOwner` | 내 개 목록 | api-gateway |
| `GetDogsByIds` | bulk 조회 (nearby 카드용) | walks, social |
| `UpdateDog` | 수정 | api-gateway |
| `DeleteDog` | 삭제 | api-gateway |

### 규칙
- 한 유저 최대 5마리.
- 크기: `SMALL | MEDIUM | LARGE`.
- 성향 태그: `활발 / 조용 / 친절 / 호기심 / 온순 / 주의` 중 1~3개.
- 사회성 등급: 정수 1~5 (Phase 1은 자기 신고).
- 예방접종: 파일 존재 여부만으로 `등록됨/미등록` 결정. 검증 로직 없음.
- 타인 소유 Dog 수정/삭제 시 `PERMISSION_DENIED`.
- 삭제 시 해당 Dog의 진행 중 Walk가 있으면 `FAILED_PRECONDITION`.

### 수락 기준
- [ ] 6마리 등록 시도 시 거절.
- [ ] 타인 소유 Dog 수정 시도 `PERMISSION_DENIED`.
- [ ] bulk 조회 `GetDogsByIds`에 없는 ID 포함 시 해당 항목만 제외 (에러 아님).

---

## 3. walks-service

### 소유 엔티티
Walk, WalkPattern

### 스키마
`walks`

### gRPC 메서드 (walks.v1.WalksService):

| Method | 설명 | 호출자 |
|--------|------|--------|
| `StartWalk` | `{ dogId, type, lat, lng }` → gridCell 스냅 후 저장 | api-gateway |
| `StopWalk` | 본인만 | api-gateway |
| `GetNearbyWalks` | 3x3 grid 내 OPEN Walk. 차단 제외. 본인 제외 | api-gateway |
| `GetMyActiveWalks` | 내 진행 중 Walk 목록 | api-gateway |
| `GetWalkGridCell` | walkId → gridCell 조회 (위치 공유용) | social |
| `GetNearbyPatterns` | 현재 시간 ± 1시간, 3x3 grid, 빈도 top 10 | api-gateway |

### 규칙
- `lat/lng`는 서버에서 즉시 grid 변환 후 폐기.
- `nearby` 응답에는 `gridDistance` (0/1/2)만. 정확한 거리(m) 노출 금지.
- SOLO 타입은 `nearby`에서 제외.
- 한 Dog당 동시 ACTIVE Walk 1개.
- 60분 자동 만료 → Kafka `walk.expired`.
- **nearby 조합:** walks-service가 Walk 목록 반환 → api-gateway가 pet-profile `GetDogsByIds` + identity `GetUsersByIds`로 카드 정보 조합 (BFF 패턴). 또는 walks-service 내에서 gRPC로 조합 후 반환.

### 수락 기준
- [ ] Active Walk 있는 Dog에 `StartWalk` 시 `ALREADY_EXISTS`.
- [ ] 60분 CRON 만료 동작 (시간 주입 테스트).
- [ ] nearby에서 차단 양방향 필터 검증.
- [ ] Grid snap 경계값 테스트 (적도, 음수, 0/0).

---

## 4. social-service

### 소유 엔티티
Greeting, Message

### 스키마
`social`

### gRPC 메서드 (social.v1.SocialService):

| Method | 설명 | 호출자 |
|--------|------|--------|
| `CreateGreeting` | 인사 전송 | api-gateway |
| `RespondGreeting` | 수락/거절 | api-gateway |
| `GetGreeting` | 상세 (양쪽 카드 + 메시지) | api-gateway |
| `ListGreetings` | 내가 보낸/받은 목록 (status 필터) | api-gateway |
| `SendMessage` | 간단 메시지 (ACCEPTED + 30분 내) | api-gateway |
| `ListMessages` | 메시지 목록 | api-gateway |

### 규칙 (불변)
- **상태 전이:** `PENDING → ACCEPTED | EXPIRED` (단방향).
- `PENDING` 5분 만료 / `ACCEPTED` 30분 후 위치공유/메시지 만료.
- Unique: `(senderUserId, receiverUserId, receiverWalkId)`.
- 차단 유저간 생성 자체가 `PERMISSION_DENIED` (identity `IsBlocked` 확인).
- 메시지 길이 1~140자.
- ACCEPTED 만료 후 메시지 시도 시 `FAILED_PRECONDITION`.

### Kafka 이벤트
- `greeting.created` → notification (FCM 발송)
- `greeting.accepted` → notification
- `greeting.expired` → notification
- `message.sent` → notification

### 수락 기준
- [ ] 동일 sender→receiver 중복 인사 시 `ALREADY_EXISTS`.
- [ ] 5분/30분 CRON 만료 동작.
- [ ] 30분 만료 후 메시지 전송 시 `FAILED_PRECONDITION`.
- [ ] 차단 관계 양방향 생성 차단.

---

## 5. notification-service

### 소유 엔티티
Notification

### 스키마
`notification`

### gRPC 메서드 (notification.v1.NotificationService):

| Method | 설명 | 호출자 |
|--------|------|--------|
| `ListNotifications` | 페이지네이션 | api-gateway |
| `MarkRead` | 읽음 | api-gateway |
| `MarkAllRead` | 모두 읽음 | api-gateway |

### Kafka Consumer
- `greeting.created` → identity `GetUser`로 push token 조회 → FCM 발송 + 인앱 알림 저장
- `greeting.accepted` → FCM + 인앱 알림
- `greeting.expired` → 인앱 알림 (FCM 선택)
- `message.sent` → FCM + 인앱 알림
- `walk.expired` → 인앱 알림 (FCM 선택)

### 규칙
- FCM 발송 실패 시 인앱 알림 테이블에 fallback 저장.
- 알림 타입: `GREETING_RECEIVED`, `GREETING_ACCEPTED`, `MESSAGE_RECEIVED`, `WALK_EXPIRED`.

---

## 6. api-gateway

### 역할
- Expo 앱의 유일한 진입점. REST/HTTP 노출.
- JWT 검증 (Spring Security).
- 여러 gRPC 서비스 호출 결과를 조합해서 REST 응답 (BFF aggregation).

### REST 엔드포인트 (클라이언트용)

| Method | Path | 대상 서비스 | 설명 |
|--------|------|-------------|------|
| POST | `/api/auth/kakao` | identity | 카카오 로그인 |
| POST | `/api/auth/email/register` | identity | 이메일 가입 |
| POST | `/api/auth/email/login` | identity | 이메일 로그인 |
| POST | `/api/auth/push-token` | identity | push token 등록 |
| GET | `/api/users/me` | identity + pet-profile | 내 정보 + 개 수 |
| PATCH | `/api/users/me` | identity | 닉네임/동네/프로필 |
| DELETE | `/api/users/me` | identity | 탈퇴 |
| POST | `/api/dogs` | pet-profile | 개 등록 |
| GET | `/api/dogs` | pet-profile | 내 개 목록 |
| GET | `/api/dogs/:id` | pet-profile | 개 상세 |
| PATCH | `/api/dogs/:id` | pet-profile | 개 수정 |
| DELETE | `/api/dogs/:id` | pet-profile | 개 삭제 |
| POST | `/api/walks/start` | walks | 산책 시작 |
| POST | `/api/walks/:id/stop` | walks | 산책 종료 |
| GET | `/api/walks/nearby` | walks + pet-profile + identity | nearby 카드 (BFF 조합) |
| GET | `/api/walks/me/active` | walks | 내 진행 중 Walk |
| GET | `/api/walk-patterns/nearby` | walks + pet-profile | 시간대 패턴 (BFF 조합) |
| POST | `/api/greetings` | social | 인사 전송 |
| POST | `/api/greetings/:id/respond` | social | 인사 응답 |
| GET | `/api/greetings` | social | 인사 목록 |
| GET | `/api/greetings/:id` | social + pet-profile | 인사 상세 + 카드 |
| POST | `/api/greetings/:id/messages` | social | 메시지 전송 |
| GET | `/api/greetings/:id/messages` | social | 메시지 목록 |
| POST | `/api/blocks` | identity | 차단 |
| DELETE | `/api/blocks/:userId` | identity | 차단 해제 |
| GET | `/api/blocks` | identity | 차단 목록 |
| POST | `/api/reports` | identity | 신고 |
| GET | `/api/notifications` | notification | 알림 목록 |
| POST | `/api/notifications/:id/read` | notification | 읽음 |
| POST | `/api/notifications/read-all` | notification | 모두 읽음 |
| POST | `/api/uploads/sign` | (직접) | Supabase pre-signed URL |
| GET | `/api/health` | (직접) | 헬스 체크 |

### BFF Aggregation 예시

`GET /api/walks/nearby` 처리 흐름:
```
1. gateway → identity.GetBlockedUserIds(myUserId)     // 차단 목록
2. gateway → walks.GetNearbyWalks(gridCell, blockedIds) // Walk 목록
3. Walk에서 dogId, userId 추출
4. gateway → pet-profile.GetDogsByIds(dogIds)          // 개 정보
5. gateway → identity.GetUsersByIds(userIds)           // 닉네임
6. 조합 → REST 응답
```

gRPC 호출 2~5는 `coroutineScope { async {} }` 로 병렬 가능 (2는 선행 필요).

---

## 7. 비기능 요건 (NFR)

- **성능:** `GET /api/walks/nearby` p95 < 300ms (gRPC 다중 호출 포함, 로컬 기준).
- **쿼리:** N+1 금지, JPA `@EntityGraph` / `JOIN FETCH`.
- **보안:** JWT secret env. bcrypt. Rate limit `/api/auth/*` 10req/min/IP (Spring Boot `@RateLimiter` 또는 bucket4j).
- **로깅:** GPS/비밀번호/이메일 금지. userId + requestId + traceId.
- **관측성:** OpenTelemetry 자동 계측. gRPC/Kafka 트레이스 전파.
- **테스트:** 서비스별 Unit + Integration (Testcontainers). Grid snap 경계값 필수.

---

## 8. 구현 병렬화 (eng-review.md 참고)

| Lane | 범위 | 선행 조건 |
|------|------|-----------|
| A | scaffolding + proto + docker-compose + common | 먼저 |
| B | identity-service | A |
| C | pet-profile-service | B (User 필요) |
| D | walks-service | B |
| E | social-service | B + D |
| F | notification-service (Kafka consumer) | B |
| G | api-gateway (REST BFF) | B~F proto 확정 |
| H | Expo app | G REST 안정화 후 |

---

## 9. Out of Scope (Phase 1)

- WebSocket / 실시간 (polling + FCM으로 충분)
- Service Mesh (Istio 등)
- CI/CD 파이프라인 (수동 배포)
- 캐싱 (Redis, MVP 불필요 — Kafka에만 사용)
- 풀 DM / 피드 / 배지 / 커머스
- 산책 로그/기록, 산책 친구 리카프, 산책 예고, 장소 리뷰 (Phase 2)
- 상호 평가 기반 사회성 재계산 (Phase 2)

---

## 10. Open Questions

1. **카카오 OAuth 심사 상태** — 현재 상태 확인. 심사 중이면 이메일 fallback 우선.
2. **동네 프리셋 데이터** — 초기 시딩 대상 공원/동 목록 1곳 선정.
3. **FCM 경로** — Expo push service (Expo가 FCM 대행) vs FCM 직접. Expo 경유가 MVP에 빠름.
4. **nearby BFF vs 서비스 내부 조합** — api-gateway에서 조합할지, walks-service가 내부적으로 pet-profile/identity를 호출할지. BFF 패턴(gateway 조합) 기본이지만 성능 이슈 시 재검토.
5. **차단 이벤트 전파** — 차단 시 social-service의 진행 중 Greeting을 EXPIRED로 만드는 방법. Kafka `block.created` 이벤트 vs 동기 gRPC.
