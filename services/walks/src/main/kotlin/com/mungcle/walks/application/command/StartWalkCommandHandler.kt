package com.mungcle.walks.application.command

import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
import com.mungcle.walks.domain.model.GridCell
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.port.`in`.StartWalkUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class StartWalkCommandHandler(
    private val walkRepository: WalkRepositoryPort,
) : StartWalkUseCase {

    @Transactional
    override suspend fun execute(command: StartWalkUseCase.Command): Walk {
        val existing = walkRepository.findByDogIdAndStatus(command.dogId, WalkStatus.ACTIVE)
        if (existing != null) {
            throw WalkAlreadyActiveException(command.dogId)
        }

        val gridCell = GridCell.fromCoordinates(command.lat, command.lng)
        val now = Instant.now()
        val walk = Walk(
            dogId = command.dogId,
            userId = command.userId,
            type = command.type,
            gridCell = gridCell.value,
            status = WalkStatus.ACTIVE,
            startedAt = now,
            endsAt = now.plusSeconds(3600), // 60분
            createdAt = now,
        )
        return walkRepository.save(walk)
    }
}
