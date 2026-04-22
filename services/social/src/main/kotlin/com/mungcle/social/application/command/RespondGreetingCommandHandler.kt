package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingExpiredException
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.port.`in`.RespondGreetingUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RespondGreetingCommandHandler(
    private val greetingRepository: GreetingRepositoryPort,
    private val eventPublisher: SocialEventPublisherPort,
) : RespondGreetingUseCase {

    @Transactional
    override fun execute(command: RespondGreetingUseCase.Command): Greeting {
        val greeting = greetingRepository.findById(command.greetingId)
            ?: throw GreetingNotFoundException(command.greetingId)

        if (greeting.receiverUserId != command.responderUserId) {
            throw GreetingAccessDeniedException(command.greetingId)
        }

        val now = Instant.now()
        if (greeting.isExpired(now)) {
            throw GreetingExpiredException(command.greetingId)
        }

        return if (command.accept) {
            val accepted = greeting.accept(now)
            val saved = greetingRepository.save(accepted)
            eventPublisher.publishGreetingAccepted(saved)
            saved
        } else {
            val expired = greeting.expire()
            greetingRepository.save(expired)
        }
    }
}
