package com.mungcle.walks.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WalkSpringDataRepository : JpaRepository<WalkEntity, Long> {

    fun findFirstByDogIdAndStatus(dogId: Long, status: WalkStatusEntity): WalkEntity?

    fun findByGridCellInAndStatus(gridCells: List<String>, status: WalkStatusEntity): List<WalkEntity>

    fun findByUserIdAndStatus(userId: Long, status: WalkStatusEntity): List<WalkEntity>
}
