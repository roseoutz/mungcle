package com.mungcle.social.domain.model

import java.time.Instant

data class Message(
    val id: Long = 0,
    val greetingId: Long,
    val senderUserId: Long,
    val body: String,
    val createdAt: Instant = Instant.now(),
)
