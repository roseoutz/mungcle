package com.mungcle.identity.domain.model

import java.time.Instant

/**
 * 신고 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class Report(
    val id: Long = 0,
    val reporterId: Long,
    val reportedId: Long,
    val reason: String,
    val createdAt: Instant = Instant.now()
) {
    init {
        require(reporterId != reportedId) { "자기 자신을 신고할 수 없습니다" }
        require(reason.length in 1..500) { "신고 사유는 1~500자여야 합니다" }
    }
}
