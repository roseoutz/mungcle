# Backend 코딩 컨벤션 (NestJS + Prisma + 클린 아키텍처)

## 기본 원칙

- 읽기 쉬운 코드 > 아키텍처 미학
- 명시적 > 암묵적
- 도메인 로직은 프레임워크에 의존하지 않는다
- 코드/변수명/함수명: 영어. 주석: 한국어 허용.

## OOP / SOLID / DDD 원칙

### SOLID (필수)

- **S (단일 책임):** 클래스는 하나의 이유로만 변경된다. Service에 검증+조회+알림 전부 넣지 않는다.
- **O (개방-폐쇄):** 새 기능은 기존 코드 수정 없이 확장으로. 예: 새 성향 태그 추가 시 enum 값만 추가, 기존 로직 수정 불필요하도록.
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
- **불필요한 Factory/Builder:** 생성자로 충분한 경우 Factory 패턴 불필요. 필드 4개 이하면 Builder 불필요.
- **제네릭 남발:** `BaseRepository<T, ID>`같은 범용 추상화 금지. 각 리포지토리가 자기 도메인에 맞는 메서드를 직접 정의.
- **이벤트 소싱/CQRS 완전 분리:** MVP에서 불필요. Command/Query는 같은 모듈 안에서 분리하되 별도 DB나 이벤트 버스 없이.
- **DTO 변환 체인:** Request → Command → Domain → Entity → Response 5단계 변환은 과설계. Request → Domain → Response 3단계면 충분. 중간 DTO가 아무 변환 없이 필드를 그대로 넘기면 제거.

**판단 기준:** "이 추상화를 제거하면 코드가 더 읽기 어려워지는가?" No라면 제거.

## 아키텍처: 클린/헥사고날

각 모듈은 4개 레이어로 구성:

```
backend/src/
├── dogs/
│   ├── domain/              # 순수 비즈니스 로직 (NestJS/Prisma 의존 금지)
│   │   ├── models/          # 도메인 모델 (Dog, DogProfile)
│   │   ├── ports/           # 인터페이스 정의
│   │   │   ├── in/          # 인바운드 포트 (유스케이스 인터페이스)
│   │   │   └── out/         # 아웃바운드 포트 (저장소 인터페이스)
│   │   └── services/        # 도메인 서비스 (순수 비즈니스 규칙)
│   │
│   ├── application/         # 유스케이스 오케스트레이션
│   │   ├── commands/        # 쓰기 유스케이스 (CreateDog, UpdateDog)
│   │   ├── queries/         # 읽기 유스케이스 (GetDog, ListDogs)
│   │   └── dto/             # 애플리케이션 레벨 DTO
│   │
│   ├── infrastructure/      # 외부 시스템 연결
│   │   ├── persistence/     # Prisma 어댑터 (아웃바운드 포트 구현)
│   │   │   ├── dog.repository.ts
│   │   │   └── dog.mapper.ts    # 도메인 ↔ Prisma 변환
│   │   └── storage/         # Supabase Storage 어댑터
│   │
│   ├── presentation/        # HTTP 레이어
│   │   ├── dogs.controller.ts
│   │   └── dto/             # 요청/응답 DTO (class-validator)
│   │
│   └── dogs.module.ts       # NestJS 모듈 와이어링
│
├── auth/                    # 같은 구조
├── users/
├── walks/
├── greetings/
├── reports/
├── walk-patterns/
│
├── common/                  # 공유 코드
│   ├── decorators/
│   ├── filters/             # 전역 예외 필터
│   ├── guards/              # JwtAuthGuard
│   ├── interceptors/
│   └── exceptions/          # 커스텀 예외
│
└── prisma/                  # Prisma 서비스 (글로벌)
    └── prisma.service.ts
```

## 레이어 규칙

### domain/ (핵심)
- NestJS, Prisma 등 프레임워크 임포트 금지
- 순수 TypeScript 클래스/인터페이스만
- 비즈니스 규칙이 여기에 있어야 한다
- 테스트 시 모킹 없이 단독 실행 가능

```typescript
// domain/models/dog.model.ts
export class Dog {
  constructor(
    public readonly id: string,
    public readonly name: string,
    public readonly breed: string,
    public readonly size: DogSize,
    public readonly temperament: Temperament,
    public readonly sociabilityScore: number,
  ) {}

  isOpenToWalk(): boolean { /* 순수 비즈니스 로직 */ }
}
```

### domain/ports/ (인터페이스)
```typescript
// domain/ports/in/create-dog.usecase.ts
export interface CreateDogUseCase {
  execute(command: CreateDogCommand): Promise<Dog>;
}

// domain/ports/out/dog.repository.port.ts
export interface DogRepositoryPort {
  save(dog: Dog): Promise<Dog>;
  findById(id: string): Promise<Dog | null>;
  findByUserId(userId: string): Promise<Dog[]>;
}
```

### application/ (오케스트레이션)
- 인바운드 포트를 구현
- 아웃바운드 포트를 주입받아 사용
- `@Injectable()` 사용 OK (NestJS DI 활용)
- 트랜잭션 경계는 여기서 관리

```typescript
// application/commands/create-dog.command.ts
@Injectable()
export class CreateDogCommandHandler implements CreateDogUseCase {
  constructor(
    @Inject('DogRepositoryPort')
    private readonly dogRepo: DogRepositoryPort,
  ) {}

  async execute(command: CreateDogCommand): Promise<Dog> {
    // 유스케이스 로직
  }
}
```

