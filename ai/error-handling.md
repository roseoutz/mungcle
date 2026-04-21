# 에러 핸들링 패턴

## Backend 에러 구조

### 도메인 예외 (domain/ 레이어)

프레임워크 독립. 순수 Kotlin.

```kotlin
// common/domain-common/src/.../exception/DomainException.kt
abstract class DomainException(
    val code: String,
    override val message: String,
) : RuntimeException(message)

// pet-profile/domain/exception/
class DogNotFoundException(id: Long) :
    DomainException("DOG_NOT_FOUND", "개를 찾을 수 없습니다: $id")

// walks/domain/exception/
class AlreadyWalkingException(userId: Long) :
    DomainException("ALREADY_WALKING", "이미 산책 중입니다")

// social/domain/exception/
class GreetingExpiredException(greetingId: Long) :
    DomainException("GREETING_EXPIRED", "인사 시간이 만료되었습니다")

class DuplicateGreetingException :
    DomainException("DUPLICATE_GREETING", "이미 인사를 보냈습니다")
```

### gRPC 상태 변환 (infrastructure 레이어)

gRPC ExceptionHandler에서 도메인 예외를 gRPC Status로 변환:

```kotlin
// common/grpc-common/src/.../GrpcExceptionAdvice.kt
@GrpcAdvice
class GrpcExceptionAdvice {

    private val statusMap = mapOf(
        "DOG_NOT_FOUND" to Status.NOT_FOUND,
        "WALK_NOT_FOUND" to Status.NOT_FOUND,
        "ALREADY_WALKING" to Status.ALREADY_EXISTS,
        "DUPLICATE_GREETING" to Status.ALREADY_EXISTS,
        "GREETING_EXPIRED" to Status.FAILED_PRECONDITION,
        "BLOCKED_USER" to Status.PERMISSION_DENIED,
        "NOT_OWNER" to Status.PERMISSION_DENIED,
    )

    @GrpcExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): StatusRuntimeException {
        val status = statusMap[e.code] ?: Status.INTERNAL
        return status.withDescription(e.message).asRuntimeException()
    }
}
```

### REST 에러 변환 (api-gateway)

api-gateway에서 gRPC Status를 HTTP 응답으로 변환:

```kotlin
// api-gateway/.../RestExceptionHandler.kt
@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(StatusRuntimeException::class)
    fun handleGrpcException(e: StatusRuntimeException): ResponseEntity<ErrorResponse> {
        val httpStatus = when (e.status.code) {
            Status.Code.NOT_FOUND -> HttpStatus.NOT_FOUND
            Status.Code.ALREADY_EXISTS -> HttpStatus.CONFLICT
            Status.Code.PERMISSION_DENIED -> HttpStatus.FORBIDDEN
            Status.Code.FAILED_PRECONDITION -> HttpStatus.GONE
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
        return ResponseEntity.status(httpStatus).body(
            ErrorResponse(httpStatus.value(), e.status.description, Instant.now())
        )
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
