package com.mungcle.social.application.command

import com.mungcle.social.domain.port.`in`.ExpireGreetingsUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ExpireGreetingsCommandHandler(
    private val greetingRepository: GreetingRepositoryPort,
    private val eventPublisher: SocialEventPublisherPort,
) : ExpireGreetingsUseCase {

    @Transactional
    override fun execute(now: Instant): Int {
        val expiredPending = greetingRepository.findExpiredPending(now)
            .map { it.expire() }

        val expiredAccepted = greetingRepository.findExpiredAccepted(now)
            .map { it.expire() }

        val allExpired = expiredPending + expiredAccepted
        if (allExpired.isEmpty()) return 0

        greetingRepository.saveAll(allExpired)
        allExpired.forEach { eventPublisher.publishGreetingExpired(it) }

        return allExpired.size
    }
}
