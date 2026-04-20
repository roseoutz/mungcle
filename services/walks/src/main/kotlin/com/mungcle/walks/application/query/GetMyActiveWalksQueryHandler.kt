package com.mungcle.walks.application.query

import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.port.`in`.GetMyActiveWalksUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetMyActiveWalksQueryHandler(
    private val walkRepository: WalkRepositoryPort,
) : GetMyActiveWalksUseCase {

    override suspend fun execute(userId: Long): List<Walk> =
        walkRepository.findByUserIdAndStatus(userId, WalkStatus.ACTIVE)
}
