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
1. plan-docs/ 관련 문서 읽기
2. 계획 수립 → 사용자 확인
3. OMC team/autopilot으로 구현
4. gstack review로 코드 리뷰
5. gstack qa로 테스트
6. gstack ship으로 배포

## 표준 구현 흐름 (Phase 1)

```
[기획 완료] plan-docs/ 참고
     ↓
[구현] OMC team 병렬 실행
     │  Lane A: auth + users + prisma schema (먼저)
     │  Lane B: dogs + reports (A 후)
     │  Lane C: walks + patterns (A 후)
     │  Lane D: greetings + messages (A+C 후)
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
- PR 전 `npx prisma validate` + lint + test 전체 통과 확인
