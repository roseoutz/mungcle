package com.mungcle.walks.application.query

import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.port.`in`.GetWalkGridCellUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetWalkGridCellQueryHandler(
    private val walkRepository: WalkRepositoryPort,
) : GetWalkGridCellUseCase {

    override suspend fun execute(walkId: Long): String {
        val walk = walkRepository.findById(walkId)
            ?: throw WalkNotFoundException(walkId)
        return walk.gridCell
    }
}
