package com.mungcle.social.application.query

import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.port.`in`.GetGreetingUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetGreetingQueryHandler(
    private val greetingRepository: GreetingRepositoryPort,
) : GetGreetingUseCase {

    override fun execute(query: GetGreetingUseCase.Query): Greeting {
        val greeting = greetingRepository.findById(query.greetingId)
            ?: throw GreetingNotFoundException(query.greetingId)

        if (greeting.senderUserId != query.userId && greeting.receiverUserId != query.userId) {
            throw GreetingAccessDeniedException(query.greetingId)
        }

        return greeting
    }
}
