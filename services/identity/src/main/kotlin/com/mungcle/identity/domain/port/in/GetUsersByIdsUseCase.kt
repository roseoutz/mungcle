package com.mungcle.identity.domain.port.`in`

import com.mungcle.identity.domain.model.User

/**
 * 여러 사용자 일괄 조회 유스케이스 포트.
 */
interface GetUsersByIdsUseCase {
    /** 주어진 ID 목록에 해당하는 사용자 목록 반환. 존재하지 않는 ID는 무시 */
    suspend fun execute(userIds: List<Long>): List<User>
}
