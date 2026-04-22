package com.mungcle.walks.application.command

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.port.`in`.StartWalkUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class StartWalkCommandHandler(
    private val walkRepository: WalkRepositoryPort,
) : StartWalkUseCase {

    @Transactional
    override fun execute(command: StartWalkUseCase.Command): Walk {
        val existing = walkRepository.findActiveByDogId(command.dogId)
        if (existing != null) {
            throw WalkAlreadyActiveException(command.dogId)
        }

        val now = Instant.now()
        val walk = Walk(
            dogId = command.dogId,
            userId = command.userId,
            type = command.type,
            gridCell = GridCell.fromCoordinates(command.lat, command.lng),
            status = WalkStatus.ACTIVE,
            startedAt = now,
            endsAt = now.plus(Duration.ofMinutes(60)),
        )
        return walkRepository.save(walk)
    }
}
