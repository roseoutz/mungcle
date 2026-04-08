---
globs: ["backend/**/*.ts"]
---

# Backend 규칙

- 클린 아키텍처: domain/ → application/ → infrastructure/ → presentation/
- domain/ 레이어에서 NestJS, Prisma import 금지.
- Controller에 비즈니스 로직 금지. Service/UseCase에서만.
- catch-all 금지. 구체적 예외만 잡는다.
- `findMany` 시 `include` 필수 (N+1 방지).
- 상세: `ai/conventions-backend.md` 참고.
