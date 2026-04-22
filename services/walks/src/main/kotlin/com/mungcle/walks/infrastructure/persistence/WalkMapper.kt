package com.mungcle.walks.infrastructure.persistence

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType

object WalkMapper {

    fun toDomain(entity: WalkEntity): Walk = Walk(
        id = entity.id,
        dogId = entity.dogId,
        userId = entity.userId,
        type = WalkType.valueOf(entity.type),
        gridCell = GridCell(entity.gridCell),
        status = WalkStatus.valueOf(entity.status),
        startedAt = entity.startedAt,
        endsAt = entity.endsAt,
    )

    fun toEntity(domain: Walk): WalkEntity = WalkEntity(
        id = domain.id,
        dogId = domain.dogId,
        userId = domain.userId,
        type = domain.type.name,
        gridCell = domain.gridCell.value,
        status = domain.status.name,
        startedAt = domain.startedAt,
        endsAt = domain.endsAt,
        endedAt = if (domain.status == WalkStatus.ENDED) domain.endsAt else null,
    )
}
