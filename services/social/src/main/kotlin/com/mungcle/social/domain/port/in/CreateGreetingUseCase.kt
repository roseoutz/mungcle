package com.mungcle.social.domain.port.`in`

import com.mungcle.social.domain.model.Greeting

interface CreateGreetingUseCase {
    suspend fun execute(command: Command): Greeting

    data class Command(
        val senderUserId: Long,
        val senderDogId: Long,
        val receiverWalkId: Long,
    )
}
