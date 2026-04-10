# Backend 코딩 컨벤션 (Kotlin + Spring Boot 3.5 + JPA + gRPC)

## 기본 원칙

- 읽기 쉬운 코드 > 아키텍처 미학
- 명시적 > 암묵적
- 도메인 로직은 프레임워크에 의존하지 않는다
- 코드/변수명/함수명: 영어. 주석: 한국어 허용.

## OOP / SOLID / DDD 원칙

### SOLID (필수)

- **S (단일 책임):** 클래스는 하나의 이유로만 변경된다. Service에 검증+조회+알림 전부 넣지 않는다.
- **O (개방-폐쇄):** 새 기능은 기존 코드 수정 없이 확장으로. 예: 새 성향 태그 추가 시 enum 값만 추가.
- **L (리스코프 치환):** 하위 타입은 상위 타입 자리에 넣어도 동작해야 한다.
- **I (인터페이스 분리):** 거대한 인터페이스 하나보다 작은 인터페이스 여러 개. 포트를 `CreateDogUseCase`, `GetDogUseCase`로 분리.
- **D (의존 역전):** 고수준 모듈이 저수준에 의존하지 않는다. domain → infrastructure가 아니라 infrastructure → domain 포트 구현.

### DDD 패턴 (적용)

- **도메인 모델(Entity):** 식별자(id)를 가진 비즈니스 객체. `Dog`, `Walk`, `Greeting`.
- **Value Object:** 식별자 없이 값으로만 비교. `DogSize`, `Temperament`, `GridCell`.
- **Aggregate:** 일관성 경계. `Greeting`은 `Message`를 포함하는 Aggregate Root.
- **Repository 포트:** 도메인이 정의하고 인프라가 구현. `DogRepositoryPort`.
- **도메인 서비스:** 여러 엔티티에 걸친 로직. 특정 엔티티에 속하지 않는 비즈니스 규칙.

### 과도한 OOP 금지 (가독성 우선)

아래 패턴은 이 프로젝트에서 **사용하지 않는다:**

- **추상 클래스 남용:** 구현체가 1개뿐인 추상 클래스/인터페이스 만들지 않는다. 포트 패턴은 예외 (테스트 용이성 때문).
- **과도한 상속 계층:** 2단계 이상 상속 금지. 상속보다 합성(composition) 선호.
- **불필요한 Factory/Builder:** 생성자 또는 Kotlin `copy()`로 충분한 경우 Factory 패턴 불필요.
- **제네릭 남발:** `BaseRepository<T, ID>` 같은 범용 추상화 금지. 각 리포지토리가 자기 도메인에 맞는 메서드를 직접 정의.
- **이벤트 소싱/CQRS 완전 분리:** MVP에서 불필요. Command/Query는 같은 모듈 안에서 분리하되 별도 DB 없이.
- **DTO 변환 체인:** Request → Command → Domain → Entity → Response 5단계 변환은 과설계. Request → Domain → Response 3단계면 충분.

**판단 기준:** "이 추상화를 제거하면 코드가 더 읽기 어려워지는가?" No라면 제거.

## 아키텍처: 서비스별 클린/헥사고날 (ADR-006)

각 MSA 서비스 내부는 4개 레이어로 구성:

