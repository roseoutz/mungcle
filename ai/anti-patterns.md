# 안티패턴 — 하지 말 것

AI가 반복하기 쉬운 실수와 프로젝트에서 금지하는 패턴.

## TypeScript 공통

- `any` 타입 사용 금지. `unknown`으로 대체하거나 구체적 타입 정의.
- 의존성 추가 전 반드시 사용자에게 확인. 임의로 `npm install` 금지.
- default export 사용 금지. named export만 사용.
- 유틸리티 파일 생성 금지. 헬퍼는 사용하는 곳 근처에 배치.
- `console.log` 디버깅 코드 커밋 금지.

## Backend (NestJS + Prisma)

- Controller에 비즈니스 로직 넣지 않는다.
- catch-all (`catch (error)`, `catch (e: any)`) 금지. 구체적 예외만 잡는다.
- GPS 좌표를 DB에 직접 저장하지 않는다. 200m 그리드 ID만.
- Prisma `findMany` 시 `include` 없이 호출하지 않는다 (N+1).
- 이미 적용된 마이그레이션 파일을 수정하지 않는다. 새 마이그레이션을 만든다.
- 모듈 간 순환 의존을 만들지 않는다.
- domain/ 레이어에서 NestJS, Prisma를 import하지 않는다.
- `@Transactional` 또는 트랜잭션 로직을 Controller에 넣지 않는다.

## Frontend (Expo / React Native)

- 인라인 스타일 사용 금지. `StyleSheet.create()` 사용.
- 색상/간격/폰트 하드코딩 금지. `constants/theme.ts` 참조.
- `features/A/`에서 `features/B/`를 직접 import하지 않는다.
- 외부 상태 관리 라이브러리 (Redux, Zustand, MobX) 추가 금지 (MVP 단계).
- 빈 화면(empty state)을 처리하지 않는 컴포넌트 금지.
- 터치 타겟 44x44px 미만 금지.

## Supabase

- Supabase 테이블 생성 시 RLS(Row Level Security) 정책 반드시 설정.
- Storage 버킷의 공개 범위를 필요 이상으로 열지 않는다.
- 서비스 키(service_role)를 프론트엔드 코드에서 사용하지 않는다.

## 일반

- 테스트 없이 기능 커밋 금지.
- 한 커밋에 여러 논리적 변경 금지.
- plan-docs/의 결정 사항과 모순되는 구현 금지. 의문이 있으면 먼저 질문.
