package com.mungcle.identity.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class BlockTest {

    @Test
    fun `자기 자신 차단 시 예외 발생`() {
        assertThrows<IllegalArgumentException> {
            Block(blockerId = 1L, blockedId = 1L)
        }
    }

    @Test
    fun `서로 다른 사용자 차단 성공`() {
        val block = Block(blockerId = 1L, blockedId = 2L)
        assertEquals(1L, block.blockerId)
        assertEquals(2L, block.blockedId)
    }
}
