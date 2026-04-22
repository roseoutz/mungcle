# 09. API Gateway — Auth + Users + Dogs

브랜치: `feature/gateway-auth-dogs` | 선행: 01, 02, 03 | 예상 PR 사이즈: ~450줄

---

## Security

- [ ] `JwtAuthenticationFilter` — Bearer → identity.validateToken gRPC → SecurityContext
- [ ] `SecurityConfig` — 공개 경로 (/api/auth/*, /api/health)
- [ ] `@AuthUser` 커스텀 어노테이션
- [ ] 인증 실패 401 표준 에러 포맷

## gRPC Client Wrapper

- [ ] `IdentityClient`, `PetProfileClient` — gRPC stub 래핑
- [ ] gRPC StatusException → REST 예외 변환 유틸

## REST 컨트롤러

- [ ] `POST /api/auth/kakao`, `POST /api/auth/email/register`, `POST /api/auth/email/login`
- [ ] `POST /api/auth/push-token`
- [ ] `GET /api/users/me` (identity + pet-profile 개 수 조합)
- [ ] `PATCH /api/users/me`, `DELETE /api/users/me`
- [ ] `POST /api/dogs`, `GET /api/dogs`, `GET /api/dogs/:id`
- [ ] `PATCH /api/dogs/:id`, `DELETE /api/dogs/:id`
- [ ] `POST /api/blocks`, `DELETE /api/blocks/:userId`, `GET /api/blocks`
- [ ] `POST /api/reports`
- [ ] `POST /api/uploads/sign` (Supabase pre-signed URL)
- [ ] `GET /api/health`

## DTO + Validation

- [ ] Auth/User/Dog/Block/Report 요청/응답 DTO
- [ ] Bean Validation (@NotBlank, @Size, @Min 등)
- [ ] `GlobalExceptionHandler` (gRPC→HTTP 변환 + validation 실패)

## 테스트

- [ ] Unit: JWT 필터 (유효/만료/없음), gRPC→HTTP 변환
- [ ] Integration: MockMvc — Auth 가입/로그인, Dogs CRUD, Blocks (gRPC 모킹)
- [ ] 비인증 접근 → 401

## 수락 기준

- [ ] 인증 플로우 (가입→로그인→JWT→인증 요청) 동작
- [ ] Bean Validation 실패 → 400 필드별 에러
- [ ] `./gradlew :services:api-gateway:test` 통과
