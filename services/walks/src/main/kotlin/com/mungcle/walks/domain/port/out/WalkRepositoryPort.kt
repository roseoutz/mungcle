package com.mungcle.walks.domain.port.out

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk

interface WalkRepositoryPort {
    fun save(walk: Walk): Walk
    fun findById(id: Long): Walk?
    fun findActiveByDogId(dogId: Long): Walk?
    fun findActiveByUserId(userId: Long): List<Walk>
    fun findActiveOpenByGridCells(gridCells: List<GridCell>): List<Walk>
}
