package com.mungcle.notification.domain.port.`in`

/**
 * 전체 알림 읽음 처리 유스케이스 포트.
 */
interface MarkAllReadUseCase {
    suspend fun execute(userId: Long)
}
