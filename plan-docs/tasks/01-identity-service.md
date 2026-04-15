# 01. Identity Service

Lane B | 브랜치: `feature/identity-service` | 선행: 00-foundation
참조: `plan-docs/backend-requirements.md` §1, `ai/domain-model.md`, `proto/identity/v1/identity.proto`

---

## 1. 프로젝트 구조 세팅

- [ ] **1.1** 서비스 내부 클린 아키텍처 패키지 생성
  ```
  services/identity/src/main/kotlin/com/mungcle/identity/
  ├── domain/model/
  ├── domain/port/in/
  ├── domain/port/out/
  ├── domain/exception/
  ├── domain/service/
  ├── application/command/
  ├── application/query/
  ├── application/dto/
  ├── infrastructure/persistence/
  ├── infrastructure/grpc/server/
  ├── config/
  ```
- [ ] **1.2** `build.gradle.kts`에 `common:grpc-client` 의존성 추가
- [ ] **1.3** TSID 자동 설정 Bean (`TsidConfig.nodeIdFor("identity")`)
- [ ] **1.4** Flyway 마이그레이션 정상 동작 확인 (`docker compose up postgres` → `./gradlew :services:identity:bootRun`)

## 2. 도메인 모델

- [ ] **2.1** `User` 도메인 모델
  - id, kakaoId?, email?, nickname, neighborhood?, pushToken?, flaggedForReview, deletedAt?, createdAt
  - 닉네임 검증 (2~16자, 한글/영문/숫자/언더스코어)
  - 이메일 소문자 정규화 메서드
  - 소프트 삭제 메서드 (개인정보 익명화)
- [ ] **2.2** `Block` 도메인 모델
  - id, blockerId, blockedId, createdAt
  - 자기 자신 차단 검증
- [ ] **2.3** `Report` 도메인 모델
  - id, reporterId, reportedId, reason(1~500자), createdAt
  - 자기 자신 신고 검증
- [ ] **2.4** 도메인 예외 정의
  - `EmailTakenException`, `InvalidCredentialsException`, `UserNotFoundException`
  - `BlockSelfException`, `ReportSelfException`

## 3. 포트 정의

### 인바운드 포트 (UseCase)

- [ ] **3.1** `AuthenticateKakaoUseCase` — 카카오 토큰으로 로그인/가입 (upsert)
- [ ] **3.2** `RegisterEmailUseCase` — 이메일 가입
- [ ] **3.3** `LoginEmailUseCase` — 이메일 로그인
- [ ] **3.4** `ValidateTokenUseCase` — JWT 검증 → userId
- [ ] **3.5** `UpdatePushTokenUseCase` — FCM push token 등록/갱신
- [ ] **3.6** `GetUserUseCase` — userId로 유저 조회
- [ ] **3.7** `GetUsersByIdsUseCase` — bulk 조회
- [ ] **3.8** `UpdateUserUseCase` — 닉네임/동네/프로필 수정
- [ ] **3.9** `DeleteUserUseCase` — 소프트 삭제 + 개인정보 익명화
- [ ] **3.10** `CreateBlockUseCase` — 차단 (idempotent)
- [ ] **3.11** `DeleteBlockUseCase` — 차단 해제
- [ ] **3.12** `ListBlocksUseCase` — 내 차단 목록
- [ ] **3.13** `GetBlockedUserIdsUseCase` — 양방향 차단 ID 목록 (다른 서비스가 호출)
- [ ] **3.14** `IsBlockedUseCase` — 두 유저간 차단 여부
- [ ] **3.15** `CreateReportUseCase` — 신고 (3회 누적 시 flaggedForReview)

### 아웃바운드 포트

- [ ] **3.16** `UserRepositoryPort` — save, findById, findByEmail, findByKakaoId, findByIds, softDelete
- [ ] **3.17** `BlockRepositoryPort` — save, delete, findByBlockerId, findBlockedUserIds(양방향), isBlocked
- [ ] **3.18** `ReportRepositoryPort` — save, countByReportedId
- [ ] **3.19** `JwtPort` — generateToken, validateToken
- [ ] **3.20** `PasswordPort` — hash, verify

## 4. Application 레이어 (UseCase 구현)

- [ ] **4.1** `AuthenticateKakaoCommandHandler`
  - 카카오 API로 사용자 정보 조회 (또는 토큰 검증)
  - kakaoId로 User 조회 → 있으면 반환, 없으면 생성
  - JWT 발급
- [ ] **4.2** `RegisterEmailCommandHandler`
  - 이메일 중복 체크 (소문자 정규화 후)
  - 비밀번호 bcrypt 해시
  - User 생성 + JWT 발급
- [ ] **4.3** `LoginEmailCommandHandler`
  - 이메일로 User 조회
  - 비밀번호 검증
  - JWT 발급
