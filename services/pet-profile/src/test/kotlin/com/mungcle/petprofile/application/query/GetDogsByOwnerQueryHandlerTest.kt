package com.mungcle.petprofile.application.query

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetDogsByOwnerQueryHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = GetDogsByOwnerQueryHandler(dogRepository)

    private fun makeDog(id: Long, ownerId: Long, name: String) = Dog(
        id = id,
        ownerId = ownerId,
        name = name,
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = listOf(Temperament.FRIENDLY),
        sociability = 3,
    )

    @Test
    fun `소유자에게 여러 마리 등록된 경우 전체 반환`() {
        val ownerId = 1L
        val dogs = listOf(
            makeDog(10L, ownerId, "초코"),
            makeDog(11L, ownerId, "바둑이"),
            makeDog(12L, ownerId, "콩이"),
        )
        every { dogRepository.findByOwnerId(ownerId) } returns dogs

        val result = handler.execute(ownerId)

        assertEquals(3, result.size)
        assertEquals(listOf("초코", "바둑이", "콩이"), result.map { it.name })
    }

    @Test
    fun `소유자에게 등록된 개가 없으면 빈 리스트 반환`() {
        every { dogRepository.findByOwnerId(999L) } returns emptyList()

        val result = handler.execute(999L)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `소유자에게 한 마리만 등록된 경우 단일 항목 반환`() {
        val ownerId = 2L
        every { dogRepository.findByOwnerId(ownerId) } returns listOf(makeDog(20L, ownerId, "흰둥이"))

        val result = handler.execute(ownerId)

        assertEquals(1, result.size)
        assertEquals(20L, result[0].id)
        assertEquals("흰둥이", result[0].name)
        assertEquals(ownerId, result[0].ownerId)
    }
}
