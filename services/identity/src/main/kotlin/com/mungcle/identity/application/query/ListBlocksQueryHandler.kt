package com.mungcle.identity.application.query

import com.mungcle.identity.domain.model.Block
import com.mungcle.identity.domain.port.`in`.ListBlocksUseCase
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import org.springframework.stereotype.Service

@Service
class ListBlocksQueryHandler(
    private val blockRepository: BlockRepositoryPort,
) : ListBlocksUseCase {

    override suspend fun execute(userId: Long): List<Block> =
        blockRepository.findByBlockerId(userId)
}
