package com.mungcle.social.domain.port.`in`

import com.mungcle.social.domain.model.Message

interface ListMessagesUseCase {
    fun execute(query: Query): List<Message>

    data class Query(
        val greetingId: Long,
        val userId: Long,
    )
}
