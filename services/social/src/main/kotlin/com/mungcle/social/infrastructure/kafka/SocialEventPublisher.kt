package com.mungcle.social.infrastructure.kafka

import com.mungcle.common.kafka.GreetingAcceptedEvent
import com.mungcle.common.kafka.GreetingCreatedEvent
import com.mungcle.common.kafka.GreetingExpiredEvent
import com.mungcle.common.kafka.MessageSentEvent
import com.mungcle.common.kafka.Topics
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.model.Message
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class SocialEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) : SocialEventPublisherPort {

    override fun publishGreetingCreated(greeting: Greeting) {
        val event = GreetingCreatedEvent(
            greetingId = greeting.id,
            senderUserId = greeting.senderUserId,
            receiverUserId = greeting.receiverUserId,
        )
        kafkaTemplate.send(Topics.GREETING_CREATED, greeting.id.toString(), event)
    }

    override fun publishGreetingAccepted(greeting: Greeting) {
        val event = GreetingAcceptedEvent(
            greetingId = greeting.id,
            senderUserId = greeting.senderUserId,
            receiverUserId = greeting.receiverUserId,
        )
        kafkaTemplate.send(Topics.GREETING_ACCEPTED, greeting.id.toString(), event)
    }

    override fun publishMessageSent(message: Message, receiverUserId: Long) {
        val event = MessageSentEvent(
            messageId = message.id,
            greetingId = message.greetingId,
            senderUserId = message.senderUserId,
            receiverUserId = receiverUserId,
        )
        kafkaTemplate.send(Topics.MESSAGE_SENT, message.id.toString(), event)
    }

    override fun publishGreetingExpired(greeting: Greeting) {
        val expiredType = when (greeting.status) {
            GreetingStatus.EXPIRED -> if (greeting.respondedAt != null) "ACCEPTED" else "PENDING"
            else -> "PENDING"
        }
        val event = GreetingExpiredEvent(
            greetingId = greeting.id,
            expiredType = expiredType,
        )
        kafkaTemplate.send(Topics.GREETING_EXPIRED, greeting.id.toString(), event)
    }
}
