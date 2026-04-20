package com.mungcle.petprofile.application.command

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.`in`.UpdateDogUseCase
import com.mungcle.petprofile.domain.port.out.DogRepositoryPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class UpdateDogCommandHandlerTest {

    private val dogRepository: DogRepositoryPort = mockk()
    private val handler = UpdateDogCommandHandler(dogRepository)

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
    fun `이름만 부분 수정 성공`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog
        coEvery { dogRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(
            UpdateDogUseCase.Command(dogId = 100L, requesterId = 1L, name = "바둑이")
        )

        assertEquals("바둑이", result.name)
        assertEquals("골든리트리버", result.breed)
    }

    @Test
    fun `존재하지 않는 반려견 수정 시 DogNotFoundException`() = runTest {
        coEvery { dogRepository.findById(999L) } returns null

        assertThrows<DogNotFoundException> {
            handler.execute(
                UpdateDogUseCase.Command(dogId = 999L, requesterId = 1L, name = "바둑이")
            )
        }
    }

    @Test
    fun `타인 반려견 수정 시 DogNotOwnedException`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog

        assertThrows<DogNotOwnedException> {
            handler.execute(
                UpdateDogUseCase.Command(dogId = 100L, requesterId = 999L, name = "바둑이")
            )
        }
    }

    @Test
    fun `전체 필드 수정 성공`() = runTest {
        coEvery { dogRepository.findById(100L) } returns existingDog
        coEvery { dogRepository.save(any()) } answers { firstArg() }

        val result = handler.execute(
            UpdateDogUseCase.Command(
                dogId = 100L,
                requesterId = 1L,
                name = "바둑이",
                breed = "시바견",
                size = DogSize.MEDIUM,
                temperaments = listOf(Temperament.ACTIVE, Temperament.CURIOUS),
                sociability = 5,
                photoPath = "photo/new.jpg",
                vaccinationPhotoPath = "photo/vaccine.jpg",
            )
        )

        assertEquals("바둑이", result.name)
        assertEquals("시바견", result.breed)
        assertEquals(DogSize.MEDIUM, result.size)
        assertEquals(listOf(Temperament.ACTIVE, Temperament.CURIOUS), result.temperaments)
        assertEquals(5, result.sociability)
        coVerify { dogRepository.save(any()) }
    }
}
