package com.mungcle.walks.application.query

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.port.`in`.GetWalkGridCellUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetWalkGridCellQueryHandler(
    private val walkRepository: WalkRepositoryPort,
) : GetWalkGridCellUseCase {

    override fun execute(walkId: Long): GridCell {
        val walk = walkRepository.findById(walkId)
            ?: throw WalkNotFoundException(walkId)
        return walk.gridCell
    }
}
