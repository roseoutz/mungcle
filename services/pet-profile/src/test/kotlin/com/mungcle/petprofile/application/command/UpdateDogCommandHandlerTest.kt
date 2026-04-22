package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.`in`.UpdateDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateDogCommandHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = UpdateDogCommandHandler(dogRepository)

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
    fun `정상 부분 업데이트`() {
        every { dogRepository.findById(100L) } returns existingDog
        val dogSlot = slot<Dog>()
        every { dogRepository.save(capture(dogSlot)) } answers { dogSlot.captured }

        val result = handler.execute(
            UpdateDogUseCase.Command(dogId = 100L, requesterId = 1L, name = "코코")
        )

        assertEquals("코코", result.name)
        assertEquals("골든리트리버", result.breed)
        verify(exactly = 1) { dogRepository.save(any()) }
    }

    @Test
    fun `존재하지 않는 반려견이면 예외`() {
        every { dogRepository.findById(999L) } returns null

        assertThrows<DogNotFoundException> {
            handler.execute(UpdateDogUseCase.Command(dogId = 999L, requesterId = 1L, name = "코코"))
        }
    }

    @Test
    fun `소유자가 아니면 예외`() {
        every { dogRepository.findById(100L) } returns existingDog

        assertThrows<DogNotOwnedException> {
            handler.execute(UpdateDogUseCase.Command(dogId = 100L, requesterId = 999L, name = "코코"))
        }
    }
}
