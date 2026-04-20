package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

/**
 * 반려견 단건 조회 유스케이스 포트.
 */
interface GetDogUseCase {
    /** ID로 반려견 조회. 없으면 DogNotFoundException */
    suspend fun execute(dogId: Long): Dog
}
