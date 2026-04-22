package com.mungcle.social.infrastructure.kafka

import com.mungcle.common.kafka.GreetingAcceptedEvent
import com.mungcle.common.kafka.GreetingCreatedEvent
import com.mungcle.common.kafka.Topics
import com.mungcle.social.domain.model.Greeting
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
}
