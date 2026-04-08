# 환경변수

`.env` 파일에 설정. `.env`는 git에 커밋하지 않는다.

## Backend (.env)

```bash
# Supabase
SUPABASE_URL=https://xxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGci...
SUPABASE_SERVICE_ROLE_KEY=eyJhbGci...   # 백엔드에서만 사용. 프론트 노출 금지.

# Database (Supabase PostgreSQL)
DATABASE_URL=postgresql://postgres:password@db.xxxxx.supabase.co:5432/postgres

# Auth
JWT_SECRET=your-jwt-secret-here
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
KAKAO_REDIRECT_URI=your-redirect-uri

# FCM (Firebase Cloud Messaging)
FCM_PROJECT_ID=your-firebase-project-id
FCM_PRIVATE_KEY=your-firebase-private-key
FCM_CLIENT_EMAIL=your-firebase-client-email

# Server
PORT=4000
```

## Frontend (Expo 앱)

Expo는 `app.json` 또는 `app.config.ts`의 `extra` 필드로 환경변수 전달.
또는 `expo-constants`의 `expoConfig.extra`로 접근.

```bash
# .env (expo-env.d.ts로 타입 정의)
EXPO_PUBLIC_SUPABASE_URL=https://xxxxx.supabase.co
EXPO_PUBLIC_SUPABASE_ANON_KEY=eyJhbGci...
EXPO_PUBLIC_API_URL=http://localhost:4000/api
```

**주의:**
- `EXPO_PUBLIC_` prefix가 있어야 프론트에서 접근 가능
- `SERVICE_ROLE_KEY`는 절대 프론트에 넣지 않는다
- FCM 키는 `google-services.json` (Android) / `GoogleService-Info.plist` (iOS)로 관리

## 로컬 개발 환경 세팅

```bash
# 1. 루트에서
cp .env.example .env
# 2. Supabase 프로젝트 생성 후 키 입력
# 3. 카카오 개발자 앱 등록 후 키 입력
# 4. Firebase 프로젝트 생성 후 키 입력
```

## .env.example (커밋 대상)

실제 값 없이 키 이름만 담은 템플릿. 새 개발자 온보딩용.
