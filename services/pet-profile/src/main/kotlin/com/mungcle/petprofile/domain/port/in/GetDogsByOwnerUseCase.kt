package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

/**
 * 소유자별 반려견 목록 조회 유스케이스 포트.
 */
interface GetDogsByOwnerUseCase {
    /** 소유자의 반려견 목록 반환 */
    suspend fun execute(ownerId: Long): List<Dog>
}
