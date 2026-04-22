package com.mungcle.petprofile.domain.port.`in`

import com.mungcle.petprofile.domain.model.Dog

interface GetDogUseCase {
    fun execute(dogId: Long): Dog
}
