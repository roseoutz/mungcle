# 12. Frontend — 홈 + 산책 + 인사

브랜치: `feature/frontend-home-walks` | 선행: 10, 11 | 예상 PR 사이즈: ~500줄

---

## 홈 화면

- [ ] 레이아웃 (home.png) — emerald hero + 3상태 토글 + 카드 목록
- [ ] 산책 토글: OFF→OPEN/SOLO (POST /api/walks/start with GPS)
- [ ] 60분 카운트다운 표시
- [ ] 주변 강아지 카드 목록 (GET /api/walks/nearby)
- [ ] 카드: 원형 사진 + 성향 칩 + 사회성 dot + "인사하기" CTA
- [ ] 시간대 패턴 바 차트 (GET /api/walk-patterns/nearby)
- [ ] 빈 화면 → 패턴 데이터로 대체
- [ ] 4상태: loading/empty/error/success

## 우리 강아지

- [ ] 강아지 목록 (woori.png) — 카드 + "추가하기" 점선 카드
- [ ] 강아지 상세/수정 화면
- [ ] 공통 컴포넌트: Chip, SociabilityDots, EmptyState

## 인사

- [ ] 인사하기 버튼 → POST /api/greetings
- [ ] 상호 인사 완료 모달 (celebrate.png) — bottom sheet + 30분 타이머 + 메시지/위치 CTA
- [ ] 푸시 알림 수신 처리

## 테스트

- [ ] 산책 토글 상태 전환
- [ ] 인사하기→응답→모달 플로우

## 수락 기준

- [ ] 터치 타겟 44x44px, accessibilityLabel
- [ ] 탄성 인터랙션 (scale 0.98)
- [ ] GPS 권한 거부 시 수동 동네 fallback
- [ ] 4상태 컴포넌트 전체 처리
