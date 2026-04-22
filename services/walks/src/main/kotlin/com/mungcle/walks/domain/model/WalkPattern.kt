package com.mungcle.walks.domain.model

import java.time.Instant

/**
 * 시간대 산책 패턴 도메인 모델 (Rich Domain Model).
 * 특정 그리드 셀에서 특정 시간대에 반려견이 얼마나 자주 산책하는지 집계한 데이터.
 * 식별자(id)로 동등성을 판단하는 Entity.
 */
class WalkPattern(
    val id: Long = 0,
    val gridCell: String,
    val hourOfDay: Int,  // 0~23
    val dogId: Long,
    walkCount: Int,
    lastWalkedAt: Instant,
) {
    var walkCount: Int = walkCount
        private set

    var lastWalkedAt: Instant = lastWalkedAt
        private set

    /**
     * 산책 횟수를 1 증가시키고 마지막 산책 시각을 갱신한다.
     */
    fun incrementCount(walkedAt: Instant) {
        walkCount += 1
        lastWalkedAt = walkedAt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WalkPattern) return false
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        if (id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)
}
