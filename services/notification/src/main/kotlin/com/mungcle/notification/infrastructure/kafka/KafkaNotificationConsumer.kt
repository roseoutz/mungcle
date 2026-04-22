package com.mungcle.notification.infrastructure.kafka

import com.mungcle.common.kafka.GreetingAcceptedEvent
import com.mungcle.common.kafka.GreetingCreatedEvent
import com.mungcle.common.kafka.MessageSentEvent
import com.mungcle.common.kafka.Topics
import com.mungcle.common.kafka.WalkExpiredEvent
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class KafkaNotificationConsumer(
    private val createNotificationUseCase: CreateNotificationUseCase,
    private val objectMapper: ObjectMapper,
) {

    private val log = LoggerFactory.getLogger(KafkaNotificationConsumer::class.java)

    @KafkaListener(topics = [Topics.GREETING_CREATED], groupId = "notification-service")
    fun onGreetingCreated(event: GreetingCreatedEvent) {
        log.info("greeting.created 수신: greetingId={}, receiver={}", event.greetingId, event.receiverUserId)
        createNotificationUseCase.execute(
            CreateNotificationUseCase.Command(
                userId = event.receiverUserId,
                type = NotificationType.GREETING_RECEIVED,
                payloadJson = objectMapper.writeValueAsString(
                    mapOf("greetingId" to event.greetingId, "senderUserId" to event.senderUserId)
                ),
            )
        )
    }

    @KafkaListener(topics = [Topics.GREETING_ACCEPTED], groupId = "notification-service")
    fun onGreetingAccepted(event: GreetingAcceptedEvent) {
        log.info("greeting.accepted 수신: greetingId={}, sender={}", event.greetingId, event.senderUserId)
        createNotificationUseCase.execute(
            CreateNotificationUseCase.Command(
                userId = event.senderUserId,
                type = NotificationType.GREETING_ACCEPTED,
                payloadJson = objectMapper.writeValueAsString(
                    mapOf("greetingId" to event.greetingId, "receiverUserId" to event.receiverUserId)
                ),
            )
        )
    }

    @KafkaListener(topics = [Topics.MESSAGE_SENT], groupId = "notification-service")
    fun onMessageSent(event: MessageSentEvent) {
        log.info("message.sent 수신: messageId={}, receiver={}", event.messageId, event.receiverUserId)
        createNotificationUseCase.execute(
            CreateNotificationUseCase.Command(
                userId = event.receiverUserId,
                type = NotificationType.MESSAGE_RECEIVED,
                payloadJson = objectMapper.writeValueAsString(
                    mapOf(
                        "messageId" to event.messageId,
                        "greetingId" to event.greetingId,
                        "senderUserId" to event.senderUserId,
                    )
                ),
            )
        )
    }

    @KafkaListener(topics = [Topics.WALK_EXPIRED], groupId = "notification-service")
    fun onWalkExpired(event: WalkExpiredEvent) {
        log.info("walk.expired 수신: walkId={}, userId={}", event.walkId, event.userId)
        createNotificationUseCase.execute(
            CreateNotificationUseCase.Command(
                userId = event.userId,
                type = NotificationType.WALK_EXPIRED,
                payloadJson = objectMapper.writeValueAsString(
                    mapOf("walkId" to event.walkId)
                ),
            )
        )
    }
}
