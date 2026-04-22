package com.mungcle.walks.infrastructure.persistence

import com.mungcle.walks.domain.model.WalkPattern

object WalkPatternMapper {

    fun toDomain(entity: WalkPatternEntity): WalkPattern = WalkPattern(
        id = entity.id,
        gridCell = entity.gridCell,
        hourOfDay = entity.hourOfDay,
        dogId = entity.dogId,
        walkCount = entity.walkCount,
        lastWalkedAt = entity.lastWalkedAt,
    )
}
