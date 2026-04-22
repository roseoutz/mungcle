package com.mungcle.social.domain.port.`in`

import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus

interface ListGreetingsUseCase {
    fun execute(query: Query): List<Greeting>

    data class Query(
        val userId: Long,
        val statusFilter: GreetingStatus? = null,
        val isSender: Boolean? = null,
    )
}
