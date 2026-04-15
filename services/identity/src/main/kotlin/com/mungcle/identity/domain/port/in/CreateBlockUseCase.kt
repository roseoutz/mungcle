package com.mungcle.identity.domain.port.`in`

/**
 * 차단 생성 유스케이스 포트.
 */
interface CreateBlockUseCase {
    /** blockerId가 blockedId를 차단. 이미 차단된 경우 멱등 처리 */
    suspend fun execute(blockerId: Long, blockedId: Long)
}
