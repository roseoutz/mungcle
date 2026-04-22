package com.mungcle.walks.domain.port.`in`

import com.mungcle.walks.domain.model.WalkPattern

/**
 * 인근 그리드 셀의 시간대별 산책 패턴 조회 유즈케이스.
 */
interface GetNearbyPatternsUseCase {
    /**
     * 인근 셀의 패턴 목록을 반환한다.
     * @param query 조회 조건 (gridCell, userId, blockedUserIds)
     * @return walkCount 기준 상위 10개의 WalkPattern 목록
     */
    suspend fun execute(query: Query): List<WalkPattern>

    data class Query(
        val gridCell: String,
        val userId: Long,
        val blockedUserIds: List<Long>,
    )
}
