package com.mungcle.identity.application.query

import com.mungcle.identity.domain.port.`in`.IsBlockedUseCase
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import org.springframework.stereotype.Service

@Service
class IsBlockedQueryHandler(
    private val blockRepository: BlockRepositoryPort,
) : IsBlockedUseCase {

    override suspend fun execute(userIdA: Long, userIdB: Long): Boolean =
        blockRepository.isBlocked(userIdA, userIdB)
}
