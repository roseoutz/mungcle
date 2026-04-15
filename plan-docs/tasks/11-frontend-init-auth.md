# 11. Frontend — 초기화 + 디자인 시스템 + 인증 + 온보딩

브랜치: `feature/frontend-init-auth` | 선행: 09 | 예상 PR 사이즈: ~500줄

---

## 프로젝트 초기화

- [ ] Expo 프로젝트 생성 (expo-router, TypeScript)
- [ ] Feature-based 디렉토리 구조 (features/, shared/, constants/)
- [ ] API 클라이언트 (fetch/axios + JWT 인터셉터)
- [ ] expo-notifications, expo-location 설정

## 디자인 시스템

- [ ] `constants/theme.ts` — Mono Mint v6 토큰 (emerald family, neutrals, spacing, shape)
- [ ] `constants/typography.ts` — Pretendard, scale (display~micro)
- [ ] 공통 컴포넌트: Button (primary/outline/disabled), Card (16px radius), TextField, Avatar

## 인증

- [ ] 로그인 화면 (Mono Mint login.png 참조) — 카카오 + 이메일 버튼
- [ ] 이메일 가입/로그인 화면
- [ ] JWT SecureStore 저장 + useAuth hook
- [ ] 비인증 → 로그인 리다이렉트

## 온보딩

- [ ] 강아지 등록 화면 (register.png) — 이름/견종/크기/성향/사회성/사진
- [ ] 동네 설정 화면 (location.png) — GPS 자동 or 수동 선택

## 네비게이션

- [ ] 탭 네비게이터: 홈/강아지/알림/설정 (한글, emerald-600 active)
- [ ] Auth flow: 비인증→로그인→온보딩, 인증→홈

## 테스트

- [ ] 공통 컴포넌트 렌더링 (Button, Card, TextField)
- [ ] 인증 플로우 (로그인→토큰 저장→인증 상태)

## 수락 기준

- [ ] Mono Mint v6 토큰 적용
- [ ] 로그인 → 온보딩 → 홈 네비게이션 동작
- [ ] 비인증 시 리다이렉트
