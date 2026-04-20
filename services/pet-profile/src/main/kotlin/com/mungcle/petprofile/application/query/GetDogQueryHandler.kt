package com.mungcle.petprofile.application.query

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.port.`in`.GetDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Service

@Service
class GetDogQueryHandler(
    private val dogRepository: DogRepositoryPort,
) : GetDogUseCase {

    override suspend fun execute(dogId: Long): Dog =
        dogRepository.findById(dogId) ?: throw DogNotFoundException(dogId)
}
