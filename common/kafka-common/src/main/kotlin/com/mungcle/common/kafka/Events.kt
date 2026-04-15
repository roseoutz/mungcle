package com.mungcle.common.kafka

import java.time.Instant

/**
 * 서비스간 공유 Kafka 이벤트 DTO.
 * 모든 이벤트는 eventId로 idempotent consumer 처리 가능.
 */
data class GreetingCreatedEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val greetingId: Long,
    val senderUserId: Long,
    val receiverUserId: Long,
    val occurredAt: Instant = Instant.now(),
)

data class GreetingAcceptedEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val greetingId: Long,
    val senderUserId: Long,
    val receiverUserId: Long,
    val occurredAt: Instant = Instant.now(),
)

data class GreetingExpiredEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val greetingId: Long,
    val expiredType: String, // "PENDING" or "ACCEPTED"
    val occurredAt: Instant = Instant.now(),
)

data class MessageSentEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val messageId: Long,
    val greetingId: Long,
    val senderUserId: Long,
    val receiverUserId: Long,
    val occurredAt: Instant = Instant.now(),
)

data class WalkExpiredEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val walkId: Long,
    val userId: Long,
    val occurredAt: Instant = Instant.now(),
)
