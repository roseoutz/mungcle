# 멍클 (Mungcle)

반려동물 신뢰 기반 산책 커뮤니티 앱.
상대 개의 성향/안전성을 미리 확인하고, 자연스럽게 산책에 합류하는 경험.

## 기술 스택

| Layer | Technology |
|-------|------------|
| Frontend | Expo (React Native) |
| Backend | NestJS (TypeScript) |
| ORM | Prisma |
| DB + Storage | Supabase |
| Push | FCM (expo-notifications) |
| Auth | 카카오 OAuth + 이메일 fallback |

## 로컬 실행

### 사전 준비

- Node.js 18+
- Expo CLI (`npm install -g expo-cli`)
- Supabase 프로젝트 (DB + Storage)
- Firebase 프로젝트 (FCM)
- 카카오 개발자 앱

### 환경변수

```bash
cp .env.example .env
# .env에 Supabase, Firebase, 카카오 키 입력
# 상세: ai/env-vars.md 참고
```

### 백엔드

```bash
cd backend
npm install
npx prisma generate
npx prisma migrate dev
npm run start:dev     # http://localhost:4000
```

### 프론트엔드 (Expo)

```bash
cd frontend
npm install
npx expo start        # Expo DevTools
```

## 프로젝트 구조

```
mungcle/
├── CLAUDE.md              # AI 코딩 가이드 (진입점)
├── backend/               # NestJS API 서버 (클린 아키텍처)
├── frontend/              # Expo React Native 앱 (Feature-based)
├── plan-docs/             # 설계/리뷰 문서 ("무엇을" 만드는지)
├── ai/                    # 코딩 가이드 ("어떻게" 만드는지)
└── .claude/rules/         # 파일 유형별 자동 규칙
```

## Phase 1 기능 (MVP)

1. 개 프로필 카드 (성향, 사회성 등급, 예방접종)
2. 산책 상태 토글 ("같이 걸어도 좋아요")
3. 주변 탐색 + 인사하기 + 간단 메시지
4. 차단/신고 시스템
5. 산책 시간대 패턴 (빈 화면 방지)

상세: `plan-docs/ceo-plan.md`

## 문서

- 설계/기능: `plan-docs/`
- 코딩 규칙: `ai/`
- 아키텍처 결정: `ai/decisions.md`
