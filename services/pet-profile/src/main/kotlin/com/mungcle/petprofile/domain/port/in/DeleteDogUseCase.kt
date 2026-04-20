package com.mungcle.petprofile.domain.port.`in`

/**
 * 반려견 삭제 유스케이스 포트.
 */
interface DeleteDogUseCase {
    /** 반려견 소프트 삭제. 소유권 확인. */
    suspend fun execute(dogId: Long, requesterId: Long)
}
