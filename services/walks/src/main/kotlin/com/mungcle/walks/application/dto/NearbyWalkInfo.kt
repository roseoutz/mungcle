package com.mungcle.walks.application.dto

import java.time.Instant

data class NearbyWalkInfo(
    val walkId: Long,
    val dogId: Long,
    val userId: Long,
    val gridDistance: Int,
    val startedAt: Instant,
)
