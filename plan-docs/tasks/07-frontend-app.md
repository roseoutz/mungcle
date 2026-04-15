# 07. Frontend App (Expo)

Lane H | 브랜치: `feature/frontend-app` | 선행: 06-api-gateway (REST API 안정화)
참조: `plan-docs/design-review.md` (Mono Mint v6), `ai/conventions-frontend.md`

---

## 1. 프로젝트 초기화

- [ ] **1.1** Expo 프로젝트 생성 (expo-router, TypeScript)
- [ ] **1.2** 디자인 시스템 세팅 — `constants/theme.ts` (Mono Mint v6 토큰)
  - Colors: emerald family + neutrals + functional
  - Typography: Pretendard (한글), scale 정의
  - Spacing: 4/8/12/16/24/32/48
  - Shape: card 16px, chip 8px, button pill, avatar circle
- [ ] **1.3** Feature-based 디렉토리 구조
  ```
  frontend/
  ├── app/              # expo-router 라우팅
  ├── features/
  │   ├── auth/
  │   ├── dogs/
  │   ├── walks/
  │   ├── greetings/
  │   └── notifications/
  ├── shared/
  │   ├── components/   # 공통 UI (Button, Card, EmptyState 등)
  │   ├── hooks/        # useApi, useAuth 등
  │   ├── services/     # API client
  │   └── utils/
  └── constants/
  ```
- [ ] **1.4** API 클라이언트 세팅 (axios/fetch + JWT 인터셉터)
- [ ] **1.5** expo-notifications 설정 (FCM)
- [ ] **1.6** expo-location 설정 (GPS → gridCell 보조 표시)

## 2. 인증 (Auth)

- [ ] **2.1** 로그인 화면 (Mono Mint v6 `login.png` 참조)
  - 멍클 로고 56px emerald
  - 추상 emerald blob 히어로
  - 카카오 로그인 버튼 (옐로우)
  - 이메일로 가입하기 outline 버튼
- [ ] **2.2** 이메일 가입 화면 — 이메일, 비밀번호, 닉네임 입력
- [ ] **2.3** 이메일 로그인 화면
- [ ] **2.4** JWT 토큰 저장 (SecureStore)
- [ ] **2.5** 인증 상태 관리 (useAuth hook)
- [ ] **2.6** 비인증 시 로그인 화면 리다이렉트

## 3. 온보딩

- [ ] **3.1** 강아지 등록 화면 (`register.png` 참조)
  - 이름, 견종, 크기 선택 (칩)
  - 성향 태그 1~3개 선택 (emerald 칩)
  - 사회성 등급 1~5 슬라이더
  - 사진 촬영/선택 (expo-image-picker)
  - 예방접종 사진 선택 (선택)
- [ ] **3.2** 동네 설정 화면 (`location.png` 참조)
  - GPS 자동 감지 or 수동 선택 드롭다운
  - 200m 그리드 설명 문구

## 4. 홈 (산책 탐색)

- [ ] **4.1** 홈 화면 레이아웃 (`home.png` 참조)
  - Emerald 그라데이션 hero "같이 걸어도 좋아요" 28px
  - 3상태 토글 (OFF / OPEN / SOLO)
- [ ] **4.2** 산책 상태 토글 기능
  - OFF → OPEN/SOLO 선택 → POST /api/walks/start (GPS lat/lng 포함)
  - ACTIVE → 탭 → POST /api/walks/:id/stop
  - 60분 카운트다운 표시
- [ ] **4.3** 주변 강아지 카드 목록
  - GET /api/walks/nearby
  - 카드: 원형 사진 72px + 한글 성향 칩 + 사회성 5dot + "인사하기" pill CTA
  - gridDistance 표시 (같은 구역/근처/조금 떨어짐)
- [ ] **4.4** 시간대 패턴 바 차트
  - GET /api/walk-patterns/nearby
  - 시간대별 막대, 현재 시간 하이라이트
  - "이 시간대에 자주 산책하는 강아지" 목록
- [ ] **4.5** 빈 화면 처리 — nearby 0건일 때 패턴 데이터로 대체 표시
- [ ] **4.6** 4상태 처리: loading (skeleton), empty, error, success

