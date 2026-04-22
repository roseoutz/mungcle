package com.mungcle.social.domain.port.`in`

import com.mungcle.social.domain.model.Message

interface SendMessageUseCase {
    fun execute(command: Command): Message

    data class Command(
        val greetingId: Long,
        val senderUserId: Long,
        val body: String,
    )
}
