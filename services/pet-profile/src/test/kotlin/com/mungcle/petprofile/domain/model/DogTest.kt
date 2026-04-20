package com.mungcle.petprofile.domain.model

import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.exception.InvalidTemperamentCountException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DogTest {

    private fun createDog(
        temperaments: List<Temperament> = listOf(Temperament.FRIENDLY),
        sociability: Int = 3,
        vaccinationPhotoPath: String? = null,
        ownerId: Long = 1L,
    ) = Dog(
        id = 100L,
        ownerId = ownerId,
        name = "뭉이",
        breed = "골든리트리버",
        size = DogSize.LARGE,
        temperaments = temperaments,
        sociability = sociability,
        vaccinationPhotoPath = vaccinationPhotoPath,
    )

    // ─── 성향 개수 검증 ──────────────────────────────────────────────────

    @Test
    fun `성향 1개 유효`() {
        assertDoesNotThrow {
            createDog(temperaments = listOf(Temperament.CALM))
        }
    }

    @Test
    fun `성향 2개 유효`() {
        assertDoesNotThrow {
            createDog(temperaments = listOf(Temperament.CALM, Temperament.GENTLE))
        }
    }

    @Test
    fun `성향 3개 유효`() {
        assertDoesNotThrow {
            createDog(temperaments = listOf(Temperament.CALM, Temperament.GENTLE, Temperament.FRIENDLY))
        }
    }

    @Test
    fun `성향 0개 InvalidTemperamentCountException`() {
        assertThrows<InvalidTemperamentCountException> {
            createDog(temperaments = emptyList())
        }
    }

    @Test
    fun `성향 4개 InvalidTemperamentCountException`() {
        assertThrows<InvalidTemperamentCountException> {
            createDog(temperaments = listOf(
                Temperament.CALM, Temperament.GENTLE, Temperament.FRIENDLY, Temperament.ACTIVE
            ))
        }
    }

    // ─── 사교성 검증 ────────────────────────────────────────────────────

    @Test
    fun `사교성 1 유효`() {
        assertDoesNotThrow { createDog(sociability = 1) }
    }

    @Test
    fun `사교성 5 유효`() {
        assertDoesNotThrow { createDog(sociability = 5) }
    }

    @Test
    fun `사교성 0 IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            createDog(sociability = 0)
        }
    }

    @Test
    fun `사교성 6 IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            createDog(sociability = 6)
        }
    }

    // ─── 예방접종 등록 여부 ──────────────────────────────────────────────

    @Test
    fun `예방접종 사진 있으면 등록됨`() {
        val dog = createDog(vaccinationPhotoPath = "photo/vaccine.jpg")
        assertTrue(dog.isVaccinationRegistered())
    }

    @Test
    fun `예방접종 사진 없으면 미등록`() {
        val dog = createDog(vaccinationPhotoPath = null)
        assertFalse(dog.isVaccinationRegistered())
    }

    @Test
    fun `예방접종 사진 빈 문자열이면 미등록`() {
        val dog = createDog(vaccinationPhotoPath = "")
        assertFalse(dog.isVaccinationRegistered())
    }

    @Test
    fun `예방접종 사진 공백 문자열이면 미등록`() {
        val dog = createDog(vaccinationPhotoPath = "   ")
        assertFalse(dog.isVaccinationRegistered())
    }

    // ─── 소유권 확인 ────────────────────────────────────────────────────

    @Test
    fun `소유자 일치 시 예외 없음`() {
        val dog = createDog(ownerId = 1L)
        assertDoesNotThrow { dog.verifyOwnership(1L) }
    }

    @Test
    fun `소유자 불일치 시 DogNotOwnedException`() {
        val dog = createDog(ownerId = 1L)
        assertThrows<DogNotOwnedException> {
            dog.verifyOwnership(999L)
        }
    }

    // ─── 소프트 삭제 ────────────────────────────────────────────────────

    @Test
    fun `소프트 삭제 시 deletedAt 설정`() {
        val dog = createDog()
        assertNull(dog.deletedAt)

        val deleted = dog.softDelete()
        assertNotNull(deleted.deletedAt)
        assertEquals(dog.name, deleted.name)
    }
}
