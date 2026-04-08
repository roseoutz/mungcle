---
globs: ["**/*.spec.ts", "**/*.test.ts", "**/*.test.tsx"]
---

# 테스트 규칙

- 테스트 없는 기능 커밋 금지.
- 에러 케이스 반드시 포함 (404, 409, 403 등).
- 프론트 컴포넌트: loading, empty, error, success 4가지 상태 테스트.
- grid snap 함수: 경계값(적도, 음수, 0/0) 필수 포함.
- 테스트 실행: `cd backend && npm test` / `cd frontend && npm test`
- 상세 시나리오: `plan-docs/test-plan.md` 참고.
