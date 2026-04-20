package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament

interface CreateDogUseCase {
    data class Command(
        val ownerId: Long,
        val name: String,
        val breed: String,
        val size: DogSize,
        val temperaments: List<Temperament>,
        val sociability: Int,
        val photoPath: String? = null,
        val vaccinationPhotoPath: String? = null,
    )

    suspend fun execute(command: Command): Dog
}
