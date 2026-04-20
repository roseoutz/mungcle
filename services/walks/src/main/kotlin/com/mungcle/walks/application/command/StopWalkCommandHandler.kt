package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.exception.WalkNotOwnedException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.port.`in`.StopWalkUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class StopWalkCommandHandler(
    private val walkRepository: WalkRepositoryPort,
) : StopWalkUseCase {

    @Transactional
    override suspend fun execute(walkId: Long, userId: Long): Walk {
        val walk = walkRepository.findById(walkId)
            ?: throw WalkNotFoundException(walkId)

        if (walk.userId != userId) {
            throw WalkNotOwnedException(walkId, userId)
        }

        val ended = walk.end(Instant.now())
        return walkRepository.save(ended)
    }
}
