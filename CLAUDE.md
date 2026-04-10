# CLAUDE.md

반려동물 신뢰 기반 산책 커뮤니티 앱 "멍클(Mungcle)" 개발.
상대 개의 성향/안전성을 미리 확인하고, 자연스럽게 산책에 합류하는 경험.

## 문서 우선순위

충돌 시: `plan-docs/` > `CLAUDE.md` > `ai/` > 코드 내 주석

| 디렉토리 | 역할 | 내용 |
|----------|------|------|
| `plan-docs/` | **무엇을** 만드는지 | 설계, 기능, 스코프, 리뷰 결과 |
| `ai/` | **어떻게** 만드는지 | 코딩 규칙, 워크플로우, 테스트, 도메인 모델 |

### ai/ 문서 목록

| 문서 | 내용 | 언제 읽나 |
|------|------|----------|
| `conventions-backend.md` | Kotlin+Spring Boot+JPA+gRPC 코딩 규칙 | 백엔드 코드 작성 시 |
| `conventions-frontend.md` | Expo/RN 코딩 규칙 | 프론트엔드 코드 작성 시 |
| `workflow.md` | 작업 흐름, gstack+OMC 연동 | 작업 시작 시 |
| `testing.md` | 테스트 전략, 프레임워크 | 테스트 작성 시 |
| `git-convention.md` | 커밋, 브랜치 규칙 | 커밋/PR 시 |
| `code-review.md` | 리뷰 체크리스트 | PR 전 자가 검토 시 |
| `domain-model.md` | 엔티티, 관계, 불변 규칙 | 데이터 모델 작업 시 |
| `task-decomposition.md` | 태스크 분해, 병렬화 | 구현 계획 시 |
| `anti-patterns.md` | 하지 말 것 목록 | 항상 (코드 작성 시) |
| `decisions.md` | 아키텍처 결정 기록 (ADR) | 설계 변경 검토 시 |
| `error-handling.md` | 에러 구조, 로깅 규칙 | 에러 처리 구현 시 |
| `performance.md` | 성능 예산, DB 쿼리 규칙 | API/쿼리 구현 시 |
| `gotchas.md` | AI가 자주 틀리는 것 | 항상 (실수 방지) |
| `code-examples.md` | 코드 패턴 템플릿 | 새 모듈/컴포넌트 생성 시 |

### .claude/rules/ (파일 유형별 자동 적용)

| 파일 | 적용 대상 |
|------|----------|
| `backend.md` | `services/**/*.kt`, `common/**/*.kt` |
| `frontend.md` | `frontend/**/*.tsx`, `frontend/**/*.ts` |
| `prisma.md` | `**/db/migration/**`, `**/persistence/**/*.kt`, `**/*.sql` |
| `tests.md` | `**/*.spec.ts`, `**/*.test.ts`, `**/*.test.tsx` |

## 기술 스택

**MSA 6개 서비스:** Kotlin + Spring Boot 3.5 + JPA + gRPC + Kafka
**프론트엔드:** Expo (React Native)
**인프라:** Supabase (PostgreSQL + Storage) + FCM + Docker Compose
**관측성:** OpenTelemetry + Grafana + Tempo + Prometheus

상세: `plan-docs/eng-review.md`, `ai/decisions.md` (ADR-010~022)

## 코드 규칙

- 코드: 영어. 커밋 메시지/주석/문서: 한국어.
- 커밋: `<type>(<scope>): <한글 설명>` (feat, fix, refactor, test, chore, docs)
- 커밋 전 반드시 테스트 실행하여 정상 동작 확인. 테스트 실패 시 커밋 금지.
- 브랜치: `feature/<name>`, `fix/<name>`
- TDD 권장. 강제는 아님.
- GPS 좌표 저장 금지 — 200m 그리드 스냅만 저장.
- N+1 방지: Prisma `include` 사용.

## 작업 판단 흐름

사용자 요청을 받으면, 아래 순서대로 판단한다. 직접 답변하지 말고 해당 도구를 실행할 것.

```
사용자 요청
  │
  ├─ 기획/문서/리뷰/QA/배포인가? ──→ gstack 스킬 사용
  │
  ├─ 코드 구현/디버깅/병렬 실행인가? ──→ OMC 스킬 또는 에이전트 사용
  │
  ├─ 작은 수정 (한 파일, 명확한 변경)? ──→ 직접 수정 (도구 불필요)
  │
  └─ 판단 불가? ──→ 사용자에게 질문
```

