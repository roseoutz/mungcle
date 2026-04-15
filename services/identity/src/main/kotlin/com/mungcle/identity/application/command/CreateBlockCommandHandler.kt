package com.mungcle.identity.application.command

import com.mungcle.identity.domain.model.Block
import com.mungcle.identity.domain.port.`in`.CreateBlockUseCase
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateBlockCommandHandler(
    private val blockRepository: BlockRepositoryPort,
) : CreateBlockUseCase {

    @Transactional
    override suspend fun execute(blockerId: Long, blockedId: Long) {
        if (blockRepository.existsByBlockerAndBlocked(blockerId, blockedId)) return
        blockRepository.save(Block(blockerId = blockerId, blockedId = blockedId))
    }
}
