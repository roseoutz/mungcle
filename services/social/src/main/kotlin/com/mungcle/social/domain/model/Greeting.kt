package com.mungcle.social.domain.model

import com.mungcle.social.domain.exception.GreetingNotPendingException
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
        if (status != GreetingStatus.PENDING) throw GreetingNotPendingException(id)
        return copy(
            status = GreetingStatus.ACCEPTED,
            respondedAt = now,
            expiresAt = now.plusSeconds(1800),
        )
    }

    fun expire(): Greeting {
        if (status != GreetingStatus.PENDING && status != GreetingStatus.ACCEPTED) throw GreetingNotPendingException(id)
        return copy(status = GreetingStatus.EXPIRED)
    }

    fun canSendMessage(now: Instant): Boolean = status == GreetingStatus.ACCEPTED && now.isBefore(expiresAt)

    fun isPending(): Boolean = status == GreetingStatus.PENDING

    fun isAccepted(): Boolean = status == GreetingStatus.ACCEPTED

    fun isExpired(now: Instant): Boolean = now.isAfter(expiresAt)
}
