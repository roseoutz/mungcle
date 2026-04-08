---
globs: ["frontend/**/*.tsx", "frontend/**/*.ts"]
---

# Frontend 규칙

- Feature-based 구조: `features/<name>/` 안에 screens, components, hooks, services 배치.
- `features/A/`에서 `features/B/` 직접 import 금지. `shared/` 또는 `index.ts` public API 사용.
- 색상/간격/폰트 하드코딩 금지. `constants/theme.ts` 참조.
- 인라인 스타일 금지. `StyleSheet.create()` 사용.
- 데이터 컴포넌트는 loading/empty/error/success 4가지 상태 필수.
- 터치 타겟 최소 44x44px. accessibilityLabel 필수.
- 상세: `ai/conventions-frontend.md` 참고.