```
services/walks/src/main/kotlin/com/mungcle/walks/
├── domain/                    # 순수 비즈니스 로직 (Spring/JPA 의존 금지)
│   ├── model/                 # 도메인 모델 (Walk, GridCell)
│   ├── port/
│   │   ├── in/                # 인바운드 포트 (유스케이스 인터페이스)
│   │   └── out/               # 아웃바운드 포트 (저장소, 외부 서비스 인터페이스)
│   └── service/               # 도메인 서비스 (순수 비즈니스 규칙)
│
├── application/               # 유스케이스 오케스트레이션
│   ├── command/               # 쓰기 유스케이스 (StartWalk, StopWalk)
│   ├── query/                 # 읽기 유스케이스 (GetNearbyWalks)
│   └── dto/                   # 애플리케이션 레벨 DTO
│
├── infrastructure/            # 외부 시스템 연결
│   ├── persistence/           # JPA 어댑터 (아웃바운드 포트 구현)
│   │   ├── WalkJpaRepository.kt
│   │   ├── WalkEntity.kt      # JPA @Entity (도메인 모델과 분리)
│   │   └── WalkMapper.kt      # 도메인 ↔ JPA 변환
│   ├── grpc/
│   │   ├── server/            # gRPC 서비스 구현 (인바운드 어댑터)
│   │   └── client/            # 다른 서비스 gRPC 클라이언트 (아웃바운드 어댑터)
│   └── kafka/                 # Kafka producer (아웃바운드 어댑터)
│
└── config/                    # Spring 설정, Bean 와이어링
```

## 레이어 규칙

### domain/ (핵심)
- Spring, JPA, gRPC 등 프레임워크 임포트 금지
- 순수 Kotlin 클래스/인터페이스만
- 비즈니스 규칙이 여기에 있어야 한다
- 테스트 시 모킹 없이 단독 실행 가능

```kotlin
// domain/model/Walk.kt
data class Walk(
    val id: Long,
    val dogId: Long,
    val userId: Long,
    val type: WalkType,
    val gridCell: GridCell,
    val status: WalkStatus,
    val startedAt: Instant,
    val endsAt: Instant,
) {
    fun isExpired(now: Instant): Boolean = now.isAfter(endsAt)
    fun isOpen(): Boolean = type == WalkType.OPEN && status == WalkStatus.ACTIVE
}

// domain/model/GridCell.kt (Value Object)
data class GridCell(val value: String) {
    companion object {
        fun fromCoordinates(lat: Double, lng: Double): GridCell {
            val latBucket = floor(lat / 0.002).toInt()
            val lngBucket = floor(lng / 0.002).toInt()
            return GridCell("$latBucket:$lngBucket")
        }

        // 3x3 인접 셀 계산
        fun adjacentCells(cell: GridCell): List<GridCell> { /* ... */ }
    }
}
```

### domain/port/ (인터페이스)
```kotlin
// domain/port/in/StartWalkUseCase.kt
interface StartWalkUseCase {
    suspend fun execute(command: StartWalkCommand): Walk
}

// domain/port/out/WalkRepositoryPort.kt
interface WalkRepositoryPort {
    fun save(walk: Walk): Walk
    fun findById(id: Long): Walk?
    fun findActiveByDogId(dogId: Long): Walk?
    fun findActiveOpenByGridCells(cells: List<GridCell>): List<Walk>
}

// domain/port/out/PetProfilePort.kt (다른 서비스 포트)
interface PetProfilePort {
    suspend fun getDogsByIds(ids: List<Long>): List<DogInfo>
}
```

### application/ (오케스트레이션)
- 인바운드 포트를 구현
- 아웃바운드 포트를 주입받아 사용
- `@Service` 사용 OK (Spring DI 활용)
- 트랜잭션 경계는 여기서 관리

```kotlin
// application/command/StartWalkCommandHandler.kt
@Service
class StartWalkCommandHandler(
    private val walkRepo: WalkRepositoryPort,
) : StartWalkUseCase {
    @Transactional
    override suspend fun execute(command: StartWalkCommand): Walk {
        walkRepo.findActiveByDogId(command.dogId)?.let {
            throw WalkAlreadyActiveException(command.dogId)
        }
        val gridCell = GridCell.fromCoordinates(command.lat, command.lng)
        val walk = Walk(/* ... */)
        return walkRepo.save(walk)
    }
}
```

### infrastructure/ (어댑터)

