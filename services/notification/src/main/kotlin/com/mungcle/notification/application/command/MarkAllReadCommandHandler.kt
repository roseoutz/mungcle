package com.mungcle.notification.application.command

import com.mungcle.notification.domain.port.`in`.MarkAllReadUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MarkAllReadCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
) : MarkAllReadUseCase {

    @Transactional
    override fun execute(command: MarkAllReadUseCase.Command) {
        notificationRepository.markAllReadByUserId(command.userId)
    }
}
