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

class GetDogsByIdsQueryHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = GetDogsByIdsQueryHandler(dogRepository)

    private fun makeDog(id: Long, ownerId: Long, name: String) = Dog(
        id = id,
        ownerId = ownerId,
        name = name,
        breed = "포메라니안",
        size = DogSize.SMALL,
        temperaments = listOf(Temperament.CALM),
        sociability = 2,
    )

    @Test
    fun `여러 ID로 정상 조회`() {
        val ids = listOf(1L, 2L, 3L)
        val dogs = listOf(
            makeDog(1L, 10L, "초코"),
            makeDog(2L, 20L, "바둑이"),
            makeDog(3L, 30L, "콩이"),
        )
        every { dogRepository.findByIds(ids) } returns dogs

        val result = handler.execute(ids)

        assertEquals(3, result.size)
        assertEquals(listOf(1L, 2L, 3L), result.map { it.id })
    }

    @Test
    fun `빈 ID 리스트 입력 시 빈 리스트 반환`() {
        every { dogRepository.findByIds(emptyList()) } returns emptyList()

        val result = handler.execute(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `존재하지 않는 ID 포함 시 존재하는 것만 반환`() {
        val ids = listOf(1L, 999L)
        val existingDog = makeDog(1L, 10L, "초코")
        every { dogRepository.findByIds(ids) } returns listOf(existingDog)

        val result = handler.execute(ids)

        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
    }

    @Test
    fun `모든 ID가 존재하지 않으면 빈 리스트 반환`() {
        val ids = listOf(999L, 998L)
        every { dogRepository.findByIds(ids) } returns emptyList()

        val result = handler.execute(ids)

        assertTrue(result.isEmpty())
    }
}
