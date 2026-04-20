package com.mungcle.petprofile.domain.port.out

import com.mungcle.petprofile.domain.model.Dog

interface DogRepositoryPort {
    suspend fun save(dog: Dog): Dog
    suspend fun findById(id: Long): Dog?
    suspend fun findByOwnerId(ownerId: Long): List<Dog>
    suspend fun findByIds(ids: List<Long>): List<Dog>
}
