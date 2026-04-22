package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.ForbiddenBlockedException
import com.mungcle.social.domain.exception.GreetingDuplicateException
import com.mungcle.social.domain.exception.SelfGreetingException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.port.`in`.CreateGreetingUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.IdentityPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import com.mungcle.social.domain.port.out.WalksPort
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

    override suspend fun execute(command: CreateGreetingUseCase.Command): Greeting {
        // gRPC 호출 (suspend, 트랜잭션 밖)
        val walkInfo = walksPort.getWalk(command.receiverWalkId)
        if (command.senderUserId == walkInfo.userId) throw SelfGreetingException(command.senderUserId)
        val blocked = identityPort.isBlocked(command.senderUserId, walkInfo.userId)
        if (blocked) throw ForbiddenBlockedException(command.senderUserId)

        // DB 저장 (트랜잭션)
        return saveGreeting(command, walkInfo)
    }

    @Transactional
    fun saveGreeting(command: CreateGreetingUseCase.Command, walkInfo: WalksPort.WalkInfo): Greeting {
        greetingRepository.findBySenderAndWalk(command.senderUserId, command.receiverWalkId)?.let {
            throw GreetingDuplicateException(command.senderUserId, command.receiverWalkId)
        }
        val greeting = Greeting(
            senderUserId = command.senderUserId,
            senderDogId = command.senderDogId,
            receiverUserId = walkInfo.userId,
            receiverDogId = walkInfo.dogId,
            receiverWalkId = command.receiverWalkId,
            expiresAt = Instant.now().plusSeconds(300),
        )
        val saved = greetingRepository.save(greeting)
        eventPublisher.publishGreetingCreated(saved)
        return saved
    }
}
