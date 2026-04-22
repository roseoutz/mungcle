package com.mungcle.petprofile.domain.port.out

import com.mungcle.petprofile.domain.model.Dog

interface DogRepositoryPort {
    /** 반려견을 저장하고 저장된 도메인 객체를 반환한다. */
    fun save(dog: Dog): Dog

    /** ID로 반려견을 조회한다. 삭제된 반려견은 반환하지 않는다. */
    fun findById(id: Long): Dog?

    /** 소유자 ID로 반려견 목록을 조회한다. 삭제된 반려견은 포함하지 않는다. */
    fun findByOwnerId(ownerId: Long): List<Dog>

    /** ID 목록으로 반려견 목록을 조회한다. 삭제된 반려견은 포함하지 않는다. */
    fun findByIds(ids: List<Long>): List<Dog>

    /** 소유자 ID로 삭제되지 않은 반려견 수를 반환한다. */
    fun countByOwnerId(ownerId: Long): Long
}