```kotlin
// infrastructure/persistence/WalkEntity.kt
@Entity
@Table(name = "walks", schema = "walks")
class WalkEntity(
    @Id @Tsid
    val id: Long = 0,

    @Column(nullable = false)
    val dogId: Long,

    @Column(nullable = false)
    val gridCell: String,

    @Enumerated(EnumType.STRING)
    val status: WalkStatus = WalkStatus.ACTIVE,

    // ...
)

// infrastructure/persistence/WalkJpaRepository.kt
@Repository
class WalkRepositoryAdapter(
    private val jpaRepo: WalkSpringDataRepository,
) : WalkRepositoryPort {
    override fun save(walk: Walk): Walk {
        val entity = WalkMapper.toEntity(walk)
        return WalkMapper.toDomain(jpaRepo.save(entity))
    }
    // ...
}

// Spring Data 인터페이스 (infrastructure 내부용)
interface WalkSpringDataRepository : JpaRepository<WalkEntity, Long> {
    fun findByDogIdAndStatus(dogId: Long, status: WalkStatus): WalkEntity?

    @Query("SELECT w FROM WalkEntity w WHERE w.gridCell IN :cells AND w.status = 'ACTIVE' AND w.type = 'OPEN'")
    fun findActiveOpenByGridCells(@Param("cells") cells: List<String>): List<WalkEntity>
}

// infrastructure/grpc/client/PetProfileGrpcClient.kt
@Component
class PetProfileGrpcClient(
    private val stub: PetProfileServiceGrpcKt.PetProfileServiceCoroutineStub,
) : PetProfilePort {
    override suspend fun getDogsByIds(ids: List<Long>): List<DogInfo> {
        val request = getDogsByIdsRequest { dogIds += ids }
        val response = stub.getDogsByIds(request)
        return response.dogsList.map { it.toDomainInfo() }
    }
}

// infrastructure/grpc/server/WalksGrpcService.kt
@GrpcService
class WalksGrpcService(
    private val startWalk: StartWalkUseCase,
    private val getNearby: GetNearbyWalksUseCase,
) : WalksServiceGrpcKt.WalksServiceCoroutineImplBase() {
    override suspend fun startWalk(request: StartWalkRequest): StartWalkResponse {
        val walk = startWalk.execute(request.toCommand())
        return walk.toProtoResponse()
    }
}
```

## Kotlin 스타일 규칙

### 필수

- `data class` 적극 활용 (DTO, VO, Command, Query)
- `val` 우선, `var` 최소화
- `sealed class`/`sealed interface`로 상태 모델링 (WalkStatus, GreetingStatus)
- Null 안전성: `?` 명시, `!!` 금지 (테스트 제외). `?.let {}`, `?: throw` 패턴 사용.
- Extension function 활용: proto ↔ domain 변환에 유용
- `when` exhaustive 매칭: `sealed class`와 함께 사용 시 `else` 금지 (컴파일 타임 체크)

### 금지

- `!!` (non-null assertion) — 프로덕션 코드에서 금지. `?: throw NotFoundException()` 사용.
- Java `Optional` — Kotlin nullable `?`로 대체.
- `companion object`에 비즈니스 로직 — factory 메서드나 상수만.
- `lateinit` 남발 — DI 주입 외에는 사용 금지.
- `object` 싱글턴에 mutable state — thread-unsafe.

### Coroutine 규칙

- gRPC 서버 핸들러: `suspend fun` (grpc-kotlin 네이티브)
- 여러 gRPC call 병렬 조합: `coroutineScope { async { } + awaitAll() }`
- Dispatcher 명시 금지 — Virtual Thread가 기본 스레드 풀. `Dispatchers.IO` 불필요.
- Exception: `try-catch` 또는 `supervisorScope` (한 실패가 다른 호출을 취소하면 안 될 때)

## JPA 규칙

- JPA `@Entity`는 `infrastructure/persistence/`에만 위치. 도메인 모델과 분리.
- 도메인 모델 ↔ JPA Entity 변환은 `Mapper` 클래스에서.
- `findAll`/`findMany` 시 `@EntityGraph` 또는 `JOIN FETCH` (N+1 방지).
- GPS 좌표 직접 저장 금지. `gridCell` 문자열에 200m 그리드 ID만.
- 서비스별 스키마 분리: `@Table(schema = "walks")`. Cross-schema 참조 금지.
- `@Transactional`은 application 레이어(CommandHandler)에서만.
- JPA Entity는 `open class`로 (Hibernate 프록시 호환). `allOpen` Gradle 플러그인 사용 권장.

