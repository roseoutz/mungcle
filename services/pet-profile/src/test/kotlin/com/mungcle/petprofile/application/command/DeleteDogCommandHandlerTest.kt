package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteDogCommandHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = DeleteDogCommandHandler(dogRepository)

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
    fun `소프트 삭제 성공`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog
        coEvery { dogRepository.save(any()) } answers { firstArg() }

        handler.execute(100L, 1L)

        coVerify {
            dogRepository.save(match { it.deletedAt != null })
        }
    }

    @Test
    fun `존재하지 않는 반려견 삭제 시 DogNotFoundException`() = runTest {
        coEvery { dogRepository.findById(999L) } returns null

        assertThrows<DogNotFoundException> {
            handler.execute(999L, 1L)
        }
    }

    @Test
    fun `타인 반려견 삭제 시 DogNotOwnedException`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog

        assertThrows<DogNotOwnedException> {
            handler.execute(100L, 999L)
        }
    }
}
