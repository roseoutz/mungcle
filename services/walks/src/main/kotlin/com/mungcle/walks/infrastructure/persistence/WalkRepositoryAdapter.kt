package com.mungcle.walks.infrastructure.persistence

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class WalkRepositoryAdapter(
    private val springDataRepository: WalkSpringDataRepository,
) : WalkRepositoryPort {

    override fun save(walk: Walk): Walk {
        val entity = WalkMapper.toEntity(walk)
        val saved = springDataRepository.save(entity)
        return WalkMapper.toDomain(saved)
    }

    override fun findById(id: Long): Walk? =
        springDataRepository.findById(id).orElse(null)?.let(WalkMapper::toDomain)

    override fun findActiveByDogId(dogId: Long): Walk? =
        springDataRepository.findActiveByDogId(dogId).orElse(null)?.let(WalkMapper::toDomain)

    override fun findActiveByUserId(userId: Long): List<Walk> =
        springDataRepository.findActiveByUserId(userId).map(WalkMapper::toDomain)

    override fun findActiveOpenByGridCells(gridCells: List<GridCell>): List<Walk> =
        springDataRepository.findActiveOpenByGridCells(gridCells.map { it.value })
            .map(WalkMapper::toDomain)

    override fun findExpiredActive(now: Instant): List<Walk> =
        springDataRepository.findByStatusAndEndsAtBefore(now).map(WalkMapper::toDomain)

    override fun findDogIdsByUserIds(userIds: List<Long>): List<Long> =
        springDataRepository.findDogIdsByUserIds(userIds)

    override fun saveAll(walks: List<Walk>): List<Walk> {
        val entities = walks.map { WalkMapper.toEntity(it) }
        return springDataRepository.saveAll(entities).map { WalkMapper.toDomain(it) }
    }
}
