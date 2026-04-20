package com.mungcle.notification.domain.port.`in`

import com.mungcle.notification.application.dto.CreateNotificationCommand
import com.mungcle.notification.application.dto.NotificationResult

/**
 * 알림 생성 유스케이스 포트.
 */
interface CreateNotificationUseCase {
    suspend fun execute(command: CreateNotificationCommand): NotificationResult
}