## 5. 우리 강아지

- [ ] **5.1** 우리 강아지 화면 (`woori.png` 참조)
  - 제목 "우리 강아지"
  - 등록된 강아지 카드 (크기 칩, 성향 칩, 사회성 dot, 예방접종 상태)
  - 점선 "강아지 추가하기" 카드
- [ ] **5.2** 강아지 상세/수정 화면
- [ ] **5.3** 강아지 등록 화면 (온보딩과 동일 컴포넌트 재사용)

## 6. 알림 (인사 및 소통)

- [ ] **6.1** 알림 화면 (`notifications.png` 참조)
  - "새로운 인사" 섹션 (emerald 좌측 bar)
  - "산책 친구 매칭" 섹션 (상호 인사 완료)
  - "내가 보낸 인사" 섹션 (회색)
- [ ] **6.2** 인사 수신 카드 — 상대 강아지 정보 + "인사 받기" CTA
- [ ] **6.3** 인사 응답 기능 — POST /api/greetings/:id/respond
- [ ] **6.4** 상호 인사 완료 모달 (`celebrate.png` 참조)
  - Bottom sheet
  - 두 강아지 사진 오버랩 + emerald 하트
  - "서로 인사 완료!" 32px
  - 30분 타이머 표시
  - "간단 메시지 보내기" emerald primary CTA
  - "대략적 위치 공유 켜기" emerald outline
- [ ] **6.5** 메시지 전송/목록 화면 — 간단 텍스트 (140자)
- [ ] **6.6** 인앱 알림 목록 — GET /api/notifications + 읽음 처리
- [ ] **6.7** 푸시 알림 수신 처리 (expo-notifications handler)

## 7. 설정

- [ ] **7.1** 설정 화면 (`settings.png` 참조)
  - 프로필 수정 (닉네임, 동네)
  - 차단 관리
  - 로그아웃
  - 회원 탈퇴
- [ ] **7.2** 차단/신고 화면 (`block-report.png` 참조)
  - 차단 목록 + 해제
  - 신고 폼 (사유 텍스트)

## 8. 네비게이션

- [ ] **8.1** 탭 네비게이터 — 홈 / 강아지 / 알림 / 설정 (한글, emerald-600 active)
- [ ] **8.2** Auth flow — 비인증: 로그인 → 온보딩 / 인증: 홈
- [ ] **8.3** Deep link — 푸시 알림 탭 → 해당 인사/알림 화면

## 9. 공통 컴포넌트

- [ ] **9.1** `Button` — emerald primary (pill), outline, disabled 상태
- [ ] **9.2** `Card` — 16px radius, tonal layering
- [ ] **9.3** `Chip` — 성향 태그 (emerald variations)
- [ ] **9.4** `SociabilityDots` — 5개 circle (emerald fill vs neutral empty)
- [ ] **9.5** `EmptyState`, `ErrorState`, `LoadingSkeleton`
- [ ] **9.6** `Avatar` — 원형, 72px/48px 사이즈
- [ ] **9.7** `TextField` — 8px radius, validation 에러 표시

## 10. 테스트

- [ ] **10.1** 공통 컴포넌트 렌더링 테스트 (4상태)
- [ ] **10.2** 인증 플로우 테스트 (로그인 → 토큰 저장 → 인증 상태)
- [ ] **10.3** 산책 토글 상태 전환 테스트
- [ ] **10.4** 인사하기 → 응답 → 메시지 플로우 테스트

## 수락 기준

- [ ] Mono Mint v6 디자인 토큰 적용 (emerald family, Pretendard)
- [ ] 모든 데이터 컴포넌트 4상태 (loading/empty/error/success)
- [ ] 터치 타겟 최소 44x44px
- [ ] accessibilityLabel 모든 이미지/아이콘에 적용
- [ ] 탄성 인터랙션 — 버튼 press 시 scale(0.98)
- [ ] GPS 권한 거부 시 수동 동네 선택 fallback
- [ ] 비인증 시 로그인 화면 리다이렉트
