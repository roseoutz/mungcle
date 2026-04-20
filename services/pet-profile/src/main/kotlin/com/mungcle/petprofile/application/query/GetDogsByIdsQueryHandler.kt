package com.mungcle.petprofile.application.query

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.port.`in`.GetDogsByIdsUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetDogsByIdsQueryHandler(
    private val dogRepository: DogRepositoryPort,
) : GetDogsByIdsUseCase {

    override suspend fun execute(dogIds: List<Long>): List<Dog> =
        dogRepository.findByIds(dogIds)
}
