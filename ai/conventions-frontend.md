# Frontend 코딩 컨벤션 (Expo / React Native, Feature-based)

## 기본 원칙

- 읽기 쉬운 코드 > 아키텍처 미학
- 명시적 > 암묵적
- 기능(feature) 폴더가 곧 팀 경계
- 코드/변수명/함수명: 영어. 주석: 한국어 허용.

## OOP / 설계 원칙 (프론트엔드 적용)

React는 함수형이지만, 타입 설계와 모듈 구조에서 OOP 원칙이 적용된다.

### 적용하는 것

- **단일 책임:** 컴포넌트 하나 = 하나의 역할. `DogCard`가 인사하기 로직까지 갖지 않는다.
- **인터페이스 분리:** Props 인터페이스는 해당 컴포넌트에 필요한 것만. 거대한 공통 Props 타입 금지.
- **의존 역전:** 컴포넌트는 구체적 API 함수를 직접 호출하지 않는다. 커스텀 훅을 통해 데이터 접근.
- **합성(Composition) 우선:** 컴포넌트 합성으로 복잡한 UI 구성. HOC/상속보다 합성.
- **Value Object 타입:** `DogSize`, `Temperament` 등은 union type이나 enum으로 정의. 원시 문자열 금지.

```typescript
// 좋은 예: Value Object 타입
type DogSize = 'SMALL' | 'MEDIUM' | 'LARGE';
type Temperament = 'ACTIVE' | 'CALM' | 'CAUTION';

// 나쁜 예: 원시 문자열
interface Dog { size: string; temperament: string; }
```

### 하지 않는 것

- **클래스 컴포넌트** 금지. 함수형 컴포넌트만 사용.
- **상속 기반 컴포넌트** 금지. `extends BaseComponent` 같은 패턴 사용하지 않는다.
- **서비스 클래스 싱글톤** 금지. API 호출은 일반 함수 또는 커스텀 훅으로.
- **과도한 추상화** 금지. `<DataRenderer<T>>` 같은 제네릭 래퍼 컴포넌트 불필요. 각 컴포넌트가 자기 상태를 직접 처리.
- **중간 레이어 남발** 금지. Screen → Hook → API 3단계면 충분. Screen → ViewModel → UseCase → Repository → API 같은 5단계는 과설계.

**판단 기준:** "이 추상화를 제거하면 코드가 더 읽기 어려워지는가?" No라면 제거.

## 아키텍처: Feature-based

```
src/
├── features/                  # 기능별 분리 (스쿼드 경계)
│   ├── auth/
│   │   ├── screens/           # 화면 컴포넌트
│   │   │   ├── LoginScreen.tsx
│   │   │   └── RegisterScreen.tsx
│   │   ├── components/        # 이 기능 전용 컴포넌트
│   │   │   └── KakaoLoginButton.tsx
│   │   ├── hooks/             # 이 기능 전용 훅
│   │   │   └── useAuth.ts
│   │   ├── services/          # API 호출
│   │   │   └── auth.api.ts
│   │   ├── types/             # 타입 정의
│   │   │   └── auth.types.ts
│   │   └── index.ts           # public API (외부 노출)
│   │
│   ├── dogs/
│   │   ├── screens/
│   │   │   ├── DogRegisterScreen.tsx
│   │   │   └── DogProfileScreen.tsx
│   │   ├── components/
│   │   │   ├── DogCard.tsx
│   │   │   └── TemperamentChip.tsx
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── types/
│   │   └── index.ts
│   │
│   ├── walks/
│   │   ├── screens/
│   │   │   └── HomeScreen.tsx     # 홈 (주변 탐색)
│   │   ├── components/
│   │   │   ├── WalkToggle.tsx
│   │   │   ├── NearbyList.tsx
│   │   │   └── TimePatternCard.tsx
│   │   ├── hooks/
│   │   │   └── useNearbyWalks.ts
│   │   ├── services/
│   │   ├── types/
│   │   └── index.ts
│   │
│   ├── greetings/
│   │   ├── screens/
│   │   │   └── AlertsScreen.tsx   # 알림 (인사 목록)
│   │   ├── components/
│   │   │   ├── GreetingButton.tsx
│   │   │   └── MessageInput.tsx
│   │   ├── hooks/
│   │   ├── services/
│   │   ├── types/
│   │   └── index.ts
│   │
│   ├── reports/
│   │   ├── components/
│   │   │   ├── BlockButton.tsx
│   │   │   └── ReportModal.tsx
│   │   ├── services/
│   │   ├── types/
│   │   └── index.ts
│   │
│   └── settings/
│       ├── screens/
│       │   └── SettingsScreen.tsx
│       └── index.ts
│
├── shared/                    # 모든 기능이 공유
│   ├── components/            # 공통 UI
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   ├── EmptyState.tsx
│   │   ├── SkeletonCard.tsx
│   │   └── SafeAreaLayout.tsx
│   ├── hooks/
│   │   ├── useLocation.ts
│   │   └── useApi.ts
│   ├── services/
│   │   └── api-client.ts      # axios/fetch 설정, 인터셉터
│   ├── types/
│   │   └── common.types.ts
│   └── utils/
│       └── grid.ts            # 200m 그리드 계산
│
├── constants/
│   ├── theme.ts               # 색상, 간격, 폰트
│   └── layout.ts              # 레이아웃 상수
│
└── app/                       # expo-router (라우팅만)
    ├── _layout.tsx
    ├── (auth)/
    │   ├── login.tsx           # → features/auth/screens/LoginScreen
    │   └── register.tsx
    ├── (onboarding)/
    │   ├── dog-register.tsx
    │   └── neighborhood.tsx
    └── (tabs)/
        ├── _layout.tsx
        ├── index.tsx           # → features/walks/screens/HomeScreen
        ├── my-dogs.tsx         # → features/dogs/screens/...
        ├── alerts.tsx          # → features/greetings/screens/AlertsScreen
        └── settings.tsx        # → features/settings/screens/SettingsScreen
```

