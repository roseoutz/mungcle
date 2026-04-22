package com.mungcle.social.application.query

import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.model.Message
import com.mungcle.social.domain.port.`in`.ListMessagesUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.MessageRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListMessagesQueryHandler(
    private val greetingRepository: GreetingRepositoryPort,
    private val messageRepository: MessageRepositoryPort,
) : ListMessagesUseCase {

    override fun execute(query: ListMessagesUseCase.Query): List<Message> {
        val greeting = greetingRepository.findById(query.greetingId)
            ?: throw GreetingNotFoundException(query.greetingId)

        val isParticipant = greeting.senderUserId == query.userId ||
            greeting.receiverUserId == query.userId
        if (!isParticipant) throw GreetingAccessDeniedException(query.greetingId)

        return messageRepository.findByGreetingId(query.greetingId)
    }
}
