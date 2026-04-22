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
        expiredWalks.forEach { walk ->
            val ended = walk.end(now)
            walkRepository.save(ended)
            walkEventPublisher.publishWalkExpired(walk.id, walk.userId)
        }
        return expiredWalks.size
    }
}
