package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament

/**
 * 반려견 등록 유스케이스 포트.
 */
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

    /** 반려견을 등록하고 생성된 Dog 반환. 유저당 최대 5마리 제한. */
    suspend fun execute(command: Command): Dog
}
