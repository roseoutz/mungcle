package com.mungcle.identity.domain.port.`in`

import com.mungcle.identity.domain.model.Block

/**
 * 차단 목록 조회 유스케이스 포트.
 */
interface ListBlocksUseCase {
    /** userId가 차단한 목록 반환 */
    suspend fun execute(userId: Long): List<Block>
}
