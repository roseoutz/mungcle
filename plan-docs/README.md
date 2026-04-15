# 멍클 (Mungcle) — Plan Documents

반려동물 신뢰 기반 산책 커뮤니티 앱. 2026-04-08 작성.

## 문서 목록

| 문서 | 내용 | 리뷰 상태 |
|------|------|-----------|
| [design-doc.md](design-doc.md) | 문제 정의, 전제, 접근법, 성공 기준 | APPROVED |
| [ceo-plan.md](ceo-plan.md) | Phase 1/2 기능 목록, scope 결정, outside voice | CLEARED |
| [eng-review.md](eng-review.md) | 기술 스택, MSA 아키텍처, 병렬화 전략 | CLEARED (2026-04-10 Kotlin MSA 전환) |
| [backend-requirements.md](backend-requirements.md) | MSA 서비스별 기능 요건, gRPC/Kafka 계약 | CLEARED (2026-04-10) |
| [design-review.md](design-review.md) | UI/UX 명세, Mono Mint v6 디자인 토큰 | CLEARED (10/10) |
| [test-plan.md](test-plan.md) | API 경로, 테스트 시나리오, 엣지 케이스 | CLEARED |

## 핵심 결정 요약

- **핵심 전환:** 위치 발견 → 신뢰/안전 중심
- **기술 스택:** Expo (React Native) + Kotlin/Spring Boot 3.5 MSA + JPA + gRPC + Kafka + Supabase + FCM
- **아키텍처:** 6개 MSA 서비스 (api-gateway + identity + pet-profile + walks + social + notification)
- **Phase 1 (5개 기능):** 개 프로필, 산책 상태, 주변 탐색+인사+메시지, 차단/신고, 시간대 패턴
- **Phase 2 (4개 기능):** 산책 로그, 산책 친구, 산책 예고(친구만), 장소 리뷰
- **Go/No-go:** 2주간 한 동네에서 인사하기 전환율 측정
- **상세 ADR:** `ai/decisions.md` (ADR-001~022)

## 리뷰 대시보드

| Review | Status | Score |
|--------|--------|-------|
| CEO Review | CLEARED | 7 proposals accepted |
| Eng Review | CLEARED | 3 issues resolved, 2026-04-10 MSA 전환 |
| Design Review | CLEARED | 3/10 → 10/10 (Mono Mint v6) |
| Outside Voice | ISSUES FOUND | 10 findings, 3 applied |
