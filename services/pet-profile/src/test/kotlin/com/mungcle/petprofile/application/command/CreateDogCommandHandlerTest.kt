package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.`in`.CreateDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateDogCommandHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = CreateDogCommandHandler(dogRepository)

    private val command = CreateDogUseCase.Command(
        ownerId = 1L,
        name = "초코",
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = listOf(Temperament.FRIENDLY, Temperament.CALM),
        sociability = 4,
    )

    @Test
    fun `정상 반려견 등록`() = runTest {
        val dogSlot = slot<Dog>()
        coEvery { dogRepository.save(capture(dogSlot)) } answers { dogSlot.captured.copy(id = 100L) }

        val result = handler.execute(command)

        assertEquals(100L, result.id)
        assertEquals("초코", result.name)
        assertEquals(1L, result.ownerId)
        assertEquals(DogSize.LARGE, result.size)
        assertEquals(listOf(Temperament.FRIENDLY, Temperament.CALM), result.temperaments)
        assertEquals(4, result.sociability)
        coVerify(exactly = 1) { dogRepository.save(any()) }
    }

    @Test
    fun `사진 경로 포함 등록`() = runTest {
        val commandWithPhoto = command.copy(
            photoPath = "dogs/photo.jpg",
            vaccinationPhotoPath = "vaccinations/cert.jpg",
        )
        val dogSlot = slot<Dog>()
        coEvery { dogRepository.save(capture(dogSlot)) } answers { dogSlot.captured.copy(id = 101L) }

        val result = handler.execute(commandWithPhoto)

        assertEquals("dogs/photo.jpg", result.photoPath)
        assertEquals("vaccinations/cert.jpg", result.vaccinationPhotoPath)
    }
}
