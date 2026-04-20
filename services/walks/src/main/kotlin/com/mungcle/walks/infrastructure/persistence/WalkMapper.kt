package com.mungcle.walks.infrastructure.persistence

import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType

object WalkMapper {

    fun toDomain(entity: WalkEntity): Walk = Walk(
        id = entity.id,
        dogId = entity.dogId,
        userId = entity.userId,
        type = when (entity.type) {
            WalkTypeEntity.OPEN -> WalkType.OPEN
            WalkTypeEntity.SOLO -> WalkType.SOLO
        },
        gridCell = entity.gridCell,
        status = when (entity.status) {
            WalkStatusEntity.ACTIVE -> WalkStatus.ACTIVE
            WalkStatusEntity.ENDED -> WalkStatus.ENDED
        },
        startedAt = entity.startedAt,
        endsAt = entity.endsAt,
        endedAt = entity.endedAt,
        createdAt = entity.createdAt,
    )

    fun toEntity(domain: Walk): WalkEntity = WalkEntity(
        id = domain.id,
        dogId = domain.dogId,
        userId = domain.userId,
        type = when (domain.type) {
            WalkType.OPEN -> WalkTypeEntity.OPEN
            WalkType.SOLO -> WalkTypeEntity.SOLO
        },
        gridCell = domain.gridCell,
        status = when (domain.status) {
            WalkStatus.ACTIVE -> WalkStatusEntity.ACTIVE
            WalkStatus.ENDED -> WalkStatusEntity.ENDED
        },
        startedAt = domain.startedAt,
        endsAt = domain.endsAt,
        endedAt = domain.endedAt,
        createdAt = domain.createdAt,
    )
}
