package com.mungcle.notification.infrastructure.kafka

import com.mungcle.common.kafka.GreetingAcceptedEvent
import com.mungcle.common.kafka.GreetingCreatedEvent
import com.mungcle.common.kafka.GreetingExpiredEvent
import com.mungcle.common.kafka.MessageSentEvent
import com.mungcle.common.kafka.Topics
import com.mungcle.common.kafka.WalkExpiredEvent
import com.mungcle.notification.application.dto.CreateNotificationCommand
import com.mungcle.notification.domain.model.NotificationType
import com.mungcle.notification.domain.port.`in`.CreateNotificationUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationKafkaConsumer(
    private val createNotificationUseCase: CreateNotificationUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [Topics.GREETING_CREATED])
    suspend fun onGreetingCreated(event: GreetingCreatedEvent) {
        log.info("인사 생성 이벤트 수신: greetingId={}, eventId={}", event.greetingId, event.eventId)
        createNotificationUseCase.execute(
            CreateNotificationCommand(
                userId = event.receiverUserId,
                type = NotificationType.GREETING_RECEIVED,
                payload = mapOf(
                    "greetingId" to event.greetingId.toString(),
                    "fromUserId" to event.senderUserId.toString(),
                ),
                eventId = event.eventId,
            )
        )
    }

    @KafkaListener(topics = [Topics.GREETING_ACCEPTED])
    suspend fun onGreetingAccepted(event: GreetingAcceptedEvent) {
        log.info("인사 수락 이벤트 수신: greetingId={}, eventId={}", event.greetingId, event.eventId)
        createNotificationUseCase.execute(
            CreateNotificationCommand(
                userId = event.senderUserId,
                type = NotificationType.GREETING_ACCEPTED,
                payload = mapOf(
                    "greetingId" to event.greetingId.toString(),
                    "fromUserId" to event.receiverUserId.toString(),
                ),
                eventId = event.eventId,
            )
        )
    }

    @KafkaListener(topics = [Topics.MESSAGE_SENT])
    suspend fun onMessageSent(event: MessageSentEvent) {
        log.info("메시지 전송 이벤트 수신: greetingId={}, eventId={}", event.greetingId, event.eventId)
        createNotificationUseCase.execute(
            CreateNotificationCommand(
                userId = event.receiverUserId,
                type = NotificationType.MESSAGE_RECEIVED,
                payload = mapOf(
                    "greetingId" to event.greetingId.toString(),
                    "fromUserId" to event.senderUserId.toString(),
                ),
                eventId = event.eventId,
            )
        )
    }

    @KafkaListener(topics = [Topics.WALK_EXPIRED])
    suspend fun onWalkExpired(event: WalkExpiredEvent) {
        log.info("산책 만료 이벤트 수신: walkId={}, eventId={}", event.walkId, event.eventId)
        createNotificationUseCase.execute(
            CreateNotificationCommand(
                userId = event.userId,
                type = NotificationType.WALK_EXPIRED,
                payload = mapOf(
                    "walkId" to event.walkId.toString(),
                ),
                eventId = event.eventId,
            )
        )
    }

    @KafkaListener(topics = [Topics.GREETING_EXPIRED])
    fun onGreetingExpired(event: GreetingExpiredEvent) {
        log.info("인사 만료 이벤트 수신 (알림 생성 안 함): greetingId={}, eventId={}", event.greetingId, event.eventId)
    }
}
