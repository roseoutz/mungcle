package com.mungcle.identity.domain.model

import com.mungcle.identity.domain.exception.InvalidNicknameException
import java.time.Instant

/**
 * 사용자 도메인 모델.
 * 순수 Kotlin 객체 — 프레임워크 의존성 없음.
 * 식별자(id) 기반 동등성 비교.
 */
class User(
    val id: Long = 0,
    socialProvider: SocialProvider? = null,
    socialId: String? = null,
    email: String? = null,
    val passwordHash: String? = null,
    nickname: String,
    pushToken: String? = null,
    neighborhood: String? = null,
    profilePhotoPath: String? = null,
    flaggedForReview: Boolean = false,
    deletedAt: Instant? = null,
    val createdAt: Instant = Instant.now(),
) {
    // softDelete()가 익명화하므로 var + private set
    var socialProvider: SocialProvider? = socialProvider
        private set

    var socialId: String? = socialId
        private set

    var email: String? = email
        private set

    var nickname: String = nickname
        private set

    var pushToken: String? = pushToken
        private set

    var neighborhood: String? = neighborhood
        private set

    var profilePhotoPath: String? = profilePhotoPath
        private set

    var flaggedForReview: Boolean = flaggedForReview
        private set

    var deletedAt: Instant? = deletedAt
        private set

    /** 닉네임 변경 — 유효성 검증 후 설정 */
    fun changeNickname(nickname: String) {
        validateNickname(nickname)
        this.nickname = nickname
    }

    /** 동네 정보 변경 */
    fun changeNeighborhood(neighborhood: String) {
        this.neighborhood = neighborhood
    }

    /** 프로필 사진 경로 변경 */
    fun changeProfilePhoto(path: String?) {
        this.profilePhotoPath = path
    }

    /** 푸시 토큰 변경 */
    fun changePushToken(token: String?) {
        this.pushToken = token
    }

    /** 신고 누적으로 인한 검토 대상 플래그 설정 */
    fun flagForReview() {
        this.flaggedForReview = true
    }

    /** 회원 탈퇴 소프트 삭제 — 개인정보 익명화 */
    fun softDelete(): User {
        this.email = "deleted_${id}@"
        this.socialProvider = null
        this.socialId = null
        // 닉네임 유효성 검증을 의도적으로 우회 — 익명화 값 "탈퇴한 사용자"는 공백 포함
        this.nickname = "탈퇴한 사용자"
        this.deletedAt = Instant.now()
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        // 미저장 엔티티(id=0)는 참조 동일성만 허용
        if (id == 0L) return false
        return id == other.id
    }

    override fun hashCode(): Int = if (id != 0L) id.hashCode() else System.identityHashCode(this)

    override fun toString(): String =
        "User(id=$id, nickname=$nickname, email=$email, flaggedForReview=$flaggedForReview, deletedAt=$deletedAt)"

    companion object {
        private val NICKNAME_REGEX = Regex("^[가-힣a-zA-Z0-9_]{2,16}$")

        /** 이메일을 소문자 trim 정규화 */
        fun normalizeEmail(email: String): String = email.trim().lowercase()

        /** 닉네임 유효성 검증. 실패 시 [InvalidNicknameException] 던짐 */
        fun validateNickname(nickname: String) {
            if (!NICKNAME_REGEX.matches(nickname)) throw InvalidNicknameException(nickname)
        }
    }
}
