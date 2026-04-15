package com.mungcle.identity.application.query

import com.mungcle.identity.domain.port.`in`.GetBlockedUserIdsUseCase
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetBlockedUserIdsQueryHandler(
    private val blockRepository: BlockRepositoryPort,
) : GetBlockedUserIdsUseCase {

    override suspend fun execute(userId: Long): List<Long> =
        blockRepository.findBlockedUserIds(userId)
}
