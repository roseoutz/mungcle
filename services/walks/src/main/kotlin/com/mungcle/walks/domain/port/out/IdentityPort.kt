package com.mungcle.walks.domain.port.out

/**
 * Identity 서비스와의 통신을 위한 포트.
 * 차단 목록 조회 등 identity 관련 기능을 제공한다.
 */
interface IdentityPort {
    suspend fun getBlockedUserIds(userId: Long): List<Long>
}
