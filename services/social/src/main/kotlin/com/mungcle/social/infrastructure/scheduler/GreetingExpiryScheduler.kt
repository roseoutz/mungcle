package com.mungcle.social.infrastructure.scheduler

import com.mungcle.social.domain.port.`in`.ExpireGreetingsUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class GreetingExpiryScheduler(
    private val expireGreetings: ExpireGreetingsUseCase,
) {

    @Scheduled(fixedRate = 60_000)
    fun expire() {
        expireGreetings.execute(Instant.now())
    }
}
