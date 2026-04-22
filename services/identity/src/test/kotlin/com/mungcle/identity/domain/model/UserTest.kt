package com.mungcle.identity.domain.model

import com.mungcle.identity.domain.exception.InvalidNicknameException
import com.mungcle.identity.domain.model.SocialProvider
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserTest {

    // ─── validateNickname ───────────────────────────────────────────────────

    @Test
    fun `한글 닉네임 유효`() {
        // 예외 없이 통과해야 함
        User.validateNickname("멍클사용자")
    }

    @Test
    fun `영문 닉네임 유효`() {
        User.validateNickname("MungcleUser")
    }

    @Test
    fun `영문 숫자 혼합 닉네임 유효`() {
        User.validateNickname("user123")
    }

    @Test
    fun `언더스코어 포함 닉네임 유효`() {
        User.validateNickname("user_name")
    }

    @Test
    fun `2자 최소 길이 닉네임 유효`() {
        User.validateNickname("ab")
    }

    @Test
    fun `16자 최대 길이 닉네임 유효`() {
        User.validateNickname("a".repeat(16))
    }

    @Test
    fun `1자 닉네임은 InvalidNicknameException`() {
        assertThrows<InvalidNicknameException> {
            User.validateNickname("a")
        }
    }

    @Test
    fun `17자 닉네임은 InvalidNicknameException`() {
        assertThrows<InvalidNicknameException> {
            User.validateNickname("a".repeat(17))
        }
    }

    @Test
    fun `공백 포함 닉네임은 InvalidNicknameException`() {
        assertThrows<InvalidNicknameException> {
            User.validateNickname("user name")
        }
    }

    @Test
    fun `특수문자 포함 닉네임은 InvalidNicknameException`() {
        assertThrows<InvalidNicknameException> {
            User.validateNickname("user@name")
        }
    }

    @Test
    fun `빈 문자열은 InvalidNicknameException`() {
        assertThrows<InvalidNicknameException> {
            User.validateNickname("")
        }
    }

    // ─── normalizeEmail ──────────────────────────────────────────────────────

    @Test
    fun `대문자 이메일을 소문자로 정규화`() {
        assertEquals("test@example.com", User.normalizeEmail("TEST@EXAMPLE.COM"))
    }

    @Test
    fun `앞뒤 공백 제거`() {
        assertEquals("test@example.com", User.normalizeEmail("  test@example.com  "))
    }

    @Test
    fun `공백과 대소문자 동시 정규화`() {
        assertEquals("user@domain.com", User.normalizeEmail("  User@Domain.Com  "))
    }

    // ─── softDelete ──────────────────────────────────────────────────────────

    @Test
    fun `softDelete — socialProvider null로 익명화`() {
        val user = User(
            id = 42L,
            socialProvider = SocialProvider.KAKAO,
            socialId = "kakao-123",
            nickname = "테스트유저",
        )
        val deleted = user.softDelete()
        assertNull(deleted.socialProvider)
        assertNull(deleted.socialId)
        assertEquals("deleted_42@", deleted.email)
        assertEquals("탈퇴한 사용자", deleted.nickname)
    }
}
