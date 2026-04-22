package com.mungcle.identity.domain.port.`in`

/**
 * 차단 해제 유스케이스 포트.
 */
interface DeleteBlockUseCase {
    /** blockerId와 blockedId 간의 차단 해제 */
    suspend fun execute(blockerId: Long, blockedId: Long)
}
