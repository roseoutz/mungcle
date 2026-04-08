---
globs: ["**/*.prisma", "**/prisma/**"]
---

# Prisma 규칙

- 스키마 변경 후 반드시 `npx prisma generate` 실행
- 이미 적용된 마이그레이션 파일 수정 금지. 새 마이그레이션 생성.
- GPS 좌표 직접 저장 금지. `gridCell` (String)에 200m 그리드 ID만.
- 새 테이블 추가 시 Supabase RLS 정책 설정 필수.
- 마이그레이션은 별도 커밋: `chore(prisma): <설명>`
