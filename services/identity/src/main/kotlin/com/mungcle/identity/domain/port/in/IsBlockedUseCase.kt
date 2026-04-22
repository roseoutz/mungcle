package com.mungcle.identity.domain.port.`in`

/**
 * 차단 여부 확인 유스케이스 포트.
 */
interface IsBlockedUseCase {
    /** userIdA와 userIdB 사이에 양방향 차단이 있으면 true */
    suspend fun execute(userIdA: Long, userIdB: Long): Boolean
}
