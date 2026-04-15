package com.mungcle.identity.domain.model

import java.time.Instant

/**
 * 차단 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class Block(
    val id: Long = 0,
    val blockerId: Long,
    val blockedId: Long,
    val createdAt: Instant = Instant.now()
) {
    init { require(blockerId != blockedId) { "자기 자신을 차단할 수 없습니다" } }
}
