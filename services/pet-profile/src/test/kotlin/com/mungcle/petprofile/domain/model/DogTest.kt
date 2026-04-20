package com.mungcle.petprofile.domain.model

import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.exception.InvalidTemperamentCountException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DogTest {

    private fun createDog(
        ownerId: Long = 1L,
        temperaments: List<Temperament> = listOf(Temperament.FRIENDLY),
        sociability: Int = 3,
    ) = Dog(
        id = 100L,
        ownerId = ownerId,
        name = "초코",
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = temperaments,
        sociability = sociability,
    )

    @Test
    fun `정상 생성`() {
        val dog = createDog()
        assertEquals("초코", dog.name)
        assertEquals(DogSize.LARGE, dog.size)
    }

    @Test
    fun `성향 0개이면 예외`() {
        assertThrows<InvalidTemperamentCountException> {
            createDog(temperaments = emptyList())
        }
    }

    @Test
    fun `성향 4개이면 예외`() {
        assertThrows<InvalidTemperamentCountException> {
            createDog(temperaments = listOf(
                Temperament.FRIENDLY, Temperament.CALM,
                Temperament.ACTIVE, Temperament.CURIOUS,
            ))
        }
    }

    @Test
    fun `성향 1~3개는 정상`() {
        createDog(temperaments = listOf(Temperament.FRIENDLY))
        createDog(temperaments = listOf(Temperament.FRIENDLY, Temperament.CALM))
        createDog(temperaments = listOf(Temperament.FRIENDLY, Temperament.CALM, Temperament.ACTIVE))
    }

    @Test
    fun `사교성 0이면 예외`() {
        assertThrows<IllegalArgumentException> {
            createDog(sociability = 0)
        }
    }

    @Test
    fun `사교성 6이면 예외`() {
        assertThrows<IllegalArgumentException> {
            createDog(sociability = 6)
        }
    }

    @Test
    fun `소유자 확인 성공`() {
        val dog = createDog(ownerId = 1L)
        dog.verifyOwnership(1L)
    }

    @Test
    fun `소유자 불일치 시 예외`() {
        val dog = createDog(ownerId = 1L)
        assertThrows<DogNotOwnedException> {
            dog.verifyOwnership(999L)
        }
    }

    @Test
    fun `예방접종 사진 없으면 미등록`() {
        val dog = createDog()
        assertFalse(dog.isVaccinationRegistered())
    }

    @Test
    fun `예방접종 사진 있으면 등록`() {
        val dog = createDog().copy(vaccinationPhotoPath = "vaccinations/photo.jpg")
        assertTrue(dog.isVaccinationRegistered())
    }

    @Test
    fun `예방접종 사진이 빈 문자열이면 미등록`() {
        val dog = createDog().copy(vaccinationPhotoPath = "")
        assertFalse(dog.isVaccinationRegistered())
    }

    @Test
    fun `예방접종 사진이 공백이면 미등록`() {
        val dog = createDog().copy(vaccinationPhotoPath = "   ")
        assertFalse(dog.isVaccinationRegistered())
    }

    @Test
    fun `소프트 삭제`() {
        val dog = createDog()
        assertNull(dog.deletedAt)
        val deleted = dog.softDelete()
        assertNotNull(deleted.deletedAt)
    }
}
