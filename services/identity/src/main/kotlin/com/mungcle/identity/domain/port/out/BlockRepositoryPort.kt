package com.mungcle.identity.domain.port.out

import com.mungcle.identity.domain.model.Block

/**
 * 차단 저장소 아웃바운드 포트.
 */
interface BlockRepositoryPort {
    /** 차단 저장 */
    fun save(block: Block): Block

    /** 차단 삭제 */
    fun delete(blockerId: Long, blockedId: Long)

    /** 차단자 ID로 차단 목록 조회 */
    fun findByBlockerId(blockerId: Long): List<Block>

    /** 양방향 차단된 사용자 ID 목록 조회 */
    fun findBlockedUserIds(userId: Long): List<Long>

    /** 양방향 차단 여부 확인 */
    fun isBlocked(userIdA: Long, userIdB: Long): Boolean

    /** 특정 방향의 차단 존재 여부 확인 */
    fun existsByBlockerAndBlocked(blockerId: Long, blockedId: Long): Boolean
}
