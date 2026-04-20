package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

/**
 * 반려견 일괄 조회 유스케이스 포트.
 */
interface GetDogsByIdsUseCase {
    /** 여러 ID로 반려견 조회. 없는 ID는 무시. */
    suspend fun execute(dogIds: List<Long>): List<Dog>
}
