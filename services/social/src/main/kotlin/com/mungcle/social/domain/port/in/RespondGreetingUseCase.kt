package com.mungcle.social.domain.port.`in`

import com.mungcle.social.domain.model.Greeting

interface RespondGreetingUseCase {
    fun execute(command: Command): Greeting

    data class Command(
        val greetingId: Long,
        val responderUserId: Long,
        val accept: Boolean,
    )
}
