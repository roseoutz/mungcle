package com.mungcle.petprofile.domain.port.out

import com.mungcle.petprofile.domain.model.Dog

/**
 * 반려견 저장소 아웃바운드 포트.
 */
interface DogRepositoryPort {
    /** 반려견 저장 (신규 생성 또는 업데이트) */
    suspend fun save(dog: Dog): Dog

    /** ID로 반려견 조회 (삭제되지 않은 것만) */
    suspend fun findById(id: Long): Dog?

    /** 소유자 ID로 반려견 목록 조회 (삭제되지 않은 것만) */
    suspend fun findByOwnerId(ownerId: Long): List<Dog>

    /** 여러 ID로 반려견 목록 조회 (삭제되지 않은 것만) */
    suspend fun findByIds(ids: List<Long>): List<Dog>

    /** 소유자의 반려견 수 (삭제되지 않은 것만) */
    suspend fun countByOwnerId(ownerId: Long): Long
}
