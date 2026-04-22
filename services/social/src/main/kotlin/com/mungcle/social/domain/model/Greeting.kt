package com.mungcle.social.domain.model

import java.time.Instant

data class Greeting(
    val id: Long = 0,
    val senderUserId: Long,
    val senderDogId: Long,
    val receiverUserId: Long,
    val receiverDogId: Long,
    val receiverWalkId: Long,
    val status: GreetingStatus = GreetingStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val respondedAt: Instant? = null,
    val expiresAt: Instant,
) {
    fun accept(now: Instant): Greeting {
        check(status == GreetingStatus.PENDING) { "Only PENDING greetings can be accepted" }
        return copy(
            status = GreetingStatus.ACCEPTED,
            respondedAt = now,
            expiresAt = now.plusSeconds(1800),
        )
    }

    fun expire(): Greeting = copy(status = GreetingStatus.EXPIRED)

    fun isPending(): Boolean = status == GreetingStatus.PENDING

    fun isAccepted(): Boolean = status == GreetingStatus.ACCEPTED

    fun isExpired(now: Instant): Boolean = now.isAfter(expiresAt)
}
