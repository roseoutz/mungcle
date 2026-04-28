package com.mungcle.walks.infrastructure.scheduler

import com.mungcle.walks.domain.port.`in`.ExpireWalksUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertTrue

class WalkExpirySchedulerTest {

    private val expireWalksUseCase: ExpireWalksUseCase = mockk(relaxed = true)
    private val scheduler = WalkExpiryScheduler(expireWalksUseCase)

    @Test
    fun `expireWalks 호출 시 UseCase execute가 1회 실행`() {
        scheduler.expireWalks()

        verify(exactly = 1) { expireWalksUseCase.execute(any()) }
    }

    @Test
    fun `expireWalks 호출 시 현재 시각을 Instant로 전달`() {
        val before = Instant.now()
        val instantSlot = slot<Instant>()
        every { expireWalksUseCase.execute(capture(instantSlot)) } returns 0

        scheduler.expireWalks()

        val after = Instant.now()
        val captured = instantSlot.captured
        assertTrue(captured >= before, "전달된 Instant가 호출 전 시각보다 이르면 안 됨")
        assertTrue(captured <= after, "전달된 Instant가 호출 후 시각보다 늦으면 안 됨")
    }

    @Test
    fun `expireWalks 반복 호출 시 매번 UseCase execute 실행`() {
        scheduler.expireWalks()
        scheduler.expireWalks()
        scheduler.expireWalks()

        verify(exactly = 3) { expireWalksUseCase.execute(any()) }
    }
}