## 핵심 규칙

### 기능 간 경계
- `features/dogs/`에서 `features/walks/`를 직접 임포트 금지
- 기능 간 통신은 `shared/`를 통하거나, `index.ts`에서 export한 public API만 사용
- 각 feature의 `index.ts`가 외부에 노출할 것만 정의

```typescript
// features/dogs/index.ts
export { DogCard } from './components/DogCard';
export { useDogProfile } from './hooks/useDogProfile';
export type { Dog } from './types/dog.types';
```

### app/ 디렉토리 (expo-router)
- 라우팅 설정만. 비즈니스 로직 금지.
- 각 페이지 파일은 해당 feature의 screen을 임포트해서 렌더링만 함.

```typescript
// app/(tabs)/index.tsx
import { HomeScreen } from '@/features/walks/screens/HomeScreen';
export default HomeScreen;
```

## 컴포넌트 규칙

- 함수형 컴포넌트만
- 파일당 하나의 export default
- Props는 인터페이스로 정의: `interface DogCardProps { ... }`
- 컴포넌트 이름 = 파일 이름 (PascalCase)

## 스타일

- `StyleSheet.create()` 사용. 인라인 스타일 최소화.
- 색상, 간격, 폰트: `constants/theme.ts`에서 참조. 하드코딩 금지.
- 디자인 토큰 상세: `plan-docs/design-review.md`

```typescript
// constants/theme.ts
export const colors = {
  primary: '#4A7C59',
  secondary: '#8B6914',
  background: '#FBF8F1',
  surface: '#FFFFFF',
  text: '#1A1A1A',
  textMuted: '#6B7280',
  safe: '#22C55E',
  caution: '#EAB308',
  warning: '#EF4444',
}

export const spacing = { xs: 4, sm: 8, md: 16, lg: 24, xl: 32 }
export const radius = { card: 12, button: 8 }
export const fontSize = { heading: 22, body: 16, caption: 13 }
```

## 상태 관리

- React 내장만: `useState`, `useContext`, `useReducer`
- 외부 상태 라이브러리 추가 금지 (MVP 단계)
- 전역 상태: Context + Provider 패턴
- 서버 상태: 커스텀 훅으로 fetch + cache (또는 React Query 검토)

## 상태별 UI (필수)

모든 데이터 컴포넌트는 4가지 상태를 처리:
- **Loading:** 스켈레톤 또는 스피너
- **Empty:** 안내 메시지 + CTA (빈 화면 방치 금지)
- **Error:** 에러 메시지 + 재시도 버튼
- **Success:** 데이터 표시

상세 명세: `plan-docs/design-review.md` 상태 테이블 참고.

## 접근성

- 모든 이미지: `accessibilityLabel` 필수
- 인터랙티브 요소: `accessibilityRole` 지정
- 터치 타겟: 최소 44x44px
- 시스템 폰트 크기 존중 (`allowFontScaling`)
- SafeAreaView로 노치/홈바 대응

## 코드 문서화 (TSDoc)

### 필수 작성 대상

- 커스텀 훅 (파라미터, 반환값, 사용 예시)
- 공유 컴포넌트 (`shared/components/`) — Props 설명
- 유틸 함수 (`shared/utils/`)
- feature의 public API (`index.ts`에서 export하는 것)

### 작성하지 않는 대상

- 화면(Screen) 컴포넌트 (파일명이 곧 문서)
- feature 내부 전용 컴포넌트 (외부에 노출 안 되는 것)
- 스타일 정의
- 테스트 코드

### 형식

```typescript
/**
 * 주변 산책 중인 개 목록을 조회하는 훅.
 * 위치 권한이 없으면 수동 선택된 동네 기준으로 조회.
 *
 * @param gridCell - 200m 그리드 ID (없으면 현재 위치에서 계산)
 * @returns dogs: 주변 개 목록, loading, error, refetch
 *
 * @example
 * const { dogs, loading } = useNearbyWalks();
 */
export const useNearbyWalks = (gridCell?: string) => { }

/**
 * 개 프로필 카드. 사회성 점수에 따라 테두리 색상이 변한다.
 * - 4-5점: 녹색 (safe)
 * - 3점: 노란색 (caution)
 * - 1-2점: 빨간색 (warning)
 *
 * @param dog - 개 정보
 * @param onGreet - 인사하기 버튼 콜백 (없으면 버튼 숨김)
 */
export const DogCard = ({ dog, onGreet }: DogCardProps) => { }
```

### 비즈니스 로직 주석

훅이나 유틸에서 "왜?"가 명확하지 않은 부분에 한국어 주석:

```typescript
// GPS 좌표를 200m 그리드 중심점으로 스냅. 정확한 위치는 전송하지 않는다.
const snapToGrid = (lat: number, lng: number): string => {
  const gridLat = Math.floor(lat / 0.002) * 0.002;
  const gridLng = Math.floor(lng / 0.002) * 0.002;
  return `${gridLat.toFixed(3)}_${gridLng.toFixed(3)}`;
};
```

## API 호출

- `shared/services/api-client.ts`에서 base URL, 인터셉터, 토큰 관리
- 각 feature의 `services/`에서 도메인별 API 함수 정의
- 에러 처리: try/catch로 감싸고, 사용자 메시지 반환
