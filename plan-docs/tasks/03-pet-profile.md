# 03. Pet Profile — Dogs CRUD

브랜치: `feature/pet-profile-service` | 선행: 02 | 예상 PR 사이즈: ~350줄

---

## 도메인

- [ ] `Dog` 모델 (id, ownerId, name, breed, size, temperaments 1~3개, sociability 1~5, photoPath?, vaccinationPhotoPath?)
- [ ] `DogSize` enum, `Temperament` enum (활발/조용/친절/호기심/온순/주의)
- [ ] `isVaccinationRegistered()` — 파일 존재 여부만
- [ ] 예외: `DogNotFoundException`, `DogNotOwnedException`, `DogLimitExceededException`

## 포트 + Application

- [ ] `CreateDogUseCase` — 유저당 최대 5마리 체크
- [ ] `GetDogUseCase`, `GetDogsByOwnerUseCase`, `GetDogsByIdsUseCase` (bulk)
- [ ] `UpdateDogUseCase` — 소유권 확인 + 부분 수정
- [ ] `DeleteDogUseCase` — 소유권 확인 + softDelete
- [ ] `DogRepositoryPort` (save, findById, findByOwnerId, findByIds, countByOwnerId)

## Infrastructure

- [ ] `DogEntity` (JPA, @Tsid, TEXT[] temperaments, schema=pet_profile)
- [ ] `DogSpringDataRepository`, `DogRepositoryAdapter`, `DogMapper`
- [ ] `PetProfileGrpcService` — 6 RPC 전체 구현
- [ ] gRPC 예외 인터셉터

## 테스트

- [ ] Unit: 5마리 제한, 소유권 위반, 성향 0/4개 거부, vaccination 판정
- [ ] Integration: DogRepositoryAdapter CRUD + countByOwnerId
- [ ] gRPC: 6 RPC 전체

## 수락 기준

- [ ] 유저당 5마리 제한 동작
- [ ] 타인 Dog 수정/삭제 → PERMISSION_DENIED
- [ ] bulk 조회에서 없는 ID는 무시 (에러 없음)
- [ ] `./gradlew :services:pet-profile:test` 통과
