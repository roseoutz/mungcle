package com.mungcle.walks.application.command

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.port.`in`.StartWalkUseCase
import com.mungcle.walks.domain.port.out.WalkPatternRepositoryPort
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@Service
class StartWalkCommandHandler(
    private val walkRepository: WalkRepositoryPort,
    private val walkPatternRepository: WalkPatternRepositoryPort,
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
        val saved = walkRepository.save(walk)

        walkPatternRepository.upsert(
            gridCell = saved.gridCell.value,
            hourOfDay = now.atZone(ZoneId.of("Asia/Seoul")).hour,
            dogId = saved.dogId,
            walkedAt = now,
        )

        return saved
    }
}
