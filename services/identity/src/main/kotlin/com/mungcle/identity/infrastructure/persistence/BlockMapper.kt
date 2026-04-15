package com.mungcle.identity.infrastructure.persistence

import com.mungcle.identity.domain.model.Block

object BlockMapper {
    fun toDomain(entity: BlockEntity): Block = Block(
        id = entity.id,
        blockerId = entity.blockerId,
        blockedId = entity.blockedId,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Block): BlockEntity = BlockEntity(
        id = domain.id,
        blockerId = domain.blockerId,
        blockedId = domain.blockedId,
        createdAt = domain.createdAt,
    )
}
