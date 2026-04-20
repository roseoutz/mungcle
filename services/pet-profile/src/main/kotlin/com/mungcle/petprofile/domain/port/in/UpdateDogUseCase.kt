package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament

/**
 * 반려견 정보 수정 유스케이스 포트.
 */
interface UpdateDogUseCase {
    /** null 필드는 변경하지 않음 (부분 업데이트). temperaments가 null이면 변경하지 않음. */
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

    /** 반려견 정보를 수정하고 갱신된 Dog 반환. 소유권 확인. */
    suspend fun execute(command: Command): Dog
}
