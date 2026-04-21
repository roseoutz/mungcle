package com.mungcle.walks.domain.model

import java.time.Instant

/**
 * 산책 도메인 모델.
 * 순수 Kotlin 객체 -- 프레임워크 의존성 없음.
 */
data class Walk(
    val id: Long = 0,
    val dogId: Long,
    val userId: Long,
    val type: WalkType,
    val gridCell: GridCell,
    val status: WalkStatus = WalkStatus.ACTIVE,
    val startedAt: Instant = Instant.now(),
    val endsAt: Instant,
) {
    /** 만료 여부 확인 */
    fun isExpired(now: Instant): Boolean = now.isAfter(endsAt)

    /** OPEN 산책인지 확인 */
    fun isOpen(): Boolean = type == WalkType.OPEN

    /** 산책 종료 처리 -- 새 복사본 반환 (endsAt을 실제 종료 시각으로 업데이트) */
    fun end(now: Instant): Walk = copy(status = WalkStatus.ENDED, endsAt = now)
}
