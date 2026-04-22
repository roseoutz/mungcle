package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingNotAcceptedException
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.exception.MessageTooLongException
import com.mungcle.social.domain.model.Message
import com.mungcle.social.domain.port.`in`.SendMessageUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.MessageRepositoryPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SendMessageCommandHandler(
    private val greetingRepository: GreetingRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
    private val eventPublisher: SocialEventPublisherPort,
) : SendMessageUseCase {

    @Transactional
    override fun execute(command: SendMessageUseCase.Command): Message {
        val trimmed = command.body.trim()
        if (trimmed.isEmpty() || trimmed.length > 140) throw MessageTooLongException(trimmed.length)

        val greeting = greetingRepository.findById(command.greetingId)
            ?: throw GreetingNotFoundException(command.greetingId)

        val isParticipant = greeting.senderUserId == command.senderUserId ||
            greeting.receiverUserId == command.senderUserId
        if (!isParticipant) throw GreetingAccessDeniedException(command.greetingId)

        val now = Instant.now()
        if (!greeting.canSendMessage(now)) throw GreetingNotAcceptedException(command.greetingId)

        val message = Message(
            greetingId = command.greetingId,
            senderUserId = command.senderUserId,
            body = trimmed,
        )
        val saved = messageRepository.save(message)
        val receiverUserId = if (greeting.senderUserId == command.senderUserId) {
            greeting.receiverUserId
        } else {
            greeting.senderUserId
        }
        eventPublisher.publishMessageSent(saved, receiverUserId)
        return saved
    }
}
