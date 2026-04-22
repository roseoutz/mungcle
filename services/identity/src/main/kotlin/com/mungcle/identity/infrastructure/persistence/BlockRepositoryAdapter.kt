package com.mungcle.identity.infrastructure.persistence

import com.mungcle.identity.domain.model.Block
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import org.springframework.stereotype.Repository

@Repository
class BlockRepositoryAdapter(
    private val springDataRepository: BlockSpringDataRepository,
) : BlockRepositoryPort {

    override fun save(block: Block): Block {
        val entity = BlockMapper.toEntity(block)
        val saved = springDataRepository.save(entity)
        return BlockMapper.toDomain(saved)
    }

    override fun delete(blockerId: Long, blockedId: Long) {
        springDataRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId)
    }

    override fun findByBlockerId(blockerId: Long): List<Block> =
        springDataRepository.findByBlockerId(blockerId).map(BlockMapper::toDomain)

    override fun findBlockedUserIds(userId: Long): List<Long> =
        springDataRepository.findBlockedUserIds(userId)

    override fun isBlocked(userIdA: Long, userIdB: Long): Boolean =
        springDataRepository.isBlocked(userIdA, userIdB)

    override fun existsByBlockerAndBlocked(blockerId: Long, blockedId: Long): Boolean =
        springDataRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)
}
