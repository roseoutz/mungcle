package com.mungcle.notification.application.command

import com.mungcle.notification.domain.model.Notification
import com.mungcle.notification.domain.model.NotificationPushTemplate
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.mungcle.notification.domain.port.out.NotificationRepositoryPort
import com.mungcle.notification.domain.port.out.PushNotificationPort
import com.mungcle.notification.domain.port.out.UserPushTokenPort
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateNotificationCommandHandler(
    private val notificationRepository: NotificationRepositoryPort,
    private val pushNotificationPort: PushNotificationPort,
    private val userPushTokenPort: UserPushTokenPort,
) : CreateNotificationUseCase {

    private val log = LoggerFactory.getLogger(CreateNotificationCommandHandler::class.java)

    @Transactional
    override fun execute(command: CreateNotificationUseCase.Command): Notification {
        val notification = Notification(
            userId = command.userId,
            type = command.type,
            payloadJson = command.payloadJson,
        )
        val saved = notificationRepository.save(notification)

        sendPush(saved)

        return saved
    }

    private fun sendPush(notification: Notification) {
        val pushToken = runBlocking {
            userPushTokenPort.getPushToken(notification.userId)
        }
        if (pushToken == null) {
            log.debug("pushToken 없음 — 인앱 알림만 저장: userId={}", notification.userId)
            return
        }
        val content = NotificationPushTemplate.of(notification.type)
        pushNotificationPort.send(
            pushToken = pushToken,
            title = content.title,
            body = content.body,
            data = mapOf(
                "notificationId" to notification.id.toString(),
                "type" to notification.type.name,
            ),
        )
    }
}
