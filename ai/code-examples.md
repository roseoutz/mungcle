# 코드 패턴 예시

새 모듈/컴포넌트를 만들 때 이 패턴을 따를 것. 구현이 시작되면 실제 코드 파일을 참조 예시로 추가.

## Backend: 새 모듈 만들기 (클린 아키텍처, Kotlin + Spring Boot)

### 도메인 모델
```kotlin
// domain/model/Dog.kt
enum class DogSize { SMALL, MEDIUM, LARGE }
enum class Temperament { ACTIVE, CALM, CAUTION }

data class Dog(
    val id: Long,
    val userId: Long,
    val name: String,
    val breed: String,
    val size: DogSize,
    val temperament: Temperament,
    val sociabilityScore: Int,
    val photoUrl: String?,
)
```

### 포트 (인터페이스)
```kotlin
// domain/port/out/DogRepositoryPort.kt
interface DogRepositoryPort {
    fun save(dog: Dog): Dog
    fun findById(id: Long): Dog?
    fun findByUserId(userId: Long): List<Dog>
}

// domain/port/in/CreateDogUseCase.kt
interface CreateDogUseCase {
    fun execute(command: CreateDogCommand): Dog
}
```

### 유스케이스
```kotlin
// application/command/CreateDogCommandHandler.kt
@Service
class CreateDogCommandHandler(
    private val dogRepo: DogRepositoryPort,
) : CreateDogUseCase {

    @Transactional
    override fun execute(command: CreateDogCommand): Dog {
        val dog = Dog(
            id = 0, // TSID 자동 할당
            userId = command.userId,
            name = command.name,
            breed = command.breed,
            size = command.size,
            temperament = command.temperament,
            sociabilityScore = command.sociabilityScore,
            photoUrl = null,
        )
        return dogRepo.save(dog)
    }
}
```

### 인프라 (JPA 어댑터)
```kotlin
// infrastructure/persistence/DogJpaRepository.kt
@Repository
class DogJpaRepositoryAdapter(
    private val jpaRepository: DogSpringDataRepository,
) : DogRepositoryPort {

    @EntityGraph(attributePaths = ["owner"]) // N+1 방지
    override fun findById(id: Long): Dog? {
        return jpaRepository.findById(id)
            .map { DogMapper.toDomain(it) }
            .orElse(null)
    }

    override fun save(dog: Dog): Dog {
        val entity = DogMapper.toEntity(dog)
        return DogMapper.toDomain(jpaRepository.save(entity))
    }
}
```

### gRPC 서버 구현
```kotlin
// infrastructure/grpc/server/PetProfileGrpcService.kt
@GrpcService
class PetProfileGrpcService(
    private val createDog: CreateDogUseCase,
) : PetProfileServiceGrpcKt.PetProfileServiceCoroutineImplBase() {

    override suspend fun createDog(request: CreateDogRequest): CreateDogResponse {
        val dog = createDog.execute(request.toCommand())
        return dog.toProto()
    }
}
```

### Spring 설정 (Bean 와이어링)
```kotlin
// config/PetProfileConfig.kt
@Configuration
class PetProfileConfig {
    // Spring의 @Service, @Repository 자동 스캔으로 와이어링
    // 필요 시 수동 Bean 등록
}
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
> 예: "새 모듈은 `services/pet-profile/`의 구조를 따를 것"
