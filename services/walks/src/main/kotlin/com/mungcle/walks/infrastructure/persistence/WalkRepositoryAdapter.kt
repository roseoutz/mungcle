package com.mungcle.walks.infrastructure.persistence

import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Repository

@Repository
class WalkRepositoryAdapter(
    private val springDataRepository: WalkSpringDataRepository,
) : WalkRepositoryPort {

    override suspend fun save(walk: Walk): Walk {
        val entity = WalkMapper.toEntity(walk)
        val saved = springDataRepository.save(entity)
        return WalkMapper.toDomain(saved)
    }

    override suspend fun findById(id: Long): Walk? =
        springDataRepository.findById(id).orElse(null)?.let(WalkMapper::toDomain)

    override suspend fun findByDogIdAndStatus(dogId: Long, status: WalkStatus): Walk? {
        val statusEntity = when (status) {
            WalkStatus.ACTIVE -> WalkStatusEntity.ACTIVE
            WalkStatus.ENDED -> WalkStatusEntity.ENDED
        }
        return springDataRepository.findFirstByDogIdAndStatus(dogId, statusEntity)
            ?.let(WalkMapper::toDomain)
    }

    override suspend fun findByGridCellInAndStatus(gridCells: List<String>, status: WalkStatus): List<Walk> {
        val statusEntity = when (status) {
            WalkStatus.ACTIVE -> WalkStatusEntity.ACTIVE
            WalkStatus.ENDED -> WalkStatusEntity.ENDED
        }
        return springDataRepository.findByGridCellInAndStatus(gridCells, statusEntity)
            .map(WalkMapper::toDomain)
    }

    override suspend fun findByUserIdAndStatus(userId: Long, status: WalkStatus): List<Walk> {
        val statusEntity = when (status) {
            WalkStatus.ACTIVE -> WalkStatusEntity.ACTIVE
            WalkStatus.ENDED -> WalkStatusEntity.ENDED
        }
        return springDataRepository.findByUserIdAndStatus(userId, statusEntity)
            .map(WalkMapper::toDomain)
    }
}
