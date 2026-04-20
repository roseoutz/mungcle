package com.mungcle.identity.application.command

import com.mungcle.identity.domain.port.`in`.DeleteBlockUseCase
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteBlockCommandHandler(
    private val blockRepository: BlockRepositoryPort,
) : DeleteBlockUseCase {

    @Transactional
    override suspend fun execute(blockerId: Long, blockedId: Long) {
        blockRepository.delete(blockerId, blockedId)
    }
}
