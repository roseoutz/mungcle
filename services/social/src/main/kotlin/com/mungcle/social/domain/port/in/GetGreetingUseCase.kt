package com.mungcle.social.domain.port.`in`

import com.mungcle.social.domain.model.Greeting

interface GetGreetingUseCase {
    fun execute(query: Query): Greeting

    data class Query(
        val greetingId: Long,
        val userId: Long,
    )
}
