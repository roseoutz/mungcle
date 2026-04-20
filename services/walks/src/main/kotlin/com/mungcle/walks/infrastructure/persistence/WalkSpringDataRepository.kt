package com.mungcle.walks.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface WalkSpringDataRepository : JpaRepository<WalkEntity, Long> {

    @Query("SELECT w FROM WalkEntity w WHERE w.dogId = :dogId AND w.status = 'ACTIVE'")
    fun findActiveByDogId(@Param("dogId") dogId: Long): Optional<WalkEntity>

    @Query("SELECT w FROM WalkEntity w WHERE w.userId = :userId AND w.status = 'ACTIVE'")
    fun findActiveByUserId(@Param("userId") userId: Long): List<WalkEntity>

    @Query("SELECT w FROM WalkEntity w WHERE w.gridCell IN :gridCells AND w.status = 'ACTIVE' AND w.type = 'OPEN'")
    fun findActiveOpenByGridCells(@Param("gridCells") gridCells: List<String>): List<WalkEntity>
}
