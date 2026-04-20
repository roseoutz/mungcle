package com.mungcle.identity.domain.port.`in`

/**
 * 사용자 탈퇴(소프트 삭제) 유스케이스 포트.
 */
interface DeleteUserUseCase {
    /** userId에 해당하는 사용자를 익명화하여 소프트 삭제 */
    suspend fun execute(userId: Long)
}