### infrastructure/ (어댑터)
- 아웃바운드 포트를 구현
- Prisma, Supabase 등 외부 의존성은 여기서만
- 도메인 모델 ↔ Prisma 모델 매핑은 mapper에서

```typescript
// infrastructure/persistence/dog.repository.ts
@Injectable()
export class DogPrismaRepository implements DogRepositoryPort {
  constructor(private readonly prisma: PrismaService) {}

  async findById(id: string): Promise<Dog | null> {
    const record = await this.prisma.dog.findUnique({ where: { id } });
    return record ? DogMapper.toDomain(record) : null;
  }
}
```

### presentation/ (컨트롤러)
- 요청 파싱 + 응답 포맷만. 비즈니스 로직 금지.
- DTO에 `class-validator` 데코레이터
- Guard로 인증/인가

## 네이밍 컨벤션

| 타입 | 패턴 | 예시 | 위치 |
|------|------|------|------|
| 도메인 모델 | `<Name>` | `Dog`, `Walk` | `domain/models/` |
| 인바운드 포트 | `<Action><Entity>UseCase` | `CreateDogUseCase` | `domain/ports/in/` |
| 아웃바운드 포트 | `<Entity>RepositoryPort` | `DogRepositoryPort` | `domain/ports/out/` |
| 커맨드 핸들러 | `<Action><Entity>CommandHandler` | `CreateDogCommandHandler` | `application/commands/` |
| 쿼리 핸들러 | `<Action><Entity>QueryHandler` | `GetNearbyWalksQueryHandler` | `application/queries/` |
| 레포지토리 | `<Entity>PrismaRepository` | `DogPrismaRepository` | `infrastructure/persistence/` |
| 매퍼 | `<Entity>Mapper` | `DogMapper` | `infrastructure/persistence/` |
| 컨트롤러 | `<Entity>Controller` | `DogsController` | `presentation/` |
| 요청 DTO | `<Action><Entity>Request` | `CreateDogRequest` | `presentation/dto/` |
| 응답 DTO | `<Entity>Response` | `DogResponse` | `presentation/dto/` |

## 모듈 와이어링

```typescript
// dogs.module.ts
@Module({
  controllers: [DogsController],
  providers: [
    CreateDogCommandHandler,
    GetDogQueryHandler,
    {
      provide: 'DogRepositoryPort',
      useClass: DogPrismaRepository,
    },
  ],
})
export class DogsModule {}
```

## Prisma 규칙

- `findMany` 시 `include`로 관련 데이터 로드 (N+1 방지)
- GPS 좌표 직접 저장 금지. `gridCell`에 200m 그리드 ID만.
- 마이그레이션: `npx prisma migrate dev --name <설명>`
- 스키마 변경 후: `npx prisma generate`

## 에러 처리

- 도메인 예외: `domain/` 안에서 정의. 프레임워크 독립.
- presentation 레이어에서 도메인 예외 → NestJS HTTP 예외로 변환 (ExceptionFilter)
- catch-all 금지. 구체적 예외만.

## 코드 문서화 (TSDoc)

TypeScript의 TSDoc(/** */) 형식으로 주석을 작성한다.

### 필수 작성 대상

- 포트 인터페이스 (인바운드/아웃바운드 모든 메서드)
- 도메인 모델의 public 메서드
- 컨트롤러 엔드포인트
- 도메인 예외 클래스
- 비즈니스 규칙이 담긴 로직 (왜 이렇게 하는지)

### 작성하지 않는 대상

- getter/setter, 한 줄짜리 헬퍼
- DTO 필드 (class-validator 데코레이터가 문서 역할)
- Mapper의 toDomain/toPrisma (변환 자체가 명확)
- 테스트 코드 (테스트 이름이 곧 문서)

### 형식

```typescript
/**
 * 새 개 프로필을 등록한다.
 *
 * @param command - 개 등록 정보 (이름, 견종, 크기, 성향)
 * @returns 생성된 Dog 도메인 모델
 * @throws DuplicateDogNameException 같은 사용자가 동일 이름의 개를 등록할 때
 */
async execute(command: CreateDogCommand): Promise<Dog> { }

/**
 * 200m 그리드 기반으로 주변 산책 중인 개를 조회한다.
 * 차단한 유저의 개는 결과에서 제외된다.
 *
 * @param gridCell - 현재 위치의 200m 그리드 ID
 * @param userId - 요청자 ID (차단 필터링용)
 * @returns 인접 그리드 셀의 활성 산책 목록
 */
async findNearby(gridCell: string, userId: string): Promise<Walk[]> { }
```

### 비즈니스 규칙 주석

도메인 로직에서 "왜?"가 명확하지 않은 부분에 한국어 주석:

```typescript
// 사회성 점수는 상호 평가 3회 이상이면 자기 신고 값을 대체한다
if (evaluationCount >= 3) {
  this.sociabilityScore = averageScore;
}

// 인사는 5분 후 자동 만료. 서버 시간 기준.
const isExpired = Date.now() - greeting.createdAt > 5 * 60 * 1000;
```

## 의존 방향 (절대 규칙)

```
presentation → application → domain ← infrastructure
                                ↑
                          의존 역전 (포트/어댑터)
```

- domain은 아무것도 임포트하지 않는다.
- infrastructure는 domain의 포트를 구현한다.
- presentation은 application의 유스케이스를 호출한다.
- 순환 의존 금지.
