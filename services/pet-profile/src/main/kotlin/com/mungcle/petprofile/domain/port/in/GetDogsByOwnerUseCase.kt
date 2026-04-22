package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

interface GetDogsByOwnerUseCase {
    fun execute(ownerId: Long): List<Dog>
}
