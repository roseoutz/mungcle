package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogLimitExceededException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.port.`in`.CreateDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateDogCommandHandler(
    private val dogRepository: DogRepositoryPort,
) : CreateDogUseCase {

    @Transactional
    override suspend fun execute(command: CreateDogUseCase.Command): Dog {
        val count = dogRepository.countByOwnerId(command.ownerId)
        if (count >= MAX_DOGS_PER_OWNER) {
            throw DogLimitExceededException(command.ownerId)
        }

        val dog = Dog(
            ownerId = command.ownerId,
            name = command.name,
            breed = command.breed,
            size = command.size,
            temperaments = command.temperaments,
            sociability = command.sociability,
            photoPath = command.photoPath,
            vaccinationPhotoPath = command.vaccinationPhotoPath,
        )
        return dogRepository.save(dog)
    }

    companion object {
        private const val MAX_DOGS_PER_OWNER = 5
    }
}
