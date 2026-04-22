package com.mungcle.identity.application.command

import com.mungcle.identity.application.dto.AuthResult
import com.mungcle.identity.domain.exception.UnsupportedProviderException
import com.mungcle.identity.domain.model.SocialProvider
import com.mungcle.identity.domain.model.User
import com.mungcle.identity.domain.port.`in`.AuthenticateSocialUseCase
import com.mungcle.identity.domain.port.out.JwtPort
import com.mungcle.identity.domain.port.out.SocialAuthPort
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

class AuthenticateSocialCommandHandlerTest {

    private val userRepository: UserRepositoryPort = mockk()
    private val jwtPort: JwtPort = mockk()

    private fun makeAdapter(p: SocialProvider, id: String): SocialAuthPort = mockk {
        every { provider } returns p
        coEvery { getUserId(any()) } returns id
    }

    private val kakaoAdapter = makeAdapter(SocialProvider.KAKAO, "kakao-123456")
    private val naverAdapter = makeAdapter(SocialProvider.NAVER, "naver-123456")
    private val appleAdapter = makeAdapter(SocialProvider.APPLE, "apple-sub-123")
    private val googleAdapter = makeAdapter(SocialProvider.GOOGLE, "google-sub-456")

    private val handler = AuthenticateSocialCommandHandler(
        socialAuthAdapters = listOf(kakaoAdapter, naverAdapter, appleAdapter, googleAdapter),
        userRepository = userRepository,
        jwtPort = jwtPort,
    )

    private fun fakeUser(provider: SocialProvider, socialId: String) = User(
        id = 1L,
        socialProvider = provider,
        socialId = socialId,
        nickname = "${provider.name.lowercase()}유저_${socialId.takeLast(6)}",
        createdAt = Instant.now(),
    )

    @Test
    fun `카카오 신규 가입 — 사용자 저장 후 JWT 반환`() = runTest {
        val socialId = "kakao-123456"
        val newUser = fakeUser(SocialProvider.KAKAO, socialId)
        coEvery { userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId) } returns null
        coEvery { userRepository.save(any()) } returns newUser
        every { jwtPort.generateToken(1L) } returns "jwt-token"

        val result = handler.execute(
            AuthenticateSocialUseCase.Command(SocialProvider.KAKAO, "kakao-access-token")
        )

        assertEquals("jwt-token", result.accessToken)
        assertEquals(newUser, result.user)
        coVerify { userRepository.save(any()) }
    }

    @Test
    fun `카카오 기존 유저 로그인 — 저장 없이 JWT 반환`() = runTest {
        val socialId = "kakao-123456"
        val existingUser = fakeUser(SocialProvider.KAKAO, socialId)
        coEvery { userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId) } returns existingUser
        every { jwtPort.generateToken(1L) } returns "jwt-token"

        val result = handler.execute(
            AuthenticateSocialUseCase.Command(SocialProvider.KAKAO, "kakao-access-token")
        )

        assertEquals("jwt-token", result.accessToken)
        coVerify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `네이버 신규 가입 — 사용자 저장 후 JWT 반환`() = runTest {
        val socialId = "naver-123456"
        val newUser = fakeUser(SocialProvider.NAVER, socialId)
        coEvery { userRepository.findBySocialProviderAndSocialId(SocialProvider.NAVER, socialId) } returns null
        coEvery { userRepository.save(any()) } returns newUser
        every { jwtPort.generateToken(1L) } returns "jwt-token"

        val result = handler.execute(
            AuthenticateSocialUseCase.Command(SocialProvider.NAVER, "naver-access-token")
        )

        assertEquals("jwt-token", result.accessToken)
        coVerify { userRepository.save(any()) }
    }

    @Test
    fun `애플 로그인 — 준비 중 SocialAuthFailedException`() = runTest {
        val appleAdapterThrows: SocialAuthPort = mockk {
            every { provider } returns SocialProvider.APPLE
            coEvery { getUserId(any()) } throws
                com.mungcle.identity.domain.exception.SocialAuthFailedException(
                    SocialProvider.APPLE, "Apple 로그인은 준비 중입니다"
                )
        }
        val handlerWithRealApple = AuthenticateSocialCommandHandler(
            socialAuthAdapters = listOf(kakaoAdapter, naverAdapter, appleAdapterThrows, googleAdapter),
            userRepository = userRepository,
            jwtPort = jwtPort,
        )

        assertThrows<com.mungcle.identity.domain.exception.SocialAuthFailedException> {
            handlerWithRealApple.execute(
                AuthenticateSocialUseCase.Command(SocialProvider.APPLE, "apple-id-token")
            )
        }
    }

    @Test
    fun `구글 신규 가입 — 사용자 저장 후 JWT 반환`() = runTest {
        val socialId = "google-sub-456"
        val newUser = fakeUser(SocialProvider.GOOGLE, socialId)
        coEvery { userRepository.findBySocialProviderAndSocialId(SocialProvider.GOOGLE, socialId) } returns null
        coEvery { userRepository.save(any()) } returns newUser
        every { jwtPort.generateToken(1L) } returns "jwt-token"

        val result = handler.execute(
            AuthenticateSocialUseCase.Command(SocialProvider.GOOGLE, "google-access-token")
        )

        assertEquals("jwt-token", result.accessToken)
        coVerify { userRepository.save(any()) }
    }

    @Test
    fun `어댑터 없는 프로바이더 — UnsupportedProviderException`() = runTest {
        // 카카오 어댑터만 등록된 핸들러
        val limitedHandler = AuthenticateSocialCommandHandler(
            socialAuthAdapters = listOf(kakaoAdapter),
            userRepository = userRepository,
            jwtPort = jwtPort,
        )

        assertThrows<UnsupportedProviderException> {
            limitedHandler.execute(
                AuthenticateSocialUseCase.Command(SocialProvider.NAVER, "naver-token")
            )
        }
    }

    @Test
    fun `Strategy 디스패치 — 카카오 요청 시 카카오 어댑터만 호출`() = runTest {
        val socialId = "kakao-123456"
        val user = fakeUser(SocialProvider.KAKAO, socialId)
        coEvery { userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId) } returns user
        every { jwtPort.generateToken(any()) } returns "jwt-token"

        handler.execute(AuthenticateSocialUseCase.Command(SocialProvider.KAKAO, "kakao-token"))

        coVerify { kakaoAdapter.getUserId("kakao-token") }
        coVerify(exactly = 0) { naverAdapter.getUserId(any()) }
        coVerify(exactly = 0) { appleAdapter.getUserId(any()) }
        coVerify(exactly = 0) { googleAdapter.getUserId(any()) }
    }
}
