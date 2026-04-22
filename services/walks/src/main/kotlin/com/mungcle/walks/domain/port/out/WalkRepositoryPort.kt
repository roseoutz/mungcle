package com.mungcle.walks.domain.port.out

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.model.Walk
import java.time.Instant

interface WalkRepositoryPort {
    fun save(walk: Walk): Walk
    fun findById(id: Long): Walk?
    fun findActiveByDogId(dogId: Long): Walk?
    fun findActiveByUserId(userId: Long): List<Walk>
    fun findActiveOpenByGridCells(gridCells: List<GridCell>): List<Walk>

    /**
     * endsAt이 now 이전인 ACTIVE 상태의 산책 목록을 반환한다.
     * @param now 기준 시각
     */
    fun findExpiredActive(now: Instant): List<Walk>
}
