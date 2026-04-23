# 워크플로우

## 도구 분담

- **기획/문서/리뷰/QA** → gstack 스킬 사용
- **구현/디버깅/병렬 실행** → OMC 스킬/에이전트 사용
- **작은 수정** (한 파일, 명확한 변경) → 직접 수정

## 작업 모드

### Fast Path (작은 작업)
버그 수정, 테스트 추가, 설정 변경 등.
1. 관련 코드 읽기
2. 수정
3. 테스트 실행
4. 완료

확인 없이 자율 실행. 모호할 때만 질문.

### Full Flow (큰 작업)
새 모듈 추가, 크로스 모듈 변경, 데이터 모델 변경 등.
아래 "표준 이슈-투-머지 파이프라인" 전체를 따른다.

## 표준 작업 파이프라인

모든 작업은 아래 파이프라인을 따른다. **각 단계에서 Notion 작업 카드를 반드시 최신화**한다.

### 작업 시작점은 두 가지

| 시작점 | 흐름 | GitHub Issue |
|--------|------|-------------|
| **버그/이슈 발견** | ① → ② → ③ → ④ ~ ⑧ | 필수 (이슈 추적) |
| **기능/작업 카드** | ③ → ④ ~ ⑧ | 불필요 (Notion 카드만) |

- GitHub Issue는 **버그 리포트, 외부 이슈 추적이 필요한 경우에만** 등록한다.
- Notion 작업 카드에서 시작하는 기능 개발/리팩터링은 GitHub Issue 없이 바로 진행한다.

```
① 이슈 발견 (버그, 결함 등)
     ↓
② GitHub Issue 등록 (버그/이슈 추적 시에만)
     ↓
③ Notion 작업 카드 등록 (Mungcle Backlog)
     │  - GitHub Issue가 있으면 링크 포함
     │  - 에픽 연결 (에픽 키 기입)
     │  - 상태: "시작 전"
     ↓
④ 브랜치 생성
     │  - base: develop (기본) 또는 파생 작업 시 해당 브랜치
     │  - 이름: <type>/MC-<번호>-<설명>
     │  - 예: feature/MC-40-rich-domain-model
     ↓
⑤ 구현 (OMC team/autopilot/ralph)
     │  - Notion 상태 → "진행 중"
     │  - 중간 진척 사항 Notion 카드 본문에 업데이트
     ↓
⑥ 코드 리뷰 요청
     │  - 작업 브랜치 → develop PR 생성 (코드 리뷰는 PR 기반으로 진행)
     │  - Notion 상태 → "코드 리뷰 중"
     │  - gstack review 또는 code-reviewer 에이전트 실행
     ↓
⑦ 코드 리뷰 완료
     │  - Notion 상태 → "리뷰 완료"
     │  - 리뷰 결과 요약을 Notion 카드에 기록
     ↓
⑧ 사용자 확인 후 머지
     │  - 사용자에게 머지 여부 확인 (자동 머지 금지)
     │  - PR 승인 후 작업 브랜치 → develop 머지
     │  - develop → main은 PR 생성 후 관리자 승인으로만 머지 (직접 머지 금지)
     │  - 머지 후 Notion 상태 → "완료"
     │  - 브랜치 삭제
```

### Notion 카드 관리 규칙

| 규칙 | 설명 |
|------|------|
| **항상 최신화** | 작업 시작, 중간 진행, 리뷰, 완료 — 모든 상태 변경 시 Notion 카드 업데이트 |
| **브랜치명 = 이슈 키 프리픽스** | `<type>/MC-<번호>-<설명>` (예: `feature/MC-40-rich-domain-model`) |
| **에픽 분리 관리** | 에픽은 Backlog에 "Epic" 유형으로 별도 등록. 하위 Story/Task의 "에픽" 필드에 에픽 이슈 키(예: `MC-10`) 기입하여 연결 |
| **GitHub 연결** | Notion 카드 본문 첫 줄에 GitHub Issue 링크 포함 |
| **진척 기록** | 구현 중 주요 변경사항/결정사항을 카드 본문에 간략히 기록 |

### Notion 상태 매핑

Mungcle Backlog DB의 "상태" 속성에 아래 옵션이 필요하다.

| 상태 | 그룹 | 파이프라인 단계 |
|------|------|---------------|
| 시작 전 | To-do | ③ 카드 등록 |
| 진행 중 | In progress | ⑤ 구현 |
| 코드 리뷰 중 | In progress | ⑥ 리뷰 요청 |
| 리뷰 완료 | In progress | ⑦ 리뷰 통과 |
| 완료 | Complete | ⑧ 머지 완료 |

> **설정 필요:** Notion에서 Mungcle Backlog DB → "상태" 속성 → "코드 리뷰 중", "리뷰 완료" 옵션을 In progress 그룹에 추가할 것.

### Notion API 정보

| 항목 | 값 |
|------|-----|
| Backlog DB ID | `0ecda766-cff9-491a-9d12-eba4b76123a7` |
| 이슈 키 프리픽스 | `MC` |
| 주요 속성 | 제목, 유형, 상태, 우선순위, 라벨, 서비스, 스프린트, 에픽, 스토리 포인트 |

## 표준 구현 흐름 (Phase 1 병렬 레인)

```
[기획 완료] plan-docs/ 참고
     ↓
[구현] OMC team 병렬 실행
     │  Lane A: proto + common + identity-service (먼저)
     │  Lane B: pet-profile-service (A 후)
     │  Lane C: walks-service (A 후)
     │  Lane D: social-service (A+C 후)
     │  Lane E: expo app shell (A와 병렬)
     ↓
[검토] gstack review
     ↓
[QA] gstack qa
     ↓
[버그] OMC ralph → gstack qa 재검증
     ↓
[배포] gstack ship
```

## 검증 규칙

- 기능 구현 후 반드시 관련 테스트 실행
- 테스트 실패 시 수정 후 재실행 (통과할 때까지)
- PR 전 `./gradlew test` + lint + build 전체 통과 확인
