package com.mungcle.notification.application.command

import com.mungcle.notification.domain.event.NotificationCreatedEvent
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateNotificationCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
    private val eventPublisher: ApplicationEventPublisher,
) : CreateNotificationUseCase {

    @Transactional
    override fun execute(command: CreateNotificationUseCase.Command): Notification {
        val notification = Notification(
            userId = command.userId,
            type = command.type,
            payloadJson = command.payloadJson,
        )
        val saved = notificationRepository.save(notification)

        eventPublisher.publishEvent(NotificationCreatedEvent(saved))

        return saved
    }
}
