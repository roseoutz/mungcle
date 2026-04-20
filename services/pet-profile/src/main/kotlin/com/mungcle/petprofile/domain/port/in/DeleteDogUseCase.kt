package com.mungcle.petprofile.domain.port.`in`

interface DeleteDogUseCase {
    suspend fun execute(dogId: Long, requesterId: Long)
}
