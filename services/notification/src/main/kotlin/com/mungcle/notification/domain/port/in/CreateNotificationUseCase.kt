package com.mungcle.notification.domain.port.`in`

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationType

interface CreateNotificationUseCase {
    data class Command(
        val userId: Long,
        val type: NotificationType,
        val payloadJson: String,
    )

    /** 새 알림을 생성한다 */
    fun execute(command: Command): Notification
}