```kotlin
// build.gradle.kts
plugins {
    kotlin("plugin.allopen")
    kotlin("plugin.jpa")  // no-arg constructor 자동 생성
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}
```

## Flyway 규칙

- 서비스별 `src/main/resources/db/migration/` 디렉토리.
- 파일명: `V{버전}__{설명}.sql` (예: `V1__create_walks_table.sql`)
- 이미 적용된 마이그레이션 파일 수정 금지. 새 마이그레이션 생성.
- 마이그레이션은 별도 커밋: `chore(walks): 마이그레이션 추가 — walks 테이블 생성`
- 각 서비스 마이그레이션 첫 파일에 스키마 생성 포함:
  ```sql
  -- V1__init_schema.sql
  CREATE SCHEMA IF NOT EXISTS walks;
  SET search_path TO walks;
  ```

## gRPC 규칙

- Proto 파일은 `proto/{service}/v1/` 에 위치.
- `buf lint` 통과 필수.
- Proto 메시지 네이밍: `{Action}{Entity}Request`, `{Action}{Entity}Response`
- Service 네이밍: `{Domain}Service` (예: `WalksService`, `PetProfileService`)
- gRPC 서버 구현은 `infrastructure/grpc/server/`에 (인바운드 어댑터).
- gRPC 클라이언트는 `infrastructure/grpc/client/`에 (아웃바운드 어댑터, 포트 구현).
- 에러 전파: gRPC Status Code 사용.

| 도메인 예외 | gRPC Status |
|-------------|-------------|
| NotFoundException | NOT_FOUND |
| AlreadyExistsException | ALREADY_EXISTS |
| ForbiddenException | PERMISSION_DENIED |
| ExpiredException | FAILED_PRECONDITION |
| ValidationException | INVALID_ARGUMENT |

## Kafka 규칙

- 토픽 네이밍: `{domain}.{event}` (예: `greeting.created`, `walk.expired`)
- 이벤트 DTO는 `common/kafka-common/`에 공유 정의.
- Producer는 `infrastructure/kafka/`에 위치 (아웃바운드 어댑터).
- Consumer는 `@KafkaListener` + Virtual Thread.
- idempotent consumer: 이벤트 ID 기반 중복 처리.
- 실패 시 DLQ (Dead Letter Queue) 전송.

```kotlin
// infrastructure/kafka/WalkExpiredEventPublisher.kt
@Component
class WalkExpiredEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, WalkExpiredEvent>,
) {
    suspend fun publish(walkId: Long, userId: Long) {
        val event = WalkExpiredEvent(walkId = walkId, userId = userId, occurredAt = Instant.now())
        kafkaTemplate.send("walk.expired", walkId.toString(), event)
    }
}
```

## 에러 처리

- 도메인 예외: `domain/` 안에서 정의. 프레임워크 독립. `sealed class` 활용.
- gRPC 서버: 도메인 예외 → gRPC StatusException 변환 (인터셉터 또는 서버 구현에서).
- api-gateway REST: gRPC StatusException → HTTP 응답 코드 변환.
- catch-all 금지. 구체적 예외만.

```kotlin
// domain/exception/WalkExceptions.kt
sealed class WalkException(message: String) : RuntimeException(message)
class WalkAlreadyActiveException(dogId: Long) : WalkException("Dog $dogId already has an active walk")
class WalkNotFoundException(walkId: Long) : WalkException("Walk $walkId not found")
class WalkAlreadyEndedException(walkId: Long) : WalkException("Walk $walkId already ended")
```

## 네이밍 컨벤션

