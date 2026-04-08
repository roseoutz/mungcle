# 도메인 모델

## 핵심 엔티티

```
User ──1:N── Dog
User ──1:N── Walk
User ──1:N── Block
User ──1:N── Report
Dog  ──1:N── Walk
Walk ──────── gridCell (200m 그리드 ID)

Greeting ── sender (User)
         ── receiver (User)
         ── senderDog (Dog)
         ──1:N── Message

WalkPattern ── gridCell + hourOfDay + dogId (집계)
```

## 엔티티 설명

### User
- 카카오 또는 이메일로 가입
- 여러 개를 등록 가능
- 동네(neighborhood) 설정 (GPS 거부 시 수동 선택)

### Dog
- 소유자(User)에 속함
- 이름, 견종, 크기(소/중/대), 사진
- 성향 태그: 활발 / 조용 / 주의필요
- 사회성 등급: 1-5 (자기 신고 → 상호 평가 3회 후 대체)
- 예방접종 사진 (검증 불가, "등록됨"으로 표시)

### Walk
- 특정 Dog의 산책 상태
- status: ACTIVE / ENDED
- type: OPEN ("같이 걸어도 좋아요") / SOLO ("혼자 산책 중")
- gridCell: 200m 그리드 ID (GPS → 스냅)
- 60분 후 자동 ENDED (CRON)

### Greeting
- 인사 보낸 사람(sender) → 받는 사람(receiver)
- status: PENDING → ACCEPTED / EXPIRED
- PENDING: 5분 후 자동 EXPIRED
- ACCEPTED: 양쪽에 위치 공유 30분
- unique constraint: (senderId, receiverId) 중복 방지

### Message
- Greeting에 속함 (상호 인사 후에만 전송 가능)
- 간단한 텍스트만 ("파란 벤치 앞에 있어요")
- 위치 공유 30분 만료 후 전송 불가

### Block
- blocker(User) → blocked(User)
- nearby 쿼리에서 차단된 유저 자동 필터링

### Report
- reporter(User) → reported(User)
- 사유(reason) 텍스트
- 3회 이상 신고 시 수동 검토 대상

### WalkPattern (Phase 1)
- Walk 데이터를 기반으로 집계
- gridCell + hourOfDay별 자주 산책하는 Dog 목록
- 실시간 산책자가 없을 때 "이 시간대에 자주 산책하는 개" 표시

## 핵심 불변 규칙

- GPS 좌표를 직접 저장하지 않는다. gridCell만 저장.
- 상호 인사(ACCEPTED) 전에는 메시지를 보낼 수 없다.
- 차단한 유저는 모든 목록(nearby, patterns)에서 제외된다.
- 한 User가 같은 User에게 중복 인사를 보낼 수 없다.
- 산책 상태는 60분 후 자동 종료된다.
