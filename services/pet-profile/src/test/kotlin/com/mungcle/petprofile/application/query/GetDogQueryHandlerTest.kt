package com.mungcle.petprofile.application.query

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GetDogQueryHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = GetDogQueryHandler(dogRepository)

    private val existingDog = Dog(
        id = 100L,
        ownerId = 1L,
        name = "뭉이",
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = listOf(Temperament.FRIENDLY),
        sociability = 3,
    )

    @Test
    fun `반려견 조회 성공`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog

        val result = handler.execute(100L)

        assertEquals("뭉이", result.name)
        assertEquals(100L, result.id)
    }

    @Test
    fun `존재하지 않는 반려견 조회 시 DogNotFoundException`() = runTest {
        coEvery { dogRepository.findById(999L) } returns null

        assertThrows<DogNotFoundException> {
            handler.execute(999L)
        }
    }
}
