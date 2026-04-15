package com.mungcle.identity.domain.port.`in`

import com.mungcle.identity.domain.model.User

/**
 * 단일 사용자 조회 유스케이스 포트.
 */
interface GetUserUseCase {
    /** userId에 해당하는 사용자 반환. 없으면 [com.mungcle.identity.domain.exception.UserNotFoundException] */
    suspend fun execute(userId: Long): User
}