| 타입 | 패턴 | 예시 | 위치 |
|------|------|------|------|
| 도메인 모델 | `<Name>` | `Walk`, `Dog` | `domain/model/` |
| Value Object | `<Name>` | `GridCell`, `Temperament` | `domain/model/` |
| 인바운드 포트 | `<Action><Entity>UseCase` | `StartWalkUseCase` | `domain/port/in/` |
| 아웃바운드 포트 | `<Entity><Purpose>Port` | `WalkRepositoryPort`, `PetProfilePort` | `domain/port/out/` |
| 커맨드 핸들러 | `<Action><Entity>CommandHandler` | `StartWalkCommandHandler` | `application/command/` |
| 쿼리 핸들러 | `<Get/List><Entity>QueryHandler` | `GetNearbyWalksQueryHandler` | `application/query/` |
| JPA Entity | `<Name>Entity` | `WalkEntity` | `infrastructure/persistence/` |
| JPA Repository | `<Name>SpringDataRepository` | `WalkSpringDataRepository` | `infrastructure/persistence/` |
| Repository 어댑터 | `<Name>RepositoryAdapter` | `WalkRepositoryAdapter` | `infrastructure/persistence/` |
| 매퍼 | `<Name>Mapper` | `WalkMapper` | `infrastructure/persistence/` |
| gRPC 서버 | `<Domain>GrpcService` | `WalksGrpcService` | `infrastructure/grpc/server/` |
| gRPC 클라이언트 | `<Domain>GrpcClient` | `PetProfileGrpcClient` | `infrastructure/grpc/client/` |
| Kafka Publisher | `<Event>Publisher` | `WalkExpiredEventPublisher` | `infrastructure/kafka/` |
| 요청 DTO | `<Action><Entity>Request` | `StartWalkRequest` | `application/dto/` |
| 응답 DTO | `<Entity>Response` | `WalkResponse` | `application/dto/` |
| Spring 설정 | `<Domain>Config` | `WalksConfig` | `config/` |

## 코드 문서화 (KDoc)

### 필수 작성 대상

- 포트 인터페이스 (인바운드/아웃바운드 모든 메서드)
- 도메인 모델의 public 메서드
- gRPC 서비스 구현의 override 메서드
- 도메인 예외 클래스
- 비즈니스 규칙이 담긴 로직 (왜 이렇게 하는지)

### 작성하지 않는 대상

- data class 필드 (이름이 곧 문서)
- Mapper의 toDomain/toEntity (변환 자체가 명확)
- 테스트 코드 (테스트 이름이 곧 문서)
- 자명한 getter/helper

### 형식

```kotlin
/**
 * 200m 그리드 기반으로 주변 산책 중인 개를 조회한다.
 * 차단한 유저의 개는 결과에서 제외된다.
 *
 * @param gridCell 현재 위치의 200m 그리드 ID
 * @param userId 요청자 ID (차단 필터링용)
 * @return 인접 3x3 그리드 셀의 활성 산책 목록
 */
suspend fun execute(query: GetNearbyWalksQuery): List<NearbyWalkInfo>
```

## 의존 방향 (절대 규칙)

```
gRPC server (인바운드) → application → domain ← infrastructure (아웃바운드)
                                          ↑
                                    의존 역전 (포트/어댑터)
```

- domain은 아무것도 임포트하지 않는다 (Spring, JPA, gRPC, Kafka 전부 금지).
- infrastructure는 domain의 포트를 구현한다.
- application은 domain의 유스케이스 포트를 구현하고 아웃바운드 포트를 주입받는다.
- 순환 의존 금지.

## 테스트 규칙

- **Unit Test:** 도메인 로직 (GridCell 경계값, Walk 만료, Greeting 상태 전이). MockK로 포트 모킹.
- **Integration Test:** `@DataJpaTest` + Testcontainers(PostgreSQL). Kafka + `@EmbeddedKafka` 또는 Testcontainers.
- **gRPC Test:** `grpc-testing`의 `InProcessServer`로 서비스 단독 테스트.
- **테스트 없는 기능 커밋 금지.**
- **에러 케이스 반드시 포함** (404, 409, 403 등).
- **Grid snap 함수: 경계값(적도, 음수, 0/0) 필수 포함.**
