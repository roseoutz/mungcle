package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

interface GetDogsByIdsUseCase {
    fun execute(dogIds: List<Long>): List<Dog>
}
