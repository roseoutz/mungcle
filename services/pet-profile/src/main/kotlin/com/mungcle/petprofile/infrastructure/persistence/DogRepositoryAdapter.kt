package com.mungcle.petprofile.infrastructure.persistence

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Repository

@Repository
class DogRepositoryAdapter(
    private val springDataRepository: DogSpringDataRepository,
) : DogRepositoryPort {

    override suspend fun save(dog: Dog): Dog {
        val entity = DogMapper.toEntity(dog)
        val saved = springDataRepository.save(entity)
        return DogMapper.toDomain(saved)
    }

    override suspend fun findById(id: Long): Dog? =
        springDataRepository.findByIdAndDeletedAtIsNull(id)?.let(DogMapper::toDomain)

    override suspend fun findByOwnerId(ownerId: Long): List<Dog> =
        springDataRepository.findByOwnerIdAndDeletedAtIsNull(ownerId).map(DogMapper::toDomain)

    override suspend fun findByIds(ids: List<Long>): List<Dog> =
        springDataRepository.findByIdInAndDeletedAtIsNull(ids).map(DogMapper::toDomain)

    override suspend fun countByOwnerId(ownerId: Long): Long =
        springDataRepository.countByOwnerIdAndDeletedAtIsNull(ownerId)
}
