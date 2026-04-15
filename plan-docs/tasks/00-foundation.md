# 00. Foundation (스카폴딩) ✅ 완료

Lane A | 브랜치: `feature/project-scaffolding` | PR: #1, #2 (MERGED)

## 완료 항목

- [x] Gradle 9.4.1 모노레포 (settings.gradle.kts + build.gradle.kts)
- [x] common/domain-common (GridCell, TsidConfig)
- [x] common/kafka-common (Topics, Events)
- [x] common/grpc-client (buf 생성 코드 모듈)
- [x] Proto 정의 5개 (buf): identity, pet_profile, walks, social, notification
- [x] 6개 서비스 Spring Boot 메인 클래스 + application.yml
- [x] 서비스별 Flyway V1 마이그레이션
- [x] Docker Compose (postgres + kafka + observability)
- [x] 공유 Dockerfile (멀티스테이지)
- [x] .gitignore, README.md, PR 템플릿
- [x] ai/ 문서 Kotlin/Spring Boot 기준 갱신
- [x] Git 브랜치 전략 (main → develop → feature/*)
