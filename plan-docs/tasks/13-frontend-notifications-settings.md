# 13. Frontend — 알림 + 설정 + 마무리

브랜치: `feature/frontend-notifications-settings` | 선행: 12 | 예상 PR 사이즈: ~400줄

---

## 알림 화면

- [ ] 알림 목록 (notifications.png) — 새 인사/매칭/보낸 인사 3섹션
- [ ] 인사 수신 카드 + "인사 받기" CTA
- [ ] 인사 응답 → POST /api/greetings/:id/respond
- [ ] 메시지 전송/목록 화면 (140자)
- [ ] 인앱 알림 목록 (GET /api/notifications + 읽음 처리)
- [ ] 푸시 알림 탭 → 해당 화면 deep link

## 설정 화면

- [ ] 설정 (settings.png) — 프로필 수정/차단 관리/로그아웃/탈퇴
- [ ] 차단/신고 (block-report.png) — 차단 목록+해제, 신고 폼
- [ ] 로그아웃 (토큰 삭제 + 로그인 리다이렉트)
- [ ] 회원 탈퇴 (확인 모달 + DELETE /api/users/me)

## 마무리

- [ ] 전체 화면 네비게이션 점검
- [ ] 에러 핸들링 통합 (네트워크 에러, 서버 에러)
- [ ] 앱 아이콘 + 스플래시 화면 (emerald 로고)

## 테스트

- [ ] 알림 목록 렌더링 (4상태)
- [ ] 차단 목록 + 해제 동작
- [ ] 탈퇴 플로우

## 수락 기준

- [ ] 모든 화면 Mono Mint v6 디자인 적용
- [ ] 푸시 → 인앱 deep link 동작
- [ ] 탈퇴 후 로그인 화면 리다이렉트
- [ ] 전체 앱 네비게이션 정상 동작
