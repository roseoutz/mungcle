package com.mungcle.walks.infrastructure.persistence

import com.mungcle.walks.domain.model.WalkPattern
import com.mungcle.walks.domain.port.out.WalkPatternRepositoryPort
import io.hypersistence.tsid.TSID
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class WalkPatternRepositoryAdapter(
    private val springDataRepository: WalkPatternSpringDataRepository,
) : WalkPatternRepositoryPort {

    override fun findByGridCellsAndHourRange(gridCells: List<String>, hourRange: IntRange): List<WalkPattern> =
        springDataRepository.findByGridCellsAndHourRange(gridCells, hourRange.first, hourRange.last)
            .map(WalkPatternMapper::toDomain)

    override fun upsert(gridCell: String, hourOfDay: Int, dogId: Long, walkedAt: Instant) {
        // ON CONFLICT handles duplicate (grid_cell, hour_of_day, dog_id) — ID is only used for new inserts
        val id = TSID.Factory.getTsid().toLong()
        springDataRepository.upsert(id, gridCell, hourOfDay, dogId, walkedAt)
    }
}
