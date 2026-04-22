package com.mungcle.walks.infrastructure.kafka

import com.mungcle.common.kafka.Topics
import com.mungcle.common.kafka.WalkExpiredEvent
import com.mungcle.walks.domain.port.out.WalkEventPublisherPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WalkExpiredEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, WalkExpiredEvent>,
) : WalkEventPublisherPort {

    override fun publishWalkExpired(walkId: Long, userId: Long) {
        val event = WalkExpiredEvent(walkId = walkId, userId = userId, occurredAt = Instant.now())
        kafkaTemplate.send(Topics.WALK_EXPIRED, walkId.toString(), event)
    }
}
