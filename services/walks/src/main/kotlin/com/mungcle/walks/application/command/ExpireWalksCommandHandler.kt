package com.mungcle.walks.application.command

import com.mungcle.walks.domain.port.`in`.ExpireWalksUseCase
import com.mungcle.walks.domain.port.out.WalkEventPublisherPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ExpireWalksCommandHandler(
    private val walkRepository: WalkRepositoryPort,
    private val walkEventPublisher: WalkEventPublisherPort,
) : ExpireWalksUseCase {

    @Transactional
    override fun execute(now: Instant): Int {
        val expiredWalks = walkRepository.findExpiredActive(now)
        if (expiredWalks.isEmpty()) return 0

        val endedWalks = expiredWalks.map { it.end(now) }
        walkRepository.saveAll(endedWalks)

        endedWalks.forEach { walk ->
            walkEventPublisher.publishWalkExpired(walk.id, walk.userId)
        }

        return endedWalks.size
    }
}
