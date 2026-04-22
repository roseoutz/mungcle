package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.port.`in`.UpdateDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateDogCommandHandler(
    private val dogRepository: DogRepositoryPort,
) : UpdateDogUseCase {

    @Transactional
    override fun execute(command: UpdateDogUseCase.Command): Dog {
        val dog = dogRepository.findById(command.dogId)
            ?: throw DogNotFoundException(command.dogId)

        dog.verifyOwnership(command.requesterId)

        dog.update(
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
}
