package com.mungcle.social.domain.port.out

import com.mungcle.social.domain.model.Message

interface MessageRepositoryPort {
    fun save(message: Message): Message
    fun findByGreetingId(greetingId: Long): List<Message>
}
