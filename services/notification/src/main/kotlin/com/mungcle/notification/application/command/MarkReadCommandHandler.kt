package com.mungcle.notification.application.command

import com.mungcle.notification.domain.exception.NotificationNotFoundException
import com.mungcle.notification.domain.exception.NotificationNotOwnedException
import com.mungcle.notification.domain.port.`in`.MarkReadUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class MarkReadCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
) : MarkReadUseCase {

    @Transactional
    override suspend fun execute(notificationId: Long, userId: Long) {
        val notification = notificationRepository.findById(notificationId)
            ?: throw NotificationNotFoundException(notificationId)

        if (notification.userId != userId) {
            throw NotificationNotOwnedException(notificationId, userId)
        }

        if (notification.isRead()) return

        val marked = notification.markRead(Instant.now())
        notificationRepository.save(marked)
    }
}
