---
globs: ["**/db/migration/**", "**/persistence/**/*.kt", "**/*.sql"]
---

# DB / JPA / Flyway 규칙

- GPS 좌표 직접 저장 금지. `gridCell` (String)에 200m 그리드 ID만.
- Flyway 마이그레이션: 이미 적용된 파일 수정 금지. 새 마이그레이션 생성.
- 서비스별 스키마 분리: `@Table(schema = "walks")`. Cross-schema 참조 금지.
- JPA Entity는 `infrastructure/persistence/`에만 위치. 도메인 모델과 분리.
- `@Transactional`은 application 레이어(CommandHandler)에서만.
- 마이그레이션은 별도 커밋: `chore(<service>): <설명>`
- TSID (`@Tsid Long`) 사용. Node ID는 서비스 이름 기반 자동 할당.
