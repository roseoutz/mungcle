package com.mungcle.common.kafka

/**
 * Kafka 토픽 상수.
 * 네이밍: {domain}.{event}
 */
object Topics {
    const val GREETING_CREATED = "greeting.created"
    const val GREETING_ACCEPTED = "greeting.accepted"
    const val GREETING_EXPIRED = "greeting.expired"
    const val MESSAGE_SENT = "message.sent"
    const val WALK_EXPIRED = "walk.expired"
}
