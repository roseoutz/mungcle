package com.mungcle.walks.domain.port.out

import com.mungcle.walks.domain.model.GridCell
import com.mungcle.walks.domain.model.Walk

interface WalkRepositoryPort {
    suspend fun save(walk: Walk): Walk
    suspend fun findById(id: Long): Walk?
    suspend fun findActiveByDogId(dogId: Long): Walk?
    suspend fun findActiveByUserId(userId: Long): List<Walk>
    suspend fun findActiveOpenByGridCells(gridCells: List<GridCell>): List<Walk>
}
