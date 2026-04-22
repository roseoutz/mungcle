package com.mungcle.social.domain.model

import java.time.Instant

class Message(
    val id: Long = 0,
    val greetingId: Long,
    val senderUserId: Long,
    val body: String,
    val createdAt: Instant = Instant.now(),
) {
    init {
        // 메시지 본문은 1자 이상 140자 이하여야 한다
        require(body.isNotBlank() && body.length <= 140) { "메시지 본문은 1~140자여야 합니다" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        if (id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)

    override fun toString(): String =
        "Message(id=$id, greetingId=$greetingId, senderUserId=$senderUserId)"
}
