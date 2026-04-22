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

        val updated = dog.copy(
            name = command.name ?: dog.name,
            breed = command.breed ?: dog.breed,
            size = command.size ?: dog.size,
            temperaments = command.temperaments ?: dog.temperaments,
            sociability = command.sociability ?: dog.sociability,
            photoPath = command.photoPath ?: dog.photoPath,
            vaccinationPhotoPath = command.vaccinationPhotoPath ?: dog.vaccinationPhotoPath,
        )
        return dogRepository.save(updated)
    }
}
