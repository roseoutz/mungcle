package com.mungcle.petprofile.application.query

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GetDogQueryHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = GetDogQueryHandler(dogRepository)

    private val existingDog = Dog(
        id = 100L,
        ownerId = 1L,
        name = "초코",
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = listOf(Temperament.FRIENDLY),
        sociability = 3,
    )

    @Test
    fun `정상 조회`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog

        val result = handler.execute(100L)

        assertEquals(100L, result.id)
        assertEquals("초코", result.name)
    }

    @Test
    fun `존재하지 않으면 예외`() = runTest {
        coEvery { dogRepository.findById(999L) } returns null

        assertThrows<DogNotFoundException> {
            handler.execute(999L)
        }
    }
}