- [ ] **4.4** `ValidateTokenQueryHandler` — JWT 파싱, 만료 확인, userId 반환
- [ ] **4.5** `UpdatePushTokenCommandHandler` — User.pushToken 업데이트
- [ ] **4.6** `GetUserQueryHandler` / `GetUsersByIdsQueryHandler`
- [ ] **4.7** `UpdateUserCommandHandler` — 닉네임/동네/프로필 부분 수정
- [ ] **4.8** `DeleteUserCommandHandler` — 소프트 삭제, 이메일/카카오ID 무효화
- [ ] **4.9** `CreateBlockCommandHandler` — 중복 시 idempotent (에러 아닌 200)
- [ ] **4.10** `DeleteBlockCommandHandler`
- [ ] **4.11** `ListBlocksQueryHandler`
- [ ] **4.12** `GetBlockedUserIdsQueryHandler` — 양방향 조회
- [ ] **4.13** `IsBlockedQueryHandler`
- [ ] **4.14** `CreateReportCommandHandler` — 저장 + 3회 누적 체크 → flaggedForReview

## 5. Infrastructure 레이어

### JPA Entity + Repository

- [ ] **5.1** `UserEntity` (JPA @Entity, @Tsid, schema=identity)
- [ ] **5.2** `BlockEntity` (JPA @Entity, unique constraint)
- [ ] **5.3** `ReportEntity` (JPA @Entity)
- [ ] **5.4** `UserSpringDataRepository` (Spring Data JPA interface)
- [ ] **5.5** `BlockSpringDataRepository`
- [ ] **5.6** `ReportSpringDataRepository`
- [ ] **5.7** `UserRepositoryAdapter` (UserRepositoryPort 구현)
- [ ] **5.8** `BlockRepositoryAdapter` (BlockRepositoryPort 구현)
- [ ] **5.9** `ReportRepositoryAdapter` (ReportRepositoryPort 구현)
- [ ] **5.10** `UserMapper`, `BlockMapper`, `ReportMapper` (Domain ↔ Entity)

### JWT + Password

- [ ] **5.11** `JwtAdapter` (JwtPort 구현, jjwt 라이브러리)
- [ ] **5.12** `BcryptPasswordAdapter` (PasswordPort 구현, Spring Security Crypto)

### gRPC Server

- [ ] **5.13** `IdentityGrpcService` — proto의 `IdentityService` 구현
  - 16개 RPC 메서드 → 각 UseCase 위임
  - 도메인 예외 → gRPC Status 변환 인터셉터
- [ ] **5.14** gRPC 예외 처리 인터셉터 (`GrpcExceptionInterceptor`)

### Config

- [ ] **5.15** `IdentityConfig` — Bean 와이어링 (포트 → 어댑터 바인딩)
- [ ] **5.16** `JwtConfig` — JWT secret, expiration 설정 바인딩

## 6. 테스트

### Unit 테스트

- [ ] **6.1** `User` 도메인 모델 테스트
  - 닉네임 검증 (길이, 허용 문자)
  - 이메일 정규화
  - 소프트 삭제 후 개인정보 익명화
- [ ] **6.2** `RegisterEmailCommandHandler` 테스트
  - 정상 가입
  - 이메일 중복 → `EmailTakenException`
  - 비밀번호 해시 확인
- [ ] **6.3** `LoginEmailCommandHandler` 테스트
  - 정상 로그인 → JWT 반환
  - 잘못된 비밀번호 → `InvalidCredentialsException`
  - 존재하지 않는 이메일 → `InvalidCredentialsException` (정보 노출 방지)
- [ ] **6.4** `CreateBlockCommandHandler` 테스트
  - 정상 차단
  - 자기 자신 차단 → `BlockSelfException`
  - 중복 차단 → idempotent (에러 없음)
- [ ] **6.5** `GetBlockedUserIdsQueryHandler` 테스트
  - A→B 차단 시 A 조회에도 B 포함, B 조회에도 A 포함 (양방향)
- [ ] **6.6** `CreateReportCommandHandler` 테스트
  - 정상 신고
  - 3회 누적 → User.flaggedForReview = true

### Integration 테스트 (Testcontainers)

- [ ] **6.7** `UserRepositoryAdapter` 통합 테스트
  - CRUD + softDelete + findByEmail/findByKakaoId
- [ ] **6.8** `BlockRepositoryAdapter` 통합 테스트
  - save, findBlockedUserIds(양방향), isBlocked, unique constraint
- [ ] **6.9** `JwtAdapter` 테스트
  - 토큰 생성 → 검증 → userId 추출
  - 만료 토큰 → 실패
- [ ] **6.10** gRPC 서버 통합 테스트 (InProcessServer)
  - AuthenticateKakao, RegisterEmail, LoginEmail, GetUser
  - CreateBlock, GetBlockedUserIds, IsBlocked
  - CreateReport

## 수락 기준

- [ ] 카카오 동일 계정 재로그인 시 User 중복 생성 안 됨
- [ ] 이메일 소문자 정규화 (`Test@X.com` == `test@x.com`)
- [ ] 비밀번호가 응답/로그에 절대 노출되지 않음
- [ ] 탈퇴한 유저의 개인정보(이메일, 카카오ID) 무효화됨
- [ ] 차단 양방향 조회 동작
- [ ] 3회 신고 누적 시 flaggedForReview 마킹
- [ ] 모든 gRPC 메서드가 proto 계약대로 동작
- [ ] `./gradlew :services:identity:test` 전체 통과
