# 06. API Gateway

Lane G | 브랜치: `feature/api-gateway` | 선행: 01~05 (모든 도메인 서비스 gRPC 구현)
참조: `plan-docs/backend-requirements.md` §6, `services/api-gateway/`

---

## 1. 프로젝트 구조 세팅

- [ ] **1.1** 패키지 구조 생성
  ```
  services/api-gateway/src/main/kotlin/com/mungcle/gateway/
  ├── controller/       # REST 컨트롤러
  ├── dto/              # 요청/응답 DTO
  ├── security/         # JWT 필터, SecurityConfig
  ├── grpc/             # gRPC client wrapper
  ├── exception/        # REST 예외 핸들러
  └── config/
  ```
- [ ] **1.2** `common:grpc-client` + `common:domain-common` 의존성 확인
- [ ] **1.3** 5개 gRPC client channel 설정 확인 (application.yml)

## 2. Security (JWT)

- [ ] **2.1** `JwtAuthenticationFilter` — Bearer 토큰 추출 → identity.validateToken gRPC → SecurityContext 설정
- [ ] **2.2** `SecurityConfig` — 공개 경로 설정 (/api/auth/*, /api/health), 나머지 인증 필수
- [ ] **2.3** `@AuthUser` 커스텀 어노테이션 — 컨트롤러에서 userId 주입
- [ ] **2.4** 인증 실패 응답 (401, 표준 에러 포맷)

## 3. gRPC Client Wrapper

- [ ] **3.1** `IdentityClient` — identity-service gRPC stub 래핑
- [ ] **3.2** `PetProfileClient` — pet-profile-service gRPC stub 래핑
- [ ] **3.3** `WalksClient` — walks-service gRPC stub 래핑
- [ ] **3.4** `SocialClient` — social-service gRPC stub 래핑
- [ ] **3.5** `NotificationClient` — notification-service gRPC stub 래핑
- [ ] **3.6** gRPC StatusException → REST 예외 변환 공통 유틸

## 4. REST 컨트롤러

### Auth

- [ ] **4.1** `POST /api/auth/kakao` → identity.authenticateKakao
- [ ] **4.2** `POST /api/auth/email/register` → identity.registerEmail
- [ ] **4.3** `POST /api/auth/email/login` → identity.loginEmail
- [ ] **4.4** `POST /api/auth/push-token` → identity.updatePushToken

### Users

- [ ] **4.5** `GET /api/users/me` → identity.getUser + pet-profile.getDogsByOwner (개 수)
- [ ] **4.6** `PATCH /api/users/me` → identity.updateUser
- [ ] **4.7** `DELETE /api/users/me` → identity.deleteUser

### Dogs

- [ ] **4.8** `POST /api/dogs` → pet-profile.createDog
- [ ] **4.9** `GET /api/dogs` → pet-profile.getDogsByOwner
- [ ] **4.10** `GET /api/dogs/:id` → pet-profile.getDog
- [ ] **4.11** `PATCH /api/dogs/:id` → pet-profile.updateDog
- [ ] **4.12** `DELETE /api/dogs/:id` → pet-profile.deleteDog

### Walks

- [ ] **4.13** `POST /api/walks/start` → GridCell 스냅(lat/lng) + walks.startWalk(gridCell)
- [ ] **4.14** `POST /api/walks/:id/stop` → walks.stopWalk
- [ ] **4.15** `GET /api/walks/nearby` — **BFF aggregation**
  1. GridCell 스냅(lat/lng)
  2. identity.getBlockedUserIds
  3. walks.getNearbyWalks(gridCell, blockedIds)
  4. pet-profile.getDogsByIds (Walk의 dogId들)
  5. identity.getUsersByIds (Walk의 userId들)
  6. 조합 → NearbyWalkCard 응답
  - gRPC 호출 3~5 `coroutineScope { async {} }` 병렬화
- [ ] **4.16** `GET /api/walks/me/active` → walks.getMyActiveWalks

### Walk Patterns

- [ ] **4.17** `GET /api/walk-patterns/nearby` — **BFF aggregation**
  1. GridCell 스냅 + identity.getBlockedUserIds
  2. walks.getNearbyPatterns(gridCell, blockedIds)
  3. pet-profile.getDogsByIds
  4. 조합

### Greetings

- [ ] **4.18** `POST /api/greetings` → social.createGreeting
- [ ] **4.19** `POST /api/greetings/:id/respond` → social.respondGreeting
- [ ] **4.20** `GET /api/greetings` → social.listGreetings
- [ ] **4.21** `GET /api/greetings/:id` → social.getGreeting + pet-profile.getDogsByIds (카드 정보)
- [ ] **4.22** `POST /api/greetings/:id/messages` → social.sendMessage
- [ ] **4.23** `GET /api/greetings/:id/messages` → social.listMessages

### Blocks / Reports

- [ ] **4.24** `POST /api/blocks` → identity.createBlock
- [ ] **4.25** `DELETE /api/blocks/:userId` → identity.deleteBlock
- [ ] **4.26** `GET /api/blocks` → identity.listBlocks
- [ ] **4.27** `POST /api/reports` → identity.createReport

### Notifications

- [ ] **4.28** `GET /api/notifications` → notification.listNotifications
- [ ] **4.29** `POST /api/notifications/:id/read` → notification.markRead
- [ ] **4.30** `POST /api/notifications/read-all` → notification.markAllRead

### Uploads / Health

- [ ] **4.31** `POST /api/uploads/sign` — Supabase pre-signed URL 발급
- [ ] **4.32** `GET /api/health` — 서비스 상태 + 마지막 health 체크

## 5. 요청/응답 DTO

- [ ] **5.1** Auth DTO — KakaoLoginRequest, EmailRegisterRequest, EmailLoginRequest, AuthResponse, PushTokenRequest
- [ ] **5.2** User DTO — UserResponse, UpdateUserRequest
- [ ] **5.3** Dog DTO — CreateDogRequest, UpdateDogRequest, DogResponse
- [ ] **5.4** Walk DTO — StartWalkRequest(lat/lng), WalkResponse, NearbyWalkCardResponse
- [ ] **5.5** Greeting DTO — CreateGreetingRequest, RespondRequest, GreetingResponse, GreetingDetailResponse
- [ ] **5.6** Message DTO — SendMessageRequest, MessageResponse
- [ ] **5.7** Notification DTO — NotificationResponse
- [ ] **5.8** Common DTO — ErrorResponse, PageResponse
- [ ] **5.9** Bean Validation 어노테이션 (@NotBlank, @Size, @Min, @Max 등)

## 6. 예외 처리

- [ ] **6.1** `GrpcStatusExceptionHandler` — gRPC StatusException → HTTP 응답 매핑
- [ ] **6.2** `GlobalExceptionHandler` — @RestControllerAdvice, 표준 에러 포맷
- [ ] **6.3** Validation 실패 → 400 VALIDATION_FAILED

## 7. 테스트

### Unit

- [ ] **7.1** JWT 필터 테스트 — 유효한 토큰, 만료 토큰, 토큰 없음
- [ ] **7.2** GridCell 스냅 로직 (gateway에서의 변환)
- [ ] **7.3** gRPC 예외 → HTTP 변환 매핑

### Integration

- [ ] **7.4** 각 REST 엔드포인트 MockMvc 테스트 (gRPC client 모킹)
  - Auth: 가입, 로그인
  - Dogs: CRUD
  - Walks: start, stop, nearby (BFF aggregation)
  - Greetings: 생성, 응답, 메시지
  - Blocks/Reports
  - Notifications
- [ ] **7.5** BFF aggregation 병렬 호출 테스트 (coroutine async)
- [ ] **7.6** 인증 필요 경로 비인증 접근 → 401

## 수락 기준

- [ ] 모든 REST 엔드포인트가 backend-requirements.md §6 테이블과 1:1 매핑
- [ ] nearby BFF가 3+ gRPC 호출을 병렬로 조합
- [ ] GPS 좌표는 gateway에서 gridCell로 스냅 후 내부 서비스에 전달 (lat/lng 전파 금지)
- [ ] 인증 실패 시 401, 표준 에러 포맷
- [ ] Bean Validation 실패 시 400, 필드별 에러 메시지
- [ ] `./gradlew :services:api-gateway:test` 전체 통과
