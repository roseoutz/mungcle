---
globs: ["services/**/*.kt", "common/**/*.kt"]
---

# Backend 규칙

- 클린 아키텍처: domain/ → application/ → infrastructure/ (각 MSA 서비스 내부 동일 구조)
- domain/ 레이어에서 Spring, JPA, gRPC, Kafka import 금지.
- gRPC 서버 구현(infrastructure/grpc/server/)에 비즈니스 로직 금지. UseCase에서만.
- catch-all 금지. 구체적 예외만 잡는다.
- JPA `findAll`/`findMany` 시 `@EntityGraph` 또는 `JOIN FETCH` 필수 (N+1 방지).
- Cross-schema JOIN 금지. 다른 서비스 데이터는 gRPC로 조회.
- 상세: `ai/conventions-backend.md` 참고.
