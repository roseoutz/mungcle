package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament

interface UpdateDogUseCase {
    data class Command(
        val dogId: Long,
        val requesterId: Long,
        val name: String? = null,
        val breed: String? = null,
        val size: DogSize? = null,
        val temperaments: List<Temperament>? = null,
        val sociability: Int? = null,
        val photoPath: String? = null,
        val vaccinationPhotoPath: String? = null,
    )

    suspend fun execute(command: Command): Dog
}
