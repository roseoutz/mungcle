package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.ForbiddenBlockedException
import com.mungcle.social.domain.exception.GreetingDuplicateException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.port.`in`.CreateGreetingUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.IdentityPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import com.mungcle.social.domain.port.out.WalksPort
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CreateGreetingCommandHandler(
    private val greetingRepository: GreetingRepositoryPort,
    private val identityPort: IdentityPort,
    private val walksPort: WalksPort,
    private val eventPublisher: SocialEventPublisherPort,
) : CreateGreetingUseCase {

    @Transactional
    override fun execute(command: CreateGreetingUseCase.Command): Greeting {
        val walkInfo = runBlocking { walksPort.getWalk(command.receiverWalkId) }

        val blocked = runBlocking { identityPort.isBlocked(command.senderUserId, walkInfo.userId) }
        if (blocked) {
            throw ForbiddenBlockedException(command.senderUserId)
        }

        val existing = greetingRepository.findBySenderAndWalk(command.senderUserId, command.receiverWalkId)
        if (existing != null) {
            throw GreetingDuplicateException(command.senderUserId, command.receiverWalkId)
        }

        val now = Instant.now()
        val greeting = Greeting(
            senderUserId = command.senderUserId,
            senderDogId = command.senderDogId,
            receiverUserId = walkInfo.userId,
            receiverDogId = walkInfo.dogId,
            receiverWalkId = command.receiverWalkId,
            status = GreetingStatus.PENDING,
            createdAt = now,
            expiresAt = now.plusSeconds(300),
        )

        val saved = greetingRepository.save(greeting)
        eventPublisher.publishGreetingCreated(saved)
        return saved
    }
}
