package com.mungcle.notification.application.command

import com.mungcle.notification.application.dto.CreateNotificationCommand
import com.mungcle.notification.application.dto.NotificationResult
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushSenderPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateNotificationCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
    private val pushSender: PushSenderPort,
) : CreateNotificationUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override suspend fun execute(command: CreateNotificationCommand): NotificationResult {
        // idempotent: 이미 처리된 이벤트는 스킵
        if (notificationRepository.existsByEventId(command.eventId)) {
            log.debug("이미 처리된 이벤트 스킵: {}", command.eventId)
            // 이미 처리된 경우에도 결과를 반환해야 하므로 더미 반환
            return NotificationResult(
                id = 0,
                userId = command.userId,
                type = command.type,
                payload = command.payload,
                isRead = false,
                createdAt = java.time.Instant.now(),
            )
        }

        val notification = Notification(
            userId = command.userId,
            type = command.type,
            payload = command.payload,
        )

        val saved = notificationRepository.save(notification)
        notificationRepository.saveEventId(command.eventId)

        // FCM 발송 시도 — 실패해도 DB 저장은 유지
        try {
            pushSender.sendPush(saved)
        } catch (e: Exception) {
            log.warn("푸시 발송 실패 (notificationId={}): {}", saved.id, e.message)
        }

        return NotificationResult.from(saved)
    }
}
