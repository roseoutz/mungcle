package com.mungcle.social.domain.model

import com.mungcle.social.domain.exception.GreetingNotPendingException
import java.time.Instant

class Greeting(
    val id: Long = 0,
    val senderUserId: Long,
    val senderDogId: Long,
    val receiverUserId: Long,
    val receiverDogId: Long,
    val receiverWalkId: Long,
    status: GreetingStatus = GreetingStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    respondedAt: Instant? = null,
    expiresAt: Instant,
) {
    var status: GreetingStatus = status
        private set

    var respondedAt: Instant? = respondedAt
        private set

    var expiresAt: Instant = expiresAt
        private set

    /** PENDING → ACCEPTED 상태 전이. expiresAt을 30분 후로 갱신한다. */
    fun accept(now: Instant): Greeting {
        if (status != GreetingStatus.PENDING) throw GreetingNotPendingException(id)
        status = GreetingStatus.ACCEPTED
        respondedAt = now
        expiresAt = now.plusSeconds(1800)
        return this
    }

    /** PENDING 또는 ACCEPTED → EXPIRED 상태 전이. */
    fun expire(): Greeting {
        if (status != GreetingStatus.PENDING && status != GreetingStatus.ACCEPTED) throw GreetingNotPendingException(id)
        status = GreetingStatus.EXPIRED
        return this
    }

    fun canSendMessage(now: Instant): Boolean = status == GreetingStatus.ACCEPTED && now.isBefore(expiresAt)

    fun isPending(): Boolean = status == GreetingStatus.PENDING

    fun isAccepted(): Boolean = status == GreetingStatus.ACCEPTED

    fun isExpired(now: Instant): Boolean = now.isAfter(expiresAt)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Greeting) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "Greeting(id=$id, senderUserId=$senderUserId, receiverUserId=$receiverUserId, status=$status)"
}
