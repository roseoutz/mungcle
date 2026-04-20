package com.mungcle.notification.domain.port.`in`

import com.mungcle.notification.application.dto.NotificationResult

/**
 * 알림 목록 조회 유스케이스 포트.
 */
interface ListNotificationsUseCase {
    suspend fun execute(userId: Long, cursor: Long?, limit: Int): List<NotificationResult>
}
