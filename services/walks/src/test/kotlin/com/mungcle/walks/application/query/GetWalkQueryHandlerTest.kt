package com.mungcle.walks.application.query

import com.mungcle.common.domain.GridCell
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.model.Walk
import com.mungcle.walks.domain.model.WalkStatus
import com.mungcle.walks.domain.model.WalkType
import com.mungcle.walks.domain.port.out.WalkRepositoryPort
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

class GetWalkQueryHandlerTest {

    private val walkRepository: WalkRepositoryPort = mockk()
    private val handler = GetWalkQueryHandler(walkRepository)

    private val now = Instant.now()

    private fun createWalk(id: Long) = Walk(
        id = id,
        dogId = id * 10,
        userId = id * 100,
        type = WalkType.OPEN,
        gridCell = GridCell("10:20"),
        status = WalkStatus.ACTIVE,
        startedAt = now,
        endsAt = now.plus(Duration.ofMinutes(60)),
    )

    @Test
    fun `정상 조회 - 존재하는 walkId 반환`() {
        val walk = createWalk(1L)
        every { walkRepository.findById(1L) } returns walk

        val result = handler.execute(1L)

        assertEquals(walk, result)
    }

    @Test
    fun `존재하지 않는 walk → WalkNotFoundException`() {
        every { walkRepository.findById(999L) } returns null

        assertThrows<WalkNotFoundException> {
            handler.execute(999L)
        }
    }
}
