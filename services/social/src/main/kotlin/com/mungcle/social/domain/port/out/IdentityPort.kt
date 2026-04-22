package com.mungcle.social.domain.port.out

/**
 * Identity 서비스와의 통신 포트.
 * 차단 여부 등 identity 관련 기능을 제공한다.
 */
interface IdentityPort {
    /**
     * 두 사용자 사이에 차단 관계가 있는지 확인한다.
     * @return 한쪽이라도 상대를 차단했으면 true
     */
    suspend fun isBlocked(userId1: Long, userId2: Long): Boolean
}
