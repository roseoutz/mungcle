package com.mungcle.petprofile.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface DogSpringDataRepository : JpaRepository<DogEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): DogEntity?
    fun findByOwnerIdAndDeletedAtIsNull(ownerId: Long): List<DogEntity>
    fun findByIdInAndDeletedAtIsNull(ids: List<Long>): List<DogEntity>
    fun countByOwnerIdAndDeletedAtIsNull(ownerId: Long): Long
}
