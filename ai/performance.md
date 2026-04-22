# 성능 예산

## API 응답 시간

| 엔드포인트 유형 | 목표 p95 | 비고 |
|---------------|---------|------|
| 단순 조회 (GET /dogs/:id) | < 200ms | DB 단일 쿼리 |
| 목록 조회 (GET /walks/nearby) | < 500ms | 인덱스 필수 |
| 쓰기 (POST /dogs, /walks/start) | < 500ms | 사진 업로드 제외 |
| 사진 업로드 | < 3s | Supabase Storage |

## DB 쿼리 규칙

- 모든 목록 API는 페이지네이션 필수: `?page=1&limit=20` (기본 20, 최대 50)
- JPA 목록 조회에 반드시 `@EntityGraph` 또는 `JOIN FETCH`로 관련 데이터 로드 (N+1 방지)
- nearby 쿼리: `walks (gridCell, status, updatedAt)` 복합 인덱스
- 인사 중복 방지: `greetings (senderId, receiverId)` unique 인덱스

## 프론트엔드

- 앱 첫 로드 (cold start): < 3s 목표
- 화면 전환: < 300ms (네이티브 느낌)
- 이미지: 개 프로필 사진은 썸네일(200x200) + 원본 분리. 목록에서는 썸네일만 로드.
- 무한 스크롤 시 한 번에 20개씩 로드

## CRON

- 산책 자동 만료: 1분 간격 (`@Scheduled(fixedRate = 60_000)`)
- 시간대 패턴 집계: MVP에서는 실시간 쿼리. 데이터 쌓이면 사전 집계 테이블로 전환.