실행 전 반드시 해당 스킬의 "필수 참조" 문서를 읽을 것. 경로는 모두 `plan-docs/` 하위.

## gstack — 기획, 리뷰, 문서, QA, 배포

```bash
test -d ~/.claude/skills/gstack/bin && echo "GSTACK_OK" || echo "GSTACK_MISSING"
```
GSTACK_MISSING이면 작업 중단. 설치: `git clone --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack && cd ~/.claude/skills/gstack && ./setup --team`

**이런 요청이 오면 gstack을 쓴다:**

| 사용자가 이렇게 말하면 | 스킬 | 필수 참조 |
|----------------------|------|----------|
| "아이디어 있는데", "이거 어때" | office-hours | `design-doc.md` |
| "스코프 리뷰해줘", "기능 추가할까" | plan-ceo-review | `ceo-plan.md` |
| "아키텍처 봐줘", "설계 검토" | plan-eng-review | `eng-review.md`, `ceo-plan.md` |
| "디자인 리뷰해줘", "UI 봐줘" | plan-design-review | `design-review.md` |
| "디자인 시스템 만들어줘" | design-consultation | `design-review.md` |
| "배포해줘", "PR 만들어" | ship | `ceo-plan.md` |
| "테스트해줘", "QA 돌려" | qa | `test-plan.md`, `design-review.md` |
| "코드 리뷰해줘", "diff 봐줘" | review | `eng-review.md`, `test-plan.md` |
| "문서 업데이트해줘" | document-release | `README.md` |
| "왜 안 돼", "버그인 것 같아" | investigate | `eng-review.md` |
| "체크포인트", "진행 상황 저장" | checkpoint | — |
| "코드 품질 확인" | health | — |

## OMC — 구현, 디버깅, 테스트, 병렬 실행

플러그인: `oh-my-claudecode@omc`. `/oh-my-claudecode:<name>`으로 실행.

**이런 요청이 오면 OMC를 쓴다:**

| 사용자가 이렇게 말하면 | 스킬 | 필수 참조 |
|----------------------|------|----------|
| "구현해줘", "만들어줘" | autopilot | `ceo-plan.md`, `eng-review.md` |
| "끝날 때까지 해줘" | ralph | `test-plan.md` |
| "병렬로 해줘", "팀으로" | team | `eng-review.md` |
| "다 같이 고쳐줘", "QA+수정" | ultraqa | `test-plan.md` |
| "AI 슬롭 정리" | ai-slop-cleaner | — |
| "원인 추적해줘" | trace | `eng-review.md` |
| "계획 세워줘" | plan | `ceo-plan.md` |
| "깊이 인터뷰" | deep-interview | `design-doc.md` |
| "3모델 합의" | ccg | — |
| "중단", "취소" | cancel | — |

**에이전트** (코드 작업 위임 시):

| 에이전트 | 언제 쓰나 | 필수 참조 |
|----------|----------|----------|
| executor | 코드 구현할 때 | `eng-review.md`, `ceo-plan.md` |
| test-engineer | 테스트 작성할 때 | `test-plan.md` |
| designer | UI 구현할 때 | `design-review.md` |
| debugger | 버그 수정할 때 | `eng-review.md` |
| code-reviewer | 코드 리뷰할 때 | `eng-review.md`, `test-plan.md` |
| architect | 설계 분석할 때 (읽기 전용) | `eng-review.md` |
| verifier | 완료 검증할 때 | `test-plan.md` |
| security-reviewer | 보안 검토할 때 | `eng-review.md` |
| git-master | Git 작업할 때 | — |

## 표준 워크플로우 (gstack + OMC 연동)

```
[기획 완료] plan-docs/ 확정 (이미 완료됨)
     ↓
[구현] OMC team 병렬 구현
     │  Lane A: auth + users + prisma schema (먼저)
     │  Lane B: dogs + reports (A 완료 후)
     │  Lane C: walks + patterns (A 완료 후)
     │  Lane D: greetings + messages (A+C 완료 후)
     │  Lane E: expo app shell (A와 병렬)
     ↓
[검토] gstack review — 각 lane 코드 리뷰
     ↓
[QA] gstack qa — E2E 테스트
     ↓
[버그] 발견 시 → OMC ralph 수정 → gstack qa 재검증
     ↓
[배포] gstack ship
```
