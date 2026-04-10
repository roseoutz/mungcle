# 테스트 전략

## 원칙

- TDD 권장. 강제는 아님.
- 테스트 없는 기능은 미완성으로 간주.
- 상세 시나리오: `plan-docs/test-plan.md` 참고.

## 프레임워크

| 영역 | 도구 |
|------|------|
| Backend unit | JUnit 5 + MockK |
| Backend integration | Testcontainers (PostgreSQL, Kafka) |
| Backend gRPC | grpc-testing (InProcessServer) |
| Frontend component | Jest + React Native Testing Library |
| E2E | 추후 결정 (MVP에서는 수동 + gstack qa) |

## 테스트 우선순위

1. **grid snap 경계값** (unit) — 200m 그리드 핵심 로직
2. **인사 상태 전이** (unit) — pending → accepted/expired
3. **차단 필터링** (integration) — nearby에서 차단 유저 제외
4. **핵심 플로우** (E2E) — 온보딩 → 산책 → 인사 → 메시지

## Backend 테스트 규칙

- 파일명: `<Name>Test.kt` (unit), `<Name>IntegrationTest.kt` (integration)
- Unit 테스트: MockK로 포트 모킹. 도메인 로직 검증.
- Integration 테스트: `@SpringBootTest` + Testcontainers. 실제 DB/Kafka.
- gRPC 테스트: `grpc-testing`의 InProcessServer로 서비스 단독 테스트.
- 에러 케이스 반드시 포함: NOT_FOUND, ALREADY_EXISTS, PERMISSION_DENIED 등.

## Frontend 테스트 규칙

- 파일명: `<Component>.test.tsx`
- 렌더링 + 사용자 인터랙션 검증
- 상태별 렌더링 확인: loading, empty, error, success

## 테스트 실행

```bash
# Backend 전체
./gradlew test

# 개별 서비스
./gradlew :services:identity:test
./gradlew :services:walks:test
./gradlew :common:domain-common:test

# Frontend (Expo 설정 후)
cd frontend && npm test
```
