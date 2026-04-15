package com.mungcle.identity.application.command

import com.mungcle.identity.domain.exception.InvalidCredentialsException
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.LoginEmailUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.PasswordPort
import com.mungcle.identity.domain.port.out.UserRepositoryPort
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals

class LoginEmailCommandHandlerTest {

    private val userRepository: UserRepositoryPort = mockk()
    private val jwtPort: JwtPort = mockk()
    private val passwordPort: PasswordPort = mockk()

    private val handler = LoginEmailCommandHandler(userRepository, jwtPort, passwordPort)

    private val existingUser = User(
        id = 1L,
        email = "test@example.com",
        passwordHash = "hashed",
        nickname = "testuser",
        createdAt = Instant.now(),
    )

    @Test
    fun `올바른 이메일과 비밀번호로 로그인 성공`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns existingUser
        every { passwordPort.verify("password123", "hashed") } returns true
        every { jwtPort.generateToken(1L) } returns "jwt-token"

        val result = handler.execute(
            LoginEmailUseCase.Command("test@example.com", "password123")
        )

        assertEquals("jwt-token", result.accessToken)
        assertEquals(existingUser, result.user)
    }

    @Test
    fun `잘못된 비밀번호는 InvalidCredentialsException`() = runTest {
        coEvery { userRepository.findByEmail("test@example.com") } returns existingUser
        every { passwordPort.verify("wrongpassword", "hashed") } returns false

        assertThrows<InvalidCredentialsException> {
            handler.execute(LoginEmailUseCase.Command("test@example.com", "wrongpassword"))
        }
    }

    @Test
    fun `존재하지 않는 이메일은 InvalidCredentialsException`() = runTest {
        coEvery { userRepository.findByEmail("notfound@example.com") } returns null

        assertThrows<InvalidCredentialsException> {
            handler.execute(LoginEmailUseCase.Command("notfound@example.com", "password123"))
        }
    }
}
