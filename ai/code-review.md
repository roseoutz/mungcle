# 코드 리뷰 체크리스트

PR 생성 전 또는 gstack review 실행 시 참고.

## 필수 확인

- [ ] 관련 테스트가 있고 전부 통과하는가
- [ ] N+1 쿼리 없는가 (JPA `@EntityGraph` / `JOIN FETCH` 사용)
- [ ] GPS 좌표를 직접 저장/전송하지 않는가 (200m 그리드만)
- [ ] catch-all 에러 처리 없는가 (구체적 예외만)
- [ ] 하드코딩된 디자인 토큰 없는가 (`constants/theme.ts` 참조)
- [ ] 차단된 유저가 nearby 결과에서 제외되는가
- [ ] Cross-schema JOIN이 없는가 (다른 서비스 데이터는 gRPC로만)

## 구조 확인

- [ ] gRPC 서버/gateway 컨트롤러에 비즈니스 로직이 없는가
- [ ] domain/ 레이어에 Spring/JPA/gRPC import가 없는가
- [ ] 모듈 간 순환 의존이 없는가
- [ ] 새 파일이 네이밍 컨벤션을 따르는가 (`ai/conventions-backend.md`, `ai/conventions-frontend.md`)
- [ ] 불필요한 추상화가 없는가 (한 번만 쓰이는 헬퍼/유틸)
- [ ] `!!` (non-null assertion)이 프로덕션 코드에 없는가

## 프론트엔드 추가 확인

- [ ] 모든 데이터 컴포넌트가 loading/empty/error/success 상태를 처리하는가
- [ ] 터치 타겟 44x44px 이상인가
- [ ] accessibilityLabel이 이미지에 있는가
- [ ] 인라인 스타일 대신 StyleSheet 사용하는가

## 보안 확인

- [ ] 사용자 입력이 검증되는가 (Bean Validation / DTO 검증)
- [ ] 본인 데이터만 수정 가능한가 (gRPC metadata userId + 소유권 확인)
- [ ] 민감 정보가 로그에 출력되지 않는가
