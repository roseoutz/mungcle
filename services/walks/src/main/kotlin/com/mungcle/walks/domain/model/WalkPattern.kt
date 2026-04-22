package com.mungcle.walks.domain.model

import java.time.Instant

/**
 * 시간대 산책 패턴 도메인 모델.
 * 특정 그리드 셀에서 특정 시간대에 반려견이 얼마나 자주 산책하는지 집계한 데이터.
 */
data class WalkPattern(
    val id: Long = 0,
    val gridCell: String,
    val hourOfDay: Int,  // 0~23
    val dogId: Long,
    val walkCount: Int,
    val lastWalkedAt: Instant,
)
