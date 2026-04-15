package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.UserNotFoundException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeleteUserCommandHandlerTest {

    private val userRepository: UserRepositoryPort = mockk()
    private val handler = DeleteUserCommandHandler(userRepository)

    private val user = User(
        id = 1L,
        email = "test@example.com",
        kakaoId = "kakao-123",
        nickname = "testuser",
        createdAt = Instant.now(),
    )

    @Test
    fun `소프트 삭제 시 이메일 익명화`() = runTest {
        coEvery { userRepository.findById(1L) } returns user
        coEvery { userRepository.save(any()) } answers { firstArg() }

        handler.execute(1L)

        coVerify {
            userRepository.save(match { it.email == "deleted_1@" })
        }
    }

    @Test
    fun `소프트 삭제 시 kakaoId null`() = runTest {
        coEvery { userRepository.findById(1L) } returns user
        coEvery { userRepository.save(any()) } answers { firstArg() }

        handler.execute(1L)

        coVerify {
            userRepository.save(match { it.kakaoId == null })
        }
    }

    @Test
    fun `소프트 삭제 시 닉네임 탈퇴한 사용자로 변경`() = runTest {
        coEvery { userRepository.findById(1L) } returns user
        coEvery { userRepository.save(any()) } answers { firstArg() }

        handler.execute(1L)

        coVerify {
            userRepository.save(match { it.nickname == "탈퇴한 사용자" })
        }
    }

    @Test
    fun `소프트 삭제 시 deletedAt 설정`() = runTest {
        coEvery { userRepository.findById(1L) } returns user
        coEvery { userRepository.save(any()) } answers { firstArg() }

        handler.execute(1L)

        coVerify {
            userRepository.save(match { it.deletedAt != null })
        }
    }

    @Test
    fun `존재하지 않는 사용자 탈퇴 시 UserNotFoundException`() = runTest {
        coEvery { userRepository.findById(99L) } returns null

        assertThrows<UserNotFoundException> {
            handler.execute(99L)
        }
    }
}
