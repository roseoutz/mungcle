package com.mungcle.walks.domain.model

import com.mungcle.common.domain.GridCell
import java.time.Instant

/**
 * 산책 도메인 모델 (Rich Domain Model).
 * 순수 Kotlin 객체 -- 프레임워크 의존성 없음.
 * 식별자(id)로 동등성을 판단하는 Entity.
 */
class Walk(
    val id: Long = 0,
    val dogId: Long,
    val userId: Long,
    val type: WalkType,
    val gridCell: GridCell,
    status: WalkStatus = WalkStatus.ACTIVE,
    val startedAt: Instant = Instant.now(),
    endsAt: Instant,
) {
    var status: WalkStatus = status
        private set

    var endsAt: Instant = endsAt
        private set

    /** 만료 여부 확인 */
    fun isExpired(now: Instant): Boolean = now.isAfter(endsAt)

    /** OPEN 타입이고 ACTIVE 상태인 산책인지 확인 */
    fun isOpen(): Boolean = type == WalkType.OPEN && status == WalkStatus.ACTIVE

    /**
     * 산책 종료 처리 -- 상태를 ENDED로 변경하고 종료 시각을 실제 시각으로 업데이트.
     * @return this (메서드 체이닝 지원)
     */
    fun end(now: Instant): Walk {
        status = WalkStatus.ENDED
        endsAt = now
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Walk) return false
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        if (id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)

    override fun toString(): String =
        "Walk(id=$id, dogId=$dogId, userId=$userId, type=$type, status=$status, gridCell=$gridCell)"
}
