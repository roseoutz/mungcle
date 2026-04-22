package com.mungcle.walks.domain.port.out

import com.mungcle.walks.domain.model.WalkPattern
import java.time.Instant

/**
 * 산책 패턴 저장소 포트.
 */
interface WalkPatternRepositoryPort {
    /**
     * 지정된 그리드 셀과 시간 범위에 해당하는 패턴 목록을 반환한다.
     * @param gridCells 조회 대상 그리드 셀 목록
     * @param hourRange 조회 대상 시간 범위 (0~23)
     * @return 해당 조건의 WalkPattern 목록
     */
    fun findByGridCellsAndHourRange(gridCells: List<String>, hourRange: IntRange): List<WalkPattern>

    /**
     * 특정 그리드 셀, 시간대, 반려견의 패턴을 upsert한다.
     * 이미 존재하면 walk_count를 증가하고 last_walked_at을 갱신한다.
     * @param gridCell 그리드 셀 ID
     * @param hourOfDay 산책 시각 (0~23)
     * @param dogId 반려견 ID
     * @param walkedAt 산책 시각 (Instant)
     */
    fun upsert(gridCell: String, hourOfDay: Int, dogId: Long, walkedAt: Instant)
}
