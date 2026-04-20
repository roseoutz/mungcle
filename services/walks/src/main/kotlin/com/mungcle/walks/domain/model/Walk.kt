package com.mungcle.walks.domain.model

import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import java.time.Instant

/**
 * 산책 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 */
data class Walk(
    val id: Long = 0,
    val dogId: Long,
    val userId: Long,
    val type: WalkType,
    val gridCell: String,
    val status: WalkStatus = WalkStatus.ACTIVE,
    val startedAt: Instant = Instant.now(),
    val endsAt: Instant,
    val endedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    /** 만료 여부 확인 */
    fun isExpired(now: Instant): Boolean = now.isAfter(endsAt)

    /** OPEN 산책인지 확인 */
    fun isOpen(): Boolean = type == WalkType.OPEN

    /** 산책 종료 처리. 이미 종료된 경우 예외 */
    fun end(now: Instant): Walk {
        if (status == WalkStatus.ENDED) {
            throw WalkAlreadyEndedException(id)
        }
        return copy(status = WalkStatus.ENDED, endedAt = now)
    }
}
