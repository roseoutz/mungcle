package com.mungcle.identity.domain.port.`in`

/**
 * 양방향 차단된 사용자 ID 목록 조회 유스케이스 포트.
 */
interface GetBlockedUserIdsUseCase {
    /** userId가 차단했거나 차단당한 사용자 ID 목록 반환 (양방향) */
    suspend fun execute(userId: Long): List<Long>
}
