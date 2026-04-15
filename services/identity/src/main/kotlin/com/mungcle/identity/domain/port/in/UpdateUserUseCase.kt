package com.mungcle.identity.domain.port.`in`

import com.mungcle.identity.domain.model.User

/**
 * 사용자 정보 수정 유스케이스 포트.
 */
interface UpdateUserUseCase {
    /** null 필드는 변경하지 않음 (부분 업데이트) */
    data class Command(
        val userId: Long,
        val nickname: String? = null,
        val neighborhood: String? = null,
        val profilePhotoPath: String? = null,
    )

    /** Command에 따라 사용자 정보를 업데이트하고 갱신된 사용자 반환 */
    suspend fun execute(command: Command): User
}
