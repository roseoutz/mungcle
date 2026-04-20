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
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteDogCommandHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = DeleteDogCommandHandler(dogRepository)

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
    fun `정상 소프트 삭제`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog
        val dogSlot = slot<Dog>()
        coEvery { dogRepository.save(capture(dogSlot)) } answers { dogSlot.captured }

        handler.execute(100L, 1L)

        assertNotNull(dogSlot.captured.deletedAt)
        coVerify(exactly = 1) { dogRepository.save(any()) }
    }

    @Test
    fun `존재하지 않는 반려견이면 예외`() = runTest {
        coEvery { dogRepository.findById(999L) } returns null

        assertThrows<DogNotFoundException> {
            handler.execute(999L, 1L)
        }
    }

    @Test
    fun `소유자가 아니면 예외`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog

        assertThrows<DogNotOwnedException> {
            handler.execute(100L, 999L)
        }
    }
}
