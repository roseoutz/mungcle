package com.mungcle.walks.domain.port.out

/**
 * 산책 관련 Kafka 이벤트 발행 포트.
 */
interface WalkEventPublisherPort {
    /**
     * 산책 만료 이벤트를 발행한다.
     * @param walkId 만료된 산책 ID
     * @param userId 산책 소유 사용자 ID
     */
    fun publishWalkExpired(walkId: Long, userId: Long)
}
