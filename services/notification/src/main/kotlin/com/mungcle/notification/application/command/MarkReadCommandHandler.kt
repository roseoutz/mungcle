package com.mungcle.notification.application.command

import com.mungcle.notification.domain.exception.NotificationNotFoundException
import com.mungcle.notification.domain.exception.NotificationNotOwnedException
import com.mungcle.notification.domain.port.`in`.MarkReadUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MarkReadCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
) : MarkReadUseCase {

    @Transactional
    override fun execute(command: MarkReadUseCase.Command) {
        val notification = notificationRepository.findById(command.notificationId)
            ?: throw NotificationNotFoundException(command.notificationId)

        if (notification.userId != command.userId) {
            throw NotificationNotOwnedException(command.notificationId, command.userId)
        }

        notificationRepository.markRead(command.notificationId)
    }
}
