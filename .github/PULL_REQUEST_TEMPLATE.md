## 개요

<!-- 이 PR이 무엇을 하는지 1-2문장으로 요약 -->


## 변경 유형

<!-- 해당하는 항목에 x 표시 -->
- [ ] feat: 새 기능
- [ ] fix: 버그 수정
- [ ] refactor: 리팩토링 (기능 변경 없음)
- [ ] test: 테스트 추가/수정
- [ ] chore: 빌드/설정/인프라
- [ ] docs: 문서 변경

## 구현 내용

### 변경된 서비스

<!-- 해당하는 서비스에 x 표시 -->
- [ ] api-gateway
- [ ] identity-service
- [ ] pet-profile-service
- [ ] walks-service
- [ ] social-service
- [ ] notification-service
- [ ] common/domain-common
- [ ] common/kafka-common
- [ ] proto 정의
- [ ] infra (docker-compose, observability)
- [ ] frontend (Expo)

### 상세 변경 사항

<!-- 구현한 내용을 구체적으로 기술 -->
-
-
-

### 새로 추가된 API / gRPC 메서드

<!-- 새로 추가된 엔드포인트가 있다면 기술. 없으면 "없음" -->

| 서비스 | Method | Path / RPC | 설명 |
|--------|--------|------------|------|
| | | | |

### DB 마이그레이션

<!-- Flyway 마이그레이션이 포함되어 있다면 기술. 없으면 "없음" -->
- [ ] 마이그레이션 파일 포함됨
- 파일명:
- 변경 내용:

## 관련 문서

### 기획/설계 문서 매핑

<!-- 이 PR이 어떤 문서의 어떤 항목을 구현하는지 명시 -->

| 문서 | 섹션/항목 | 구현 상태 |
|------|-----------|-----------|
| `plan-docs/ceo-plan.md` | | |
| `plan-docs/eng-review.md` | | |
| `plan-docs/backend-requirements.md` | | |
| `plan-docs/design-review.md` | | |
| `ai/decisions.md` | | |

### ADR 준수 확인

<!-- 이 PR이 영향을 받는 ADR에 x 표시 후 준수 여부 확인 -->
- [ ] ADR-005: GPS 좌표 미저장, gridCell만 사용
- [ ] ADR-006: 클린/헥사고날 아키텍처 (서비스 내부)
- [ ] ADR-011: 서비스 경계 (bounded context) 준수
- [ ] ADR-012: Cross-schema JOIN 금지, gRPC로만 조회
- [ ] ADR-013: 서비스간 동기 통신 gRPC
- [ ] ADR-015: 비동기 통신 Kafka
- [ ] ADR-016: Coroutine vs Virtual Thread 사용 기준
- [ ] ADR-017: JPA + Flyway + TSID

## 테스트

### 자동 테스트

<!-- 테스트 결과를 기록 -->
- [ ] Unit 테스트 통과
- [ ] Integration 테스트 통과 (Testcontainers)
- [ ] gRPC 테스트 통과
- 실행 명령어: `./gradlew :services:<name>:test`
- 새로 추가된 테스트 수:
- 커버리지 변화:

### 수동 검증

<!-- 수동으로 확인한 항목 -->
- [ ] `./gradlew projects` — 모듈 인식
- [ ] `./gradlew :services:<name>:compileKotlin` — 컴파일 성공
- [ ] `docker-compose up` — 인프라 기동
- [ ] gRPC 호출 테스트 (grpcurl 등)
- [ ] API Gateway REST 호출 테스트

### 코딩 규칙 체크 (`ai/conventions-backend.md`)

- [ ] domain/ 레이어에 Spring/JPA/gRPC import 없음
- [ ] N+1 방지: `@EntityGraph` 또는 `JOIN FETCH` 사용
- [ ] 에러 처리: 구체적 예외만, catch-all 없음
- [ ] KDoc: 포트 인터페이스, 도메인 메서드에 문서화
- [ ] `!!` (non-null assertion) 미사용

## 불변 규칙 위반 체크

<!-- 아래 항목은 위반 시 머지 불가 -->
- [ ] GPS 좌표가 DB/로그/응답에 저장되지 않음
- [ ] Cross-schema JOIN이 없음 (다른 서비스 데이터는 gRPC로만)
- [ ] 상호 인사(ACCEPTED) 전 메시지 전송 불가 (해당 시)
- [ ] 차단 유저 양방향 필터 적용 (해당 시)
- [ ] 비밀번호/PII가 로그에 노출되지 않음

## 스크린샷 / 증거

<!-- Grafana 트레이싱, API 응답, 테스트 결과 캡처 등 -->


## 리뷰 요청 사항

<!-- 리뷰어에게 특별히 봐달라고 할 부분 -->
-

## 체크리스트

- [ ] 커밋 메시지가 `<type>(<scope>): <한글 설명>` 형식
- [ ] 불필요한 `println`, `TODO`, 디버그 코드 제거
- [ ] `./gradlew build` 성공 (관련 서비스)
- [ ] 관련 문서 업데이트 (필요 시)
- [ ] PR 제목이 70자 이내
