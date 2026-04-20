package com.mungcle.notification.domain.port.`in`

/**
 * 단건 알림 읽음 처리 유스케이스 포트.
 */
interface MarkReadUseCase {
    suspend fun execute(notificationId: Long, userId: Long)
}
