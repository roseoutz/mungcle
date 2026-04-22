package com.mungcle.social.application.query

import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.port.`in`.ListGreetingsUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListGreetingsQueryHandler(
    private val greetingRepository: GreetingRepositoryPort,
) : ListGreetingsUseCase {

    override fun execute(query: ListGreetingsUseCase.Query): List<Greeting> {
        return greetingRepository.findByUserId(
            userId = query.userId,
            statusFilter = query.statusFilter,
            isSender = query.isSender,
        )
    }
}
