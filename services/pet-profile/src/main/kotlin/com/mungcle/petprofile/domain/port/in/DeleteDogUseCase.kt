package com.mungcle.petprofile.domain.port.`in`

interface DeleteDogUseCase {
    fun execute(dogId: Long, requesterId: Long)
}
