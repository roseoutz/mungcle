package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.EmailTakenException
import com.mungcle.identity.domain.exception.InvalidNicknameException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.RegisterEmailUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.PasswordPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals

class RegisterEmailCommandHandlerTest {

    private val userRepository: UserRepositoryPort = mockk()
    private val jwtPort: JwtPort = mockk()
    private val passwordPort: PasswordPort = mockk()

    private val handler = RegisterEmailCommandHandler(userRepository, jwtPort, passwordPort)

    private val savedUser = User(
        id = 1L,
        email = "test@example.com",
        passwordHash = "hashed",
        nickname = "testuser",
        createdAt = Instant.now(),
    )

    @Test
    fun `정상 회원가입 후 AuthResult 반환`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns null
        every { passwordPort.hash("password123") } returns "hashed"
        coEvery { userRepository.save(any()) } returns savedUser
        every { jwtPort.generateToken(1L) } returns "jwt-token"

        val result = handler.execute(
            RegisterEmailUseCase.Command("test@example.com", "password123", "testuser")
        )

        assertEquals("jwt-token", result.accessToken)
        assertEquals(savedUser, result.user)
        coVerify { userRepository.save(any()) }
    }

    @Test
    fun `이미 존재하는 이메일은 EmailTakenException`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns savedUser

        assertThrows<EmailTakenException> {
            handler.execute(
                RegisterEmailUseCase.Command("test@example.com", "password123", "testuser")
            )
        }
    }

    @Test
    fun `유효하지 않은 닉네임은 InvalidNicknameException`() = runTest {
        coEvery { userRepository.findByEmail(any()) } returns null

        assertThrows<InvalidNicknameException> {
            handler.execute(
                RegisterEmailUseCase.Command("test@example.com", "password123", "a") // 1자 — 너무 짧음
            )
        }
    }

    @Test
    fun `이메일은 소문자로 정규화하여 중복 확인`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns null
        every { passwordPort.hash(any()) } returns "hashed"
        coEvery { userRepository.save(any()) } returns savedUser
        every { jwtPort.generateToken(any()) } returns "jwt-token"

        handler.execute(
            RegisterEmailUseCase.Command("TEST@EXAMPLE.COM", "password123", "testuser")
        )

        coVerify { userRepository.findByEmail("test@example.com") }
    }
}
