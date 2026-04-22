package com.mungcle.walks.infrastructure.scheduler

import com.mungcle.walks.domain.port.`in`.ExpireWalksUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WalkExpiryScheduler(
    private val expireWalks: ExpireWalksUseCase,
) {

    @Scheduled(fixedRate = 60_000)
    fun expireWalks() {
        expireWalks.execute(Instant.now())
    }
}
