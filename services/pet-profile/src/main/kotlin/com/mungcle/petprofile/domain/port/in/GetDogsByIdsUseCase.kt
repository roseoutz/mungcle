package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

interface GetDogsByIdsUseCase {
    suspend fun execute(dogIds: List<Long>): List<Dog>
}
