package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.port.`in`.DeleteDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteDogCommandHandler(
    private val dogRepository: DogRepositoryPort,
) : DeleteDogUseCase {

    @Transactional
    override suspend fun execute(dogId: Long, requesterId: Long) {
        val dog = dogRepository.findById(dogId)
            ?: throw DogNotFoundException(dogId)

        dog.verifyOwnership(requesterId)

        dogRepository.save(dog.softDelete())
    }
}
