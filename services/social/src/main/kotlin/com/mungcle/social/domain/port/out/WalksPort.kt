package com.mungcle.social.domain.port.out

/**
 * Walks 서비스와의 통신 포트.
 * 산책 정보 조회 기능을 제공한다.
 */
interface WalksPort {
    /**
     * 산책 ID로 산책 참여자 정보를 조회한다.
     * @throws com.mungcle.social.domain.exception.GreetingNotFoundException 산책을 찾을 수 없을 때
     */
    suspend fun getWalk(walkId: Long): WalkInfo

    data class WalkInfo(
        val walkId: Long,
        val userId: Long,
        val dogId: Long,
    )
}
