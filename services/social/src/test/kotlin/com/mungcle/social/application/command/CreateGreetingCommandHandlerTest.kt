package com.mungcle.social.application.command

import com.mungcle.social.domain.exception.ForbiddenBlockedException
import com.mungcle.social.domain.exception.GreetingDuplicateException
import com.mungcle.social.domain.exception.SelfGreetingException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.port.`in`.CreateGreetingUseCase
import com.mungcle.social.domain.port.out.GreetingRepositoryPort
import com.mungcle.social.domain.port.out.IdentityPort
import com.mungcle.social.domain.port.out.SocialEventPublisherPort
import com.mungcle.social.domain.port.out.WalksPort
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals

class CreateGreetingCommandHandlerTest {

    private val greetingRepository: GreetingRepositoryPort = mockk()
    private val identityPort: IdentityPort = mockk()
    private val walksPort: WalksPort = mockk()
    private val eventPublisher: SocialEventPublisherPort = mockk(relaxed = true)

    private val handler = CreateGreetingCommandHandler(
        greetingRepository = greetingRepository,
        identityPort = identityPort,
        walksPort = walksPort,
        eventPublisher = eventPublisher,
    )

    private val command = CreateGreetingUseCase.Command(
        senderUserId = 10L,
        senderDogId = 100L,
        receiverWalkId = 300L,
    )

    private val walkInfo = WalksPort.WalkInfo(walkId = 300L, userId = 20L, dogId = 200L)

    @Test
    fun `정상 인사 생성`() = runTest {
        coEvery { walksPort.getWalk(300L) } returns walkInfo
        coEvery { identityPort.isBlocked(10L, 20L) } returns false
        every { greetingRepository.findBySenderAndWalk(10L, 300L) } returns null
        val slot = slot<Greeting>()
        every { greetingRepository.save(capture(slot)) } answers {
            val g = slot.captured
            Greeting(
                id = 1L,
                senderUserId = g.senderUserId,
                senderDogId = g.senderDogId,
                receiverUserId = g.receiverUserId,
                receiverDogId = g.receiverDogId,
                receiverWalkId = g.receiverWalkId,
                status = g.status,
                createdAt = g.createdAt,
                respondedAt = g.respondedAt,
                expiresAt = g.expiresAt,
            )
        }

        val result = handler.execute(command)

        assertEquals(1L, result.id)
        assertEquals(10L, result.senderUserId)
        assertEquals(20L, result.receiverUserId)
        assertEquals(GreetingStatus.PENDING, result.status)
        verify { eventPublisher.publishGreetingCreated(any()) }
    }

    @Test
    fun `expiresAt은 생성 시각으로부터 5분 후`() = runTest {
        coEvery { walksPort.getWalk(300L) } returns walkInfo
        coEvery { identityPort.isBlocked(10L, 20L) } returns false
        every { greetingRepository.findBySenderAndWalk(10L, 300L) } returns null
        val slot = slot<Greeting>()
        every { greetingRepository.save(capture(slot)) } answers {
            val g = slot.captured
            Greeting(
                id = 1L,
                senderUserId = g.senderUserId,
                senderDogId = g.senderDogId,
                receiverUserId = g.receiverUserId,
                receiverDogId = g.receiverDogId,
                receiverWalkId = g.receiverWalkId,
                status = g.status,
                createdAt = g.createdAt,
                respondedAt = g.respondedAt,
                expiresAt = g.expiresAt,
            )
        }

        handler.execute(command)

        val saved = slot.captured
        val secondsUntilExpiry = saved.expiresAt.epochSecond - saved.createdAt.epochSecond
        assert(secondsUntilExpiry in 299..301) { "expiresAt should be ~300s after createdAt, was $secondsUntilExpiry" }
    }

    @Test
    fun `차단된 사용자 → ForbiddenBlockedException`() = runTest {
        coEvery { walksPort.getWalk(300L) } returns walkInfo
        coEvery { identityPort.isBlocked(10L, 20L) } returns true

        assertThrows<ForbiddenBlockedException> {
            handler.execute(command)
        }
    }

    @Test
    fun `중복 인사 → GreetingDuplicateException`() = runTest {
        coEvery { walksPort.getWalk(300L) } returns walkInfo
        coEvery { identityPort.isBlocked(10L, 20L) } returns false
        val existing = Greeting(
            id = 99L,
            senderUserId = 10L,
            senderDogId = 100L,
            receiverUserId = 20L,
            receiverDogId = 200L,
            receiverWalkId = 300L,
            status = GreetingStatus.PENDING,
            expiresAt = Instant.now().plusSeconds(300),
        )
        every { greetingRepository.findBySenderAndWalk(10L, 300L) } returns existing

        assertThrows<GreetingDuplicateException> {
            handler.execute(command)
        }
    }

    @Test
    fun `자기 자신에게 인사 → SelfGreetingException`() = runTest {
        val selfWalkInfo = WalksPort.WalkInfo(walkId = 300L, userId = 10L, dogId = 100L)
        coEvery { walksPort.getWalk(300L) } returns selfWalkInfo

        assertThrows<SelfGreetingException> {
            handler.execute(command)
        }
    }
}
