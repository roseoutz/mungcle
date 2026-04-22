package com.mungcle.walks.application.query

import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.port.`in`.GetWalkUseCase
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetWalkQueryHandler(
    private val walkRepository: WalkRepositoryPort,
) : GetWalkUseCase {

    override fun execute(walkId: Long): Walk {
        return walkRepository.findById(walkId)
            ?: throw WalkNotFoundException(walkId)
    }
}
