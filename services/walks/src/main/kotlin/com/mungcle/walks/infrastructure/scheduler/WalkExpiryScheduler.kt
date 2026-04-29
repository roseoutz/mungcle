package com.mungcle.walks.infrastructure.scheduler

import com.mungcle.walks.domain.port.`in`.ExpireWalksUseCase
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WalkExpiryScheduler(
    private val expireWalks: ExpireWalksUseCase,
) {

    private val log = LoggerFactory.getLogger(WalkExpiryScheduler::class.java)

    @Scheduled(fixedRate = 60_000)
    fun expireWalks() {
        try {
            expireWalks.execute(Instant.now())
        } catch (e: Exception) {
            log.error("산책 만료 스케줄러 실행 중 오류 발생 — 다음 주기에 재시도", e)
        }
    }
}
