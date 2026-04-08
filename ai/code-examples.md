# 코드 패턴 예시

새 모듈/컴포넌트를 만들 때 이 패턴을 따를 것. 구현이 시작되면 실제 코드 파일을 참조 예시로 추가.

## Backend: 새 모듈 만들기 (클린 아키텍처)

### 도메인 모델
```typescript
// dogs/domain/models/dog.model.ts
export enum DogSize { SMALL = 'SMALL', MEDIUM = 'MEDIUM', LARGE = 'LARGE' }
export enum Temperament { ACTIVE = 'ACTIVE', CALM = 'CALM', CAUTION = 'CAUTION' }

export class Dog {
  constructor(
    public readonly id: string,
    public readonly userId: string,
    public readonly name: string,
    public readonly breed: string,
    public readonly size: DogSize,
    public readonly temperament: Temperament,
    public readonly sociabilityScore: number,
    public readonly photoUrl: string | null,
  ) {}
}
```

### 포트 (인터페이스)
```typescript
// dogs/domain/ports/out/dog-repository.port.ts
export interface DogRepositoryPort {
  save(dog: Dog): Promise<Dog>;
  findById(id: string): Promise<Dog | null>;
  findByUserId(userId: string): Promise<Dog[]>;
}

// dogs/domain/ports/in/create-dog.usecase.ts
export interface CreateDogUseCase {
  execute(command: CreateDogCommand): Promise<Dog>;
}
```

### 유스케이스
```typescript
// dogs/application/commands/create-dog.handler.ts
@Injectable()
export class CreateDogCommandHandler implements CreateDogUseCase {
  constructor(
    @Inject('DogRepositoryPort')
    private readonly dogRepo: DogRepositoryPort,
  ) {}

  async execute(command: CreateDogCommand): Promise<Dog> {
    const dog = new Dog(
      generateId(),
      command.userId,
      command.name,
      command.breed,
      command.size,
      command.temperament,
      command.sociabilityScore,
      null,
    );
    return this.dogRepo.save(dog);
  }
}
```

### 인프라 (Prisma 어댑터)
```typescript
// dogs/infrastructure/persistence/dog.repository.ts
@Injectable()
export class DogPrismaRepository implements DogRepositoryPort {
  constructor(private readonly prisma: PrismaService) {}

  async findById(id: string): Promise<Dog | null> {
    const record = await this.prisma.dog.findUnique({
      where: { id },
      include: { user: true },  // N+1 방지
    });
    return record ? DogMapper.toDomain(record) : null;
  }

  async save(dog: Dog): Promise<Dog> {
    const record = await this.prisma.dog.create({
      data: DogMapper.toPrisma(dog),
    });
    return DogMapper.toDomain(record);
  }
}
```

### 컨트롤러
```typescript
// dogs/presentation/dogs.controller.ts
@Controller('dogs')
@UseGuards(JwtAuthGuard)
export class DogsController {
  constructor(
    @Inject('CreateDogUseCase')
    private readonly createDog: CreateDogUseCase,
  ) {}

  @Post()
  async create(
    @Body() dto: CreateDogRequest,
    @CurrentUser() user: UserPayload,
  ): Promise<DogResponse> {
    const dog = await this.createDog.execute({
      userId: user.id,
      ...dto,
    });
    return DogResponse.from(dog);
  }
}
```

### 모듈 와이어링
```typescript
// dogs/dogs.module.ts
@Module({
  controllers: [DogsController],
  providers: [
    { provide: 'CreateDogUseCase', useClass: CreateDogCommandHandler },
    { provide: 'DogRepositoryPort', useClass: DogPrismaRepository },
  ],
  exports: ['DogRepositoryPort'],
})
export class DogsModule {}
```

## Frontend: 새 Feature 만들기

### 화면
```typescript
// features/dogs/screens/DogRegisterScreen.tsx
import { View, ScrollView } from 'react-native';
import { SafeAreaLayout } from '@/shared/components/SafeAreaLayout';
import { DogForm } from '../components/DogForm';
import { useDogRegister } from '../hooks/useDogRegister';
import { styles } from './DogRegisterScreen.styles';

export const DogRegisterScreen = () => {
  const { register, loading, error } = useDogRegister();

  return (
    <SafeAreaLayout>
      <ScrollView style={styles.container}>
        <DogForm onSubmit={register} loading={loading} error={error} />
      </ScrollView>
    </SafeAreaLayout>
  );
};
```

### 컴포넌트 (상태 4가지 처리)
```typescript
// features/walks/components/NearbyList.tsx
import { EmptyState } from '@/shared/components/EmptyState';
import { SkeletonCard } from '@/shared/components/SkeletonCard';
import { ErrorState } from '@/shared/components/ErrorState';
import { DogCard } from '@/features/dogs';  // public API 통해 import

interface NearbyListProps {
  dogs: Dog[];
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export const NearbyList = ({ dogs, loading, error, onRefresh }: NearbyListProps) => {
  if (loading) return <SkeletonCard count={3} />;
  if (error) return <ErrorState message={error} onRetry={onRefresh} />;
  if (dogs.length === 0) {
    return (
      <EmptyState
        message="이 시간대에 자주 산책하는 친구들"
        action={{ label: '산책 시작하기', onPress: startWalk }}
      />
    );
  }
  return dogs.map(dog => <DogCard key={dog.id} dog={dog} />);
};
```

### Feature의 public API
```typescript
// features/dogs/index.ts
export { DogCard } from './components/DogCard';
export { DogRegisterScreen } from './screens/DogRegisterScreen';
export { useDogProfile } from './hooks/useDogProfile';
export type { Dog } from './types/dog.types';
// 내부 구현은 export하지 않음
```

### expo-router 페이지 (라우팅만)
```typescript
// app/(tabs)/index.tsx
import { HomeScreen } from '@/features/walks/screens/HomeScreen';
export default HomeScreen;
```

---

> 구현 시작 후 실제 코드가 생기면, "이 파일을 참고하라"는 식으로 업데이트할 것.
> 예: "새 모듈은 `backend/src/dogs/`의 구조를 따를 것"
