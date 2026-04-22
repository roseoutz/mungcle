package com.mungcle.notification.application.query

import com.mungcle.notification.domain.port.`in`.ListNotificationsUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListNotificationsQueryHandler(
    private val notificationRepository: NotificationRepositoryPort,
) : ListNotificationsUseCase {

    override fun execute(query: ListNotificationsUseCase.Query): ListNotificationsUseCase.Result {
        val notifications = notificationRepository.findByUserId(
            userId = query.userId,
            cursor = query.cursor,
            limit = query.limit + 1,
        )

        val hasNext = notifications.size > query.limit
        val page = if (hasNext) notifications.dropLast(1) else notifications
        val nextCursor = if (hasNext) page.last().id else null

        return ListNotificationsUseCase.Result(
            notifications = page,
            nextCursor = nextCursor,
        )
    }
}
