package com.mungcle.petprofile.application.query

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.port.`in`.GetDogsByOwnerUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetDogsByOwnerQueryHandler(
    private val dogRepository: DogRepositoryPort,
) : GetDogsByOwnerUseCase {

    override suspend fun execute(ownerId: Long): List<Dog> =
        dogRepository.findByOwnerId(ownerId)
}
