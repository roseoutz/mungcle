package com.mungcle.petprofile.infrastructure.grpc.server

import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.exception.DogLimitExceededException
import com.mungcle.petprofile.domain.model.Dog
import com.mungcle.petprofile.domain.model.DogSize
import com.mungcle.petprofile.domain.model.Temperament
import com.mungcle.petprofile.domain.port.`in`.CreateDogUseCase
import com.mungcle.petprofile.domain.port.`in`.DeleteDogUseCase
import com.mungcle.petprofile.domain.port.`in`.GetDogUseCase
import com.mungcle.petprofile.domain.port.`in`.GetDogsByIdsUseCase
import com.mungcle.petprofile.domain.port.`in`.GetDogsByOwnerUseCase
import com.mungcle.petprofile.domain.port.`in`.UpdateDogUseCase
import com.mungcle.proto.petprofile.v1.CreateDogRequest
import com.mungcle.proto.petprofile.v1.DeleteDogRequest
import com.mungcle.proto.petprofile.v1.GetDogRequest
import com.mungcle.proto.petprofile.v1.GetDogsByIdsRequest
import com.mungcle.proto.petprofile.v1.GetDogsByOwnerRequest
import com.mungcle.proto.petprofile.v1.UpdateDogRequest
import com.mungcle.proto.petprofile.v1.DogSize as ProtoDogSize
import io.grpc.Status
import io.grpc.StatusException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PetProfileGrpcServiceTest {

    private val createDogUseCase: CreateDogUseCase = mockk()
    private val getDogUseCase: GetDogUseCase = mockk()
    private val getDogsByOwnerUseCase: GetDogsByOwnerUseCase = mockk()
    private val getDogsByIdsUseCase: GetDogsByIdsUseCase = mockk()
    private val updateDogUseCase: UpdateDogUseCase = mockk()
    private val deleteDogUseCase: DeleteDogUseCase = mockk()

    private val service = PetProfileGrpcService(
        createDogUseCase,
        getDogUseCase,
        getDogsByOwnerUseCase,
        getDogsByIdsUseCase,
        updateDogUseCase,
        deleteDogUseCase,
    )

    private fun makeDog(
        id: Long = 100L,
        ownerId: Long = 1L,
        name: String = "초코",
        breed: String = "골든리트리버",
        size: DogSize = DogSize.LARGE,
        temperaments: List<Temperament> = listOf(Temperament.FRIENDLY),
        sociability: Int = 3,
        photoPath: String? = null,
        vaccinationPhotoPath: String? = null,
    ) = Dog(
        id = id,
        ownerId = ownerId,
        name = name,
        breed = breed,
        size = size,
        temperaments = temperaments,
        sociability = sociability,
        photoPath = photoPath,
        vaccinationPhotoPath = vaccinationPhotoPath,
    )

    // ── createDog ────────────────────────────────────────────────────────────

    @Test
    fun `createDog 정상 등록`() = runTest {
        val request = CreateDogRequest.newBuilder()
            .setOwnerId(1L)
            .setName("초코")
            .setBreed("골든리트리버")
            .setSize(ProtoDogSize.DOG_SIZE_LARGE)
            .addTemperaments("FRIENDLY")
            .setSociability(3)
            .build()
        val dog = makeDog()
        every { createDogUseCase.execute(any()) } returns dog

        val result = service.createDog(request)

        assertEquals(100L, result.id)
        assertEquals("초코", result.name)
        assertEquals(1L, result.ownerId)
        assertEquals(ProtoDogSize.DOG_SIZE_LARGE, result.size)
    }

    @Test
    fun `createDog 등록 한도 초과 시 RESOURCE_EXHAUSTED 반환`() = runTest {
        val request = CreateDogRequest.newBuilder()
            .setOwnerId(1L)
            .setName("초코")
            .setBreed("골든리트리버")
            .setSize(ProtoDogSize.DOG_SIZE_LARGE)
            .addTemperaments("FRIENDLY")
            .setSociability(3)
            .build()
        every { createDogUseCase.execute(any()) } throws DogLimitExceededException(1L)

        val ex = assertThrows<StatusException> {
            service.createDog(request)
        }

        assertEquals(Status.Code.RESOURCE_EXHAUSTED, ex.status.code)
    }

    // ── getDog ───────────────────────────────────────────────────────────────

    @Test
    fun `getDog 정상 조회`() = runTest {
        val request = GetDogRequest.newBuilder().setDogId(100L).build()
        every { getDogUseCase.execute(100L) } returns makeDog()

        val result = service.getDog(request)

        assertEquals(100L, result.id)
        assertEquals("초코", result.name)
    }

    @Test
    fun `getDog 존재하지 않으면 NOT_FOUND 반환`() = runTest {
        val request = GetDogRequest.newBuilder().setDogId(999L).build()
        every { getDogUseCase.execute(999L) } throws DogNotFoundException(999L)

        val ex = assertThrows<StatusException> {
            service.getDog(request)
        }

        assertEquals(Status.Code.NOT_FOUND, ex.status.code)
    }

    // ── getDogsByOwner ───────────────────────────────────────────────────────

    @Test
    fun `getDogsByOwner 정상 조회 — 여러 마리`() = runTest {
        val request = GetDogsByOwnerRequest.newBuilder().setOwnerId(1L).build()
        val dogs = listOf(makeDog(10L, name = "초코"), makeDog(11L, name = "바둑이"))
        every { getDogsByOwnerUseCase.execute(1L) } returns dogs

        val result = service.getDogsByOwner(request)

        assertEquals(2, result.dogsList.size)
        assertEquals(listOf("초코", "바둑이"), result.dogsList.map { it.name })
    }

    @Test
    fun `getDogsByOwner 등록된 개 없으면 빈 응답`() = runTest {
        val request = GetDogsByOwnerRequest.newBuilder().setOwnerId(99L).build()
        every { getDogsByOwnerUseCase.execute(99L) } returns emptyList()

        val result = service.getDogsByOwner(request)

        assertTrue(result.dogsList.isEmpty())
    }

    // ── getDogsByIds ─────────────────────────────────────────────────────────

    @Test
    fun `getDogsByIds 여러 ID 정상 조회`() = runTest {
        val request = GetDogsByIdsRequest.newBuilder()
            .addAllDogIds(listOf(1L, 2L))
            .build()
        val dogs = listOf(makeDog(1L, name = "초코"), makeDog(2L, name = "바둑이"))
        every { getDogsByIdsUseCase.execute(listOf(1L, 2L)) } returns dogs

        val result = service.getDogsByIds(request)

        assertEquals(2, result.dogsList.size)
    }

    @Test
    fun `getDogsByIds 빈 ID 리스트 입력 시 빈 응답`() = runTest {
        val request = GetDogsByIdsRequest.newBuilder().build()
        every { getDogsByIdsUseCase.execute(emptyList()) } returns emptyList()

        val result = service.getDogsByIds(request)

        assertTrue(result.dogsList.isEmpty())
    }

    @Test
    fun `getDogsByIds 존재하지 않는 ID 포함 시 존재하는 것만 반환`() = runTest {
        val request = GetDogsByIdsRequest.newBuilder()
            .addAllDogIds(listOf(1L, 999L))
            .build()
        every { getDogsByIdsUseCase.execute(listOf(1L, 999L)) } returns listOf(makeDog(1L))

        val result = service.getDogsByIds(request)

        assertEquals(1, result.dogsList.size)
        assertEquals(1L, result.dogsList[0].id)
    }

    // ── updateDog ────────────────────────────────────────────────────────────

    @Test
    fun `updateDog 정상 업데이트`() = runTest {
        val request = UpdateDogRequest.newBuilder()
            .setDogId(100L)
            .setRequesterId(1L)
            .setName("흰둥이")
            .build()
        val updatedDog = makeDog(name = "흰둥이")
        every { updateDogUseCase.execute(any()) } returns updatedDog

        val result = service.updateDog(request)

        assertEquals("흰둥이", result.name)
    }

    @Test
    fun `updateDog 존재하지 않으면 NOT_FOUND 반환`() = runTest {
        val request = UpdateDogRequest.newBuilder()
            .setDogId(999L)
            .setRequesterId(1L)
            .build()
        every { updateDogUseCase.execute(any()) } throws DogNotFoundException(999L)

        val ex = assertThrows<StatusException> {
            service.updateDog(request)
        }

        assertEquals(Status.Code.NOT_FOUND, ex.status.code)
    }

    @Test
    fun `updateDog 소유자가 아니면 PERMISSION_DENIED 반환`() = runTest {
        val request = UpdateDogRequest.newBuilder()
            .setDogId(100L)
            .setRequesterId(999L)
            .build()
        every { updateDogUseCase.execute(any()) } throws DogNotOwnedException(100L, 999L)

        val ex = assertThrows<StatusException> {
            service.updateDog(request)
        }

        assertEquals(Status.Code.PERMISSION_DENIED, ex.status.code)
    }

    // ── deleteDog ────────────────────────────────────────────────────────────

    @Test
    fun `deleteDog 정상 삭제`() = runTest {
        val request = DeleteDogRequest.newBuilder()
            .setDogId(100L)
            .setRequesterId(1L)
            .build()
        every { deleteDogUseCase.execute(100L, 1L) } returns Unit

        val result = service.deleteDog(request)

        // DeleteDogResponse는 빈 메시지이므로 예외 없이 반환되면 성공
        verify(exactly = 1) { deleteDogUseCase.execute(100L, 1L) }
    }

    @Test
    fun `deleteDog 존재하지 않으면 NOT_FOUND 반환`() = runTest {
        val request = DeleteDogRequest.newBuilder()
            .setDogId(999L)
            .setRequesterId(1L)
            .build()
        every { deleteDogUseCase.execute(999L, 1L) } throws DogNotFoundException(999L)

        val ex = assertThrows<StatusException> {
            service.deleteDog(request)
        }

        assertEquals(Status.Code.NOT_FOUND, ex.status.code)
    }

    @Test
    fun `deleteDog 소유자가 아니면 PERMISSION_DENIED 반환`() = runTest {
        val request = DeleteDogRequest.newBuilder()
            .setDogId(100L)
            .setRequesterId(999L)
            .build()
        every { deleteDogUseCase.execute(100L, 999L) } throws DogNotOwnedException(100L, 999L)

        val ex = assertThrows<StatusException> {
            service.deleteDog(request)
        }

        assertEquals(Status.Code.PERMISSION_DENIED, ex.status.code)
    }

    // ── vaccinationRegistered 매핑 검증 ─────────────────────────────────────

    @Test
    fun `예방접종 사진 있으면 vaccinationRegistered true`() = runTest {
        val request = GetDogRequest.newBuilder().setDogId(100L).build()
        val dog = makeDog(vaccinationPhotoPath = "vaccinations/cert.jpg")
        every { getDogUseCase.execute(100L) } returns dog

        val result = service.getDog(request)

        assertTrue(result.vaccinationRegistered)
    }

    @Test
    fun `예방접종 사진 없으면 vaccinationRegistered false`() = runTest {
        val request = GetDogRequest.newBuilder().setDogId(100L).build()
        val dog = makeDog(vaccinationPhotoPath = null)
        every { getDogUseCase.execute(100L) } returns dog

        val result = service.getDog(request)

        assertFalse(result.vaccinationRegistered)
    }
}
