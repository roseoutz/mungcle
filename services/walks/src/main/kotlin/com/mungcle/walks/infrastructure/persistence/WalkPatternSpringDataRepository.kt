package com.mungcle.walks.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface WalkPatternSpringDataRepository : JpaRepository<WalkPatternEntity, Long> {

    @Query(
        "SELECT p FROM WalkPatternEntity p WHERE p.gridCell IN :gridCells AND p.hourOfDay BETWEEN :minHour AND :maxHour"
    )
    fun findByGridCellsAndHourRange(
        @Param("gridCells") gridCells: List<String>,
        @Param("minHour") minHour: Int,
        @Param("maxHour") maxHour: Int,
    ): List<WalkPatternEntity>

    @Modifying
    @Query(
        value = """
            INSERT INTO walks.walk_patterns (id, grid_cell, hour_of_day, dog_id, walk_count, last_walked_at)
            VALUES (:id, :gridCell, :hourOfDay, :dogId, 1, :walkedAt)
            ON CONFLICT (grid_cell, hour_of_day, dog_id)
            DO UPDATE SET walk_count = walks.walk_patterns.walk_count + 1,
                          last_walked_at = EXCLUDED.last_walked_at
        """,
        nativeQuery = true,
    )
    fun upsert(
        @Param("id") id: Long,
        @Param("gridCell") gridCell: String,
        @Param("hourOfDay") hourOfDay: Int,
        @Param("dogId") dogId: Long,
        @Param("walkedAt") walkedAt: Instant,
    )
}
