# 01. Identity — Auth (가입/로그인/JWT)

브랜치: `feature/identity-auth` | 선행: 00 | 예상 PR 사이즈: ~400줄

---

## 도메인

- [ ] `User` 도메인 모델 (id, kakaoId?, email?, nickname, createdAt)
- [ ] 이메일 소문자 정규화 메서드
- [ ] 닉네임 검증 (2~16자, 한글/영문/숫자/언더스코어)
- [ ] 도메인 예외: `EmailTakenException`, `InvalidCredentialsException`

## 포트

- [ ] `RegisterEmailUseCase`, `LoginEmailUseCase`, `AuthenticateKakaoUseCase`
- [ ] `ValidateTokenUseCase`, `UpdatePushTokenUseCase`
- [ ] `UserRepositoryPort` (save, findByEmail, findByKakaoId, findById)
- [ ] `JwtPort` (generateToken, validateToken)
- [ ] `PasswordPort` (hash, verify)

## Application

- [ ] `RegisterEmailCommandHandler` — 이메일 중복 체크 + bcrypt + User 생성 + JWT 발급
- [ ] `LoginEmailCommandHandler` — 이메일 조회 + 비밀번호 검증 + JWT 발급
- [ ] `AuthenticateKakaoCommandHandler` — kakaoId upsert + JWT 발급
- [ ] `ValidateTokenQueryHandler` — JWT 파싱 → userId
- [ ] `UpdatePushTokenCommandHandler` — pushToken 저장

## Infrastructure

- [ ] `UserEntity` (JPA, @Tsid, schema=identity)
- [ ] `UserSpringDataRepository`, `UserRepositoryAdapter`, `UserMapper`
- [ ] `JwtAdapter` (jjwt), `BcryptPasswordAdapter` (Spring Security Crypto)
- [ ] `IdentityGrpcService` — Auth 관련 5 RPC 구현 (AuthenticateKakao, RegisterEmail, LoginEmail, ValidateToken, UpdatePushToken)
- [ ] `GrpcExceptionInterceptor` — 도메인 예외 → gRPC Status
- [ ] `IdentityConfig` — Bean 와이어링, `JwtConfig` — secret/expiration

## 테스트

- [ ] Unit: User 닉네임/이메일 검증, RegisterEmail(정상/중복), LoginEmail(정상/실패), JWT 생성+검증
- [ ] Integration: UserRepositoryAdapter CRUD (Testcontainers PostgreSQL)
- [ ] gRPC: InProcessServer로 Auth 5 RPC 테스트

## 수락 기준

- [ ] 이메일 가입 → 로그인 → JWT → ValidateToken 플로우 동작
- [ ] 카카오 동일 계정 재로그인 시 중복 User 없음
- [ ] 비밀번호가 응답/로그에 절대 노출 안 됨
- [ ] `./gradlew :services:identity:test` 통과
