package com.mungcle.identity.application.command

import com.mungcle.identity.domain.model.Block
import com.mungcle.identity.domain.port.out.BlockRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateBlockCommandHandlerTest {

    private val blockRepository: BlockRepositoryPort = mockk()
    private val handler = CreateBlockCommandHandler(blockRepository)

    @Test
    fun `정상 차단 저장`() = runTest {
        every { blockRepository.existsByBlockerAndBlocked(1L, 2L) } returns false
        every { blockRepository.save(any()) } answers { firstArg() }

        handler.execute(blockerId = 1L, blockedId = 2L)

        verify { blockRepository.save(any()) }
    }

    @Test
    fun `이미 차단된 경우 멱등 처리 — 중복 저장 없음`() = runTest {
        every { blockRepository.existsByBlockerAndBlocked(1L, 2L) } returns true

        handler.execute(blockerId = 1L, blockedId = 2L)

        verify(exactly = 0) { blockRepository.save(any()) }
    }

    @Test
    fun `자기 자신 차단 시 Block 생성에서 예외 발생`() = runTest {
        every { blockRepository.existsByBlockerAndBlocked(1L, 1L) } returns false
        every { blockRepository.save(any()) } answers { firstArg() }

        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            handler.execute(blockerId = 1L, blockedId = 1L)
        }
    }
}
