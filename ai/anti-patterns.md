# 안티패턴 — 하지 말 것

AI가 반복하기 쉬운 실수와 프로젝트에서 금지하는 패턴.

## Kotlin 공통

- `!!` (non-null assertion) 프로덕션 코드에서 금지. `?: throw` 또는 `?.let {}` 사용.
- Java `Optional` 사용 금지. Kotlin nullable `?`로 대체.
- 의존성 추가 전 반드시 사용자에게 확인. 임의로 의존성 추가 금지.
- `companion object`에 비즈니스 로직 금지. factory 메서드나 상수만.
- `println` 디버깅 코드 커밋 금지.
- `lateinit` 남발 금지. DI 주입 외에는 사용 금지.

## Backend (Kotlin + Spring Boot + JPA + gRPC)

- gRPC 서버 구현에 비즈니스 로직 넣지 않는다. UseCase에서만.
- catch-all (`catch (e: Exception)`) 금지. 구체적 예외만 잡는다.
- GPS 좌표를 DB에 직접 저장하지 않는다. 200m 그리드 ID만.
- JPA `findAll`/`findMany` 시 `@EntityGraph` 또는 `JOIN FETCH` 없이 호출하지 않는다 (N+1).
- 이미 적용된 Flyway 마이그레이션 파일을 수정하지 않는다. 새 마이그레이션을 만든다.
- Cross-schema JOIN 금지. 다른 서비스 데이터는 gRPC로만 조회.
- domain/ 레이어에서 Spring, JPA, gRPC, Kafka를 import하지 않는다.
- `@Transactional`을 gRPC 서버 구현이나 도메인 서비스에 넣지 않는다. application 레이어만.
- 서비스간 직접 메서드 호출 금지. gRPC 또는 Kafka로만.

## Frontend (Expo / React Native)

- 인라인 스타일 사용 금지. `StyleSheet.create()` 사용.
- 색상/간격/폰트 하드코딩 금지. `constants/theme.ts` 참조.
- `features/A/`에서 `features/B/`를 직접 import하지 않는다.
- 외부 상태 관리 라이브러리 (Redux, Zustand, MobX) 추가 금지 (MVP 단계).
- 빈 화면(empty state)을 처리하지 않는 컴포넌트 금지.
- 터치 타겟 44x44px 미만 금지.

## Supabase

- Storage 버킷의 공개 범위를 필요 이상으로 열지 않는다.
- 서비스 키(service_role)를 프론트엔드 코드에서 사용하지 않는다.

## 일반

- 테스트 없이 기능 커밋 금지.
- 한 커밋에 여러 논리적 변경 금지.
- plan-docs/의 결정 사항과 모순되는 구현 금지. 의문이 있으면 먼저 질문.
