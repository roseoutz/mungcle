package com.mungcle.social.infrastructure.persistence

import com.mungcle.social.domain.model.Message
import com.mungcle.social.domain.port.out.MessageRepositoryPort
import org.springframework.stereotype.Repository

@Repository
class MessageRepositoryAdapter(
    private val springDataRepository: MessageSpringDataRepository,
) : MessageRepositoryPort {

    override fun save(message: Message): Message {
        val entity = MessageMapper.toEntity(message)
        val saved = springDataRepository.save(entity)
        return MessageMapper.toDomain(saved)
    }

    override fun findByGreetingId(greetingId: Long): List<Message> =
        springDataRepository.findByGreetingId(greetingId).map(MessageMapper::toDomain)
}
