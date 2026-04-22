package com.mungcle.social.domain.port.out

import com.mungcle.social.domain.model.Greeting

/**
 * 소셜 이벤트 발행 포트.
 * Kafka 등 메시지 브로커로 이벤트를 발행한다.
 */
interface SocialEventPublisherPort {
    /** 인사 생성 이벤트를 발행한다. */
    fun publishGreetingCreated(greeting: Greeting)

    /** 인사 수락 이벤트를 발행한다. */
    fun publishGreetingAccepted(greeting: Greeting)
}
