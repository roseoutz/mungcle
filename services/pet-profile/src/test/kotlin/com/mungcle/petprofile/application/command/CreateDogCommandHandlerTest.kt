package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogLimitExceededException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.`in`.CreateDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CreateDogCommandHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = CreateDogCommandHandler(dogRepository)

    private val command = CreateDogUseCase.Command(
        ownerId = 1L,
        name = "뭉이",
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = listOf(Temperament.FRIENDLY, Temperament.GENTLE),
        sociability = 4,
    )

    @Test
    fun `반려견 등록 성공`() = runTest {
        coEvery { dogRepository.countByOwnerId(1L) } returns 0L
        coEvery { dogRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(command)

        assertEquals("뭉이", result.name)
        assertEquals(DogSize.LARGE, result.size)
        coVerify { dogRepository.save(any()) }
    }

    @Test
    fun `4마리 보유 시 5번째 등록 성공`() = runTest {
        coEvery { dogRepository.countByOwnerId(1L) } returns 4L
        coEvery { dogRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(command)

        assertEquals("뭉이", result.name)
    }

    @Test
    fun `5마리 보유 시 DogLimitExceededException`() = runTest {
        coEvery { dogRepository.countByOwnerId(1L) } returns 5L

        assertThrows<DogLimitExceededException> {
            handler.execute(command)
        }
    }

    @Test
    fun `6마리 이상 보유 시에도 DogLimitExceededException`() = runTest {
        coEvery { dogRepository.countByOwnerId(1L) } returns 10L

        assertThrows<DogLimitExceededException> {
            handler.execute(command)
        }
    }
}
