package com.mungcle.walks.domain.port.out

import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus

/**
 * 산책 저장소 아웃바운드 포트.
 */
interface WalkRepositoryPort {
    suspend fun save(walk: Walk): Walk
    suspend fun findById(id: Long): Walk?
    suspend fun findByDogIdAndStatus(dogId: Long, status: WalkStatus): Walk?
    suspend fun findByGridCellInAndStatus(gridCells: List<String>, status: WalkStatus): List<Walk>
    suspend fun findByUserIdAndStatus(userId: Long, status: WalkStatus): List<Walk>
}
