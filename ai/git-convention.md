# Git 컨벤션

## 브랜치

```
main                          # 프로덕션 릴리스
  └── develop                 # 통합 개발 브랜치
        └── feature/<name>    # 새 기능 (develop에서 분기 → develop으로 PR)
        └── fix/<name>        # 버그 수정
```

- main, develop에 직접 푸시 금지. PR을 통해서만.
- feature/fix 브랜치는 **develop**에서 분기하고 develop으로 머지.
- develop → main 머지는 릴리스 시점에만.
- 브랜치 이름은 영어, kebab-case: `feature/dog-profile`, `fix/grid-snap-boundary`

## 커밋

형식:
```
<type>(<scope>): <한글 설명>
```

타입: `feat`, `fix`, `refactor`, `test`, `chore`, `docs`
스코프: `auth`, `users`, `dogs`, `walks`, `greetings`, `reports`, `walk-patterns`, `notification`, `gateway`, `proto`, `infra`, `app`

예시:
```
feat(dogs): 개 프로필 CRUD gRPC 서비스 구현
fix(walks): 적도 부근 grid snap 계산 오류 수정
test(greetings): 인사 만료 엣지 케이스 테스트 추가
chore(infra): docker-compose Kafka 설정 업데이트
chore(flyway): walks 테이블 마이그레이션 추가
```

## 커밋 전 필수 확인

커밋 전에 반드시 관련 테스트를 실행하여 정상 동작을 확인할 것.

```bash
# 전체 테스트
./gradlew test

# 개별 서비스 테스트
./gradlew :services:identity:test
./gradlew :services:walks:test

# Frontend
cd frontend && npm test
```

- 테스트가 실패하면 커밋하지 않는다.
- 새 기능 추가 시 테스트 코드도 함께 작성한 뒤 커밋.
- 테스트 없는 기능 커밋은 허용하지 않는다.

## 규칙

- 한 논리적 변경당 한 커밋
- 커밋 메시지는 한국어
- Flyway 마이그레이션은 별도 커밋: `chore(flyway): <서비스명> <테이블명> 마이그레이션 추가`
- Proto 정의 변경은 별도 커밋: `chore(proto): <서비스명> RPC 추가/변경`
