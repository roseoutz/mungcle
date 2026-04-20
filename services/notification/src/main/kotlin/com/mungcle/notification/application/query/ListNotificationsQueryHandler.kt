package com.mungcle.notification.application.query

import com.mungcle.notification.application.dto.NotificationResult
import com.mungcle.notification.domain.port.`in`.ListNotificationsUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListNotificationsQueryHandler(
    private val notificationRepository: NotificationRepositoryPort,
) : ListNotificationsUseCase {

    override suspend fun execute(userId: Long, cursor: Long?, limit: Int): List<NotificationResult> {
        val notifications = notificationRepository.findByUserIdWithCursor(userId, cursor, limit)
        return notifications.map(NotificationResult::from)
    }
}
