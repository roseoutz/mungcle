package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.exception.WalkNotOwnedException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
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
    override suspend fun execute(command: StopWalkUseCase.Command): Walk {
        val walk = walkRepository.findById(command.walkId)
            ?: throw WalkNotFoundException(command.walkId)

        if (walk.userId != command.userId) {
            throw WalkNotOwnedException(command.walkId, command.userId)
        }

        if (walk.status == WalkStatus.ENDED) {
            throw WalkAlreadyEndedException(command.walkId)
        }

        val ended = walk.end(Instant.now())
        return walkRepository.save(ended)
    }
}
