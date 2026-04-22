# 태스크 분해

## 원칙

- 하나의 태스크 = 하나의 커밋 단위
- 태스크 완료 = 테스트 통과 + 커밋 가능 상태
- 기능(Feature) = PR 단위. 여러 태스크로 구성.

## 분해 기준

### Feature (PR 단위)
- 하나의 모듈 또는 하나의 화면
- 예: "개 프로필 CRUD", "산책 상태 토글", "인사하기 플로우"

### Task (커밋 단위)
- 한 가지 논리적 변경
- 예: "Dog JPA Entity + Flyway 마이그레이션 추가", "CreateDogCommandHandler 구현", "DogCard 컴포넌트 생성"

## Phase 1 분해 (참고)

상세 병렬화 전략: `plan-docs/eng-review.md` 참고.

```
Lane A: Foundation
  ├── Proto 정의 + common 패키지 + Docker Compose
  ├── identity-service (Auth + Users + Blocks + Reports)
  └── Flyway 마이그레이션

Lane B: pet-profile-service (A 완료 후)
  └── Dogs CRUD + 사진 업로드

Lane C: walks-service (A 완료 후)
  ├── 산책 상태 토글 + nearby + 자동 만료
  └── WalkPatterns (시간대 집계)

Lane D: social-service (A+C 완료 후)
  ├── Greetings (인사 + 응답)
  └── Messages (간단 메시지)

Lane E: Expo App (A와 병렬)
  ├── Expo 프로젝트 초기화 + expo-router
  ├── 온보딩 화면 (로그인 → 개 등록 → 동네 설정)
  ├── 홈 화면 (탐색 + 산책 토글)
  ├── 알림 화면 (인사 목록)
  └── 설정 화면
```

## OMC team 사용 시

`/oh-my-claudecode:team`으로 병렬 실행할 때, 각 lane을 별도 에이전트에 할당.
Lane 간 의존성 순서를 반드시 지킬 것 (A → B,C,E → D).
