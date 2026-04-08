# 테스트 전략

## 원칙

- TDD 권장. 강제는 아님.
- 테스트 없는 기능은 미완성으로 간주.
- 상세 시나리오: `plan-docs/test-plan.md` 참고.

## 프레임워크

| 영역 | 도구 |
|------|------|
| Backend unit/integration | Jest + Supertest |
| Frontend component | Jest + React Native Testing Library |
| E2E | 추후 결정 (MVP에서는 수동 + gstack qa) |

## 테스트 우선순위

1. **grid snap 경계값** (unit) — 200m 그리드 핵심 로직
2. **인사 상태 전이** (unit) — pending → accepted/expired
3. **차단 필터링** (integration) — nearby에서 차단 유저 제외
4. **핵심 플로우** (E2E) — 온보딩 → 산책 → 인사 → 메시지

## Backend 테스트 규칙

- 파일명: `<module>.service.spec.ts`, `<module>.controller.spec.ts`
- Service 테스트: Prisma를 모킹. 비즈니스 로직 검증.
- Controller 테스트: Supertest로 HTTP 요청/응답 검증.
- 에러 케이스 반드시 포함: 404, 409, 403 등.

## Frontend 테스트 규칙

- 파일명: `<Component>.test.tsx`
- 렌더링 + 사용자 인터랙션 검증
- 상태별 렌더링 확인: loading, empty, error, success

## 테스트 실행

```bash
# Backend
cd backend && npm test

# Frontend (Expo 설정 후)
cd frontend && npm test
```
