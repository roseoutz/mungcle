# 02. Pet Profile Service

Lane C | 브랜치: `feature/pet-profile-service` | 선행: 01-identity-service
참조: `plan-docs/backend-requirements.md` §2, `proto/pet_profile/v1/pet_profile.proto`

---

## 1. 프로젝트 구조 세팅

- [ ] **1.1** 클린 아키텍처 패키지 생성 (domain/application/infrastructure/config)
- [ ] **1.2** `common:grpc-client` 의존성 추가
- [ ] **1.3** TSID 자동 설정 Bean
- [ ] **1.4** Flyway V1 마이그레이션 동작 확인

## 2. 도메인 모델

- [ ] **2.1** `Dog` 도메인 모델
  - id, ownerId, name, breed, size(SMALL/MEDIUM/LARGE), temperaments(1~3개), sociability(1~5), photoPath?, vaccinationPhotoPath?, deletedAt?, createdAt
  - 성향 태그 enum: `활발, 조용, 친절, 호기심, 온순, 주의`
  - `isVaccinationRegistered()` — vaccinationPhotoPath 존재 여부만
  - 검증: 이름 1~50자, 견종 1~100자, 성향 1~3개, 사회성 1~5
- [ ] **2.2** `DogSize` enum (SMALL, MEDIUM, LARGE)
- [ ] **2.3** `Temperament` enum (6종)
- [ ] **2.4** 도메인 예외
  - `DogNotFoundException`, `DogNotOwnedException`, `DogLimitExceededException`, `DogHasActiveWalkException`

## 3. 포트 정의

### 인바운드 포트

- [ ] **3.1** `CreateDogUseCase` — 개 등록 (유저당 최대 5마리 검증)
- [ ] **3.2** `GetDogUseCase` — 개 상세 조회
- [ ] **3.3** `GetDogsByOwnerUseCase` — 내 개 목록
- [ ] **3.4** `GetDogsByIdsUseCase` — bulk 조회 (다른 서비스용)
- [ ] **3.5** `UpdateDogUseCase` — 개 정보 수정 (소유권 확인)
- [ ] **3.6** `DeleteDogUseCase` — 개 삭제 (active walk 체크)

### 아웃바운드 포트

- [ ] **3.7** `DogRepositoryPort` — save, findById, findByOwnerId, findByIds, countByOwnerId, softDelete

## 4. Application 레이어

- [ ] **4.1** `CreateDogCommandHandler`
  - ownerId로 현재 등록 수 조회 → 5마리 초과 시 예외
  - Dog 생성 + 저장
- [ ] **4.2** `GetDogQueryHandler` — findById, 없으면 DogNotFoundException
- [ ] **4.3** `GetDogsByOwnerQueryHandler` — findByOwnerId (deletedAt IS NULL)
- [ ] **4.4** `GetDogsByIdsQueryHandler` — bulk 조회, 없는 ID는 무시 (에러 아님)
- [ ] **4.5** `UpdateDogCommandHandler`
  - findById → 소유권 확인 (ownerId != requesterId → DogNotOwnedException)
  - 부분 수정 (null이 아닌 필드만)
- [ ] **4.6** `DeleteDogCommandHandler`
  - findById → 소유권 확인
  - active walk 존재 여부 확인 (walks-service gRPC? 아니면 로컬?)
  - → Phase 1에서는 softDelete만. active walk 체크는 gateway에서 사전 확인.
  - softDelete 처리

## 5. Infrastructure 레이어

### JPA + Repository

- [ ] **5.1** `DogEntity` (JPA @Entity, @Tsid, schema=pet_profile, TEXT[] temperaments)
- [ ] **5.2** `DogSpringDataRepository`
- [ ] **5.3** `DogRepositoryAdapter` (DogRepositoryPort 구현)
- [ ] **5.4** `DogMapper` (Domain ↔ Entity)

### gRPC Server

- [ ] **5.5** `PetProfileGrpcService` — proto의 `PetProfileService` 구현 (6 RPC)
- [ ] **5.6** gRPC 예외 처리 인터셉터

### Config

- [ ] **5.7** `PetProfileConfig` — Bean 와이어링

## 6. 테스트

### Unit

- [ ] **6.1** `Dog` 도메인 모델 테스트 — 검증 규칙, isVaccinationRegistered
- [ ] **6.2** `CreateDogCommandHandler` — 정상 등록, 5마리 초과, 성향 0개/4개 거부
- [ ] **6.3** `UpdateDogCommandHandler` — 정상 수정, 소유권 위반
- [ ] **6.4** `DeleteDogCommandHandler` — 정상 삭제, 소유권 위반

### Integration (Testcontainers)

- [ ] **6.5** `DogRepositoryAdapter` — CRUD + softDelete + findByOwnerId + countByOwnerId
- [ ] **6.6** gRPC 서버 통합 테스트
  - CreateDog, GetDog, GetDogsByOwner, GetDogsByIds, UpdateDog, DeleteDog

## 수락 기준

- [ ] 유저당 최대 5마리 등록 제한
- [ ] 타인 소유 Dog 수정/삭제 시 PERMISSION_DENIED
- [ ] 예방접종 "등록됨" 플래그는 파일 존재 여부만으로 결정
- [ ] bulk 조회에서 없는 ID는 에러 없이 해당 항목만 제외
- [ ] `./gradlew :services:pet-profile:test` 전체 통과
