package com.mungcle.notification.application.command

import com.mungcle.notification.application.dto.CreateNotificationCommand
import com.mungcle.notification.application.dto.NotificationResult
import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.mungcle.notification.domain.port.out.IdentityPort
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushSenderPort
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateNotificationCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
    private val pushSender: PushSenderPort,
    private val identityPort: IdentityPort,
) : CreateNotificationUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override suspend fun execute(command: CreateNotificationCommand): NotificationResult {
        // 1. eventId 저장 시도 (unique constraint로 원자적 중복 방지)
        try {
            notificationRepository.saveEventId(command.eventId)
        } catch (e: DataIntegrityViolationException) {
            log.debug("중복 이벤트 무시: {}", command.eventId)
            return NotificationResult(
                id = 0,
                userId = command.userId,
                type = command.type,
                payload = command.payload,
                isRead = false,
                createdAt = java.time.Instant.now(),
            )
        }

        // 2. 알림 저장
        val notification = Notification(
            userId = command.userId,
            type = command.type,
            payload = command.payload,
        )
        val saved = notificationRepository.save(notification)

        // 3. pushToken 조회 후 FCM 발송 시도 — 실패해도 DB 저장은 유지
        try {
            val pushToken = identityPort.getPushToken(command.userId)
            if (pushToken != null) {
                pushSender.sendPush(saved, pushToken)
            } else {
                log.debug("pushToken 없음, 푸시 생략 (userId={})", command.userId)
            }
        } catch (e: Exception) {
            log.warn("푸시 발송 실패 (notificationId={}): {}", saved.id, e.message)
        }

        return NotificationResult.from(saved)
    }
}
