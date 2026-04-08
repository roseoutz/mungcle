# 에러 핸들링 패턴

## Backend 에러 구조

### 도메인 예외 (domain/ 레이어)

프레임워크 독립. 순수 TypeScript.

```typescript
// common/domain/exceptions/base.exception.ts
export abstract class DomainException extends Error {
  constructor(
    public readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

// dogs/domain/exceptions/
export class DogNotFoundException extends DomainException {
  constructor(id: string) {
    super('DOG_NOT_FOUND', `개를 찾을 수 없습니다: ${id}`);
  }
}

// walks/domain/exceptions/
export class AlreadyWalkingException extends DomainException {
  constructor(userId: string) {
    super('ALREADY_WALKING', `이미 산책 중입니다`);
  }
}

// greetings/domain/exceptions/
export class GreetingExpiredException extends DomainException {
  constructor(greetingId: string) {
    super('GREETING_EXPIRED', `인사 시간이 만료되었습니다`);
  }
}

export class DuplicateGreetingException extends DomainException {
  constructor() {
    super('DUPLICATE_GREETING', `이미 인사를 보냈습니다`);
  }
}
```

### HTTP 변환 (presentation 레이어)

NestJS ExceptionFilter에서 도메인 예외를 HTTP 응답으로 변환:

```typescript
// common/filters/domain-exception.filter.ts
@Catch(DomainException)
export class DomainExceptionFilter implements ExceptionFilter {
  private readonly statusMap: Record<string, number> = {
    DOG_NOT_FOUND: 404,
    WALK_NOT_FOUND: 404,
    ALREADY_WALKING: 409,
    DUPLICATE_GREETING: 409,
    GREETING_EXPIRED: 410,
    BLOCKED_USER: 403,
    NOT_OWNER: 403,
  };

  catch(exception: DomainException, host: ArgumentsHost) {
    const status = this.statusMap[exception.code] ?? 500;
    const response = host.switchToHttp().getResponse();
    response.status(status).json({
      statusCode: status,
      code: exception.code,
      message: exception.message,
      timestamp: new Date().toISOString(),
    });
  }
}
```

### 에러 응답 형식 (통일)

```json
{
  "statusCode": 404,
  "code": "DOG_NOT_FOUND",
  "message": "개를 찾을 수 없습니다: abc123",
  "timestamp": "2026-04-08T12:00:00.000Z"
}
```

## Frontend 에러 처리

### API 에러 핸들링

```typescript
// shared/services/api-client.ts
// 인터셉터에서 공통 에러 처리

// 각 feature의 hooks에서:
const { data, error, loading } = useApi(() => dogsApi.getById(id));

// 컴포넌트에서:
if (loading) return <SkeletonCard />;
if (error) return <ErrorState message={error.message} onRetry={refetch} />;
if (!data) return <EmptyState message="개 정보가 없습니다" />;
return <DogCard dog={data} />;
```

### 사용자 에러 메시지 매핑

```typescript
const errorMessages: Record<string, string> = {
  DOG_NOT_FOUND: '개 정보를 찾을 수 없어요',
  ALREADY_WALKING: '이미 산책 중이에요',
  DUPLICATE_GREETING: '이미 인사를 보냈어요',
  GREETING_EXPIRED: '인사 시간이 지났어요',
  BLOCKED_USER: '차단된 사용자예요',
  NETWORK_ERROR: '인터넷 연결을 확인해주세요',
  UNKNOWN: '문제가 발생했어요. 다시 시도해주세요',
};
```

## 로깅 규칙

- 비밀 정보(토큰, 비밀번호, GPS 좌표) 로깅 금지
- 예상치 못한 에러: 스택 트레이스 포함
- 비즈니스 이벤트(인사, 산책 시작): 간결하게 기록
- 루프/고빈도 경로에서 과도한 로깅 금지
