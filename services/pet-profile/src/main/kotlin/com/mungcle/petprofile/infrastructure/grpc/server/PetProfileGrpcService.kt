package com.mungcle.petprofile.infrastructure.grpc.server

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
import com.mungcle.proto.petprofile.v1.DeleteDogResponse
import com.mungcle.proto.petprofile.v1.DogInfo
import com.mungcle.proto.petprofile.v1.GetDogRequest
import com.mungcle.proto.petprofile.v1.GetDogsByIdsRequest
import com.mungcle.proto.petprofile.v1.GetDogsByOwnerRequest
import com.mungcle.proto.petprofile.v1.GetDogsResponse
import com.mungcle.proto.petprofile.v1.PetProfileServiceGrpcKt
import com.mungcle.proto.petprofile.v1.UpdateDogRequest
import com.mungcle.proto.petprofile.v1.deleteDogResponse
import com.mungcle.proto.petprofile.v1.dogInfo
import com.mungcle.proto.petprofile.v1.getDogsResponse
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class PetProfileGrpcService(
    private val createDogUseCase: CreateDogUseCase,
    private val getDogUseCase: GetDogUseCase,
    private val getDogsByOwnerUseCase: GetDogsByOwnerUseCase,
    private val getDogsByIdsUseCase: GetDogsByIdsUseCase,
    private val updateDogUseCase: UpdateDogUseCase,
    private val deleteDogUseCase: DeleteDogUseCase,
) : PetProfileServiceGrpcKt.PetProfileServiceCoroutineImplBase() {

    override suspend fun createDog(request: CreateDogRequest): DogInfo {
        val dog = createDogUseCase.execute(
            CreateDogUseCase.Command(
                ownerId = request.ownerId,
                name = request.name,
                breed = request.breed,
                size = toDomainSize(request.size),
                temperaments = request.temperamentsList.map { Temperament.valueOf(it) },
                sociability = request.sociability,
                photoPath = if (request.hasPhotoPath()) request.photoPath.ifBlank { null } else null,
                vaccinationPhotoPath = if (request.hasVaccinationPhotoPath()) request.vaccinationPhotoPath.ifBlank { null } else null,
            )
        )
        return dog.toDogInfo()
    }

    override suspend fun getDog(request: GetDogRequest): DogInfo {
        val dog = getDogUseCase.execute(request.dogId)
        return dog.toDogInfo()
    }

    override suspend fun getDogsByOwner(request: GetDogsByOwnerRequest): GetDogsResponse {
        val dogs = getDogsByOwnerUseCase.execute(request.ownerId)
        return getDogsResponse {
            this.dogs += dogs.map { it.toDogInfo() }
        }
    }

    override suspend fun getDogsByIds(request: GetDogsByIdsRequest): GetDogsResponse {
        val dogs = getDogsByIdsUseCase.execute(request.dogIdsList)
        return getDogsResponse {
            this.dogs += dogs.map { it.toDogInfo() }
        }
    }

    override suspend fun updateDog(request: UpdateDogRequest): DogInfo {
        val dog = updateDogUseCase.execute(
            UpdateDogUseCase.Command(
                dogId = request.dogId,
                requesterId = request.requesterId,
                name = if (request.hasName()) request.name else null,
                breed = if (request.hasBreed()) request.breed else null,
                size = if (request.hasSize()) toDomainSize(request.size) else null,
                temperaments = if (request.temperamentsList.isNotEmpty()) {
                    request.temperamentsList.map { Temperament.valueOf(it) }
                } else {
                    null
                },
                sociability = if (request.hasSociability()) request.sociability else null,
                photoPath = if (request.hasPhotoPath()) request.photoPath.ifBlank { null } else null,
                vaccinationPhotoPath = if (request.hasVaccinationPhotoPath()) request.vaccinationPhotoPath.ifBlank { null } else null,
            )
        )
        return dog.toDogInfo()
    }

    override suspend fun deleteDog(request: DeleteDogRequest): DeleteDogResponse {
        deleteDogUseCase.execute(request.dogId, request.requesterId)
        return deleteDogResponse { }
    }

    private fun Dog.toDogInfo(): DogInfo = dogInfo {
        id = this@toDogInfo.id
        ownerId = this@toDogInfo.ownerId
        name = this@toDogInfo.name
        breed = this@toDogInfo.breed
        size = toProtoSize(this@toDogInfo.size)
        temperaments += this@toDogInfo.temperaments.map { it.name }
        sociability = this@toDogInfo.sociability
        photoUrl = this@toDogInfo.photoPath ?: ""
        vaccinationRegistered = this@toDogInfo.isVaccinationRegistered()
    }

    private fun toDomainSize(protoSize: com.mungcle.proto.petprofile.v1.DogSize): DogSize =
        when (protoSize) {
            com.mungcle.proto.petprofile.v1.DogSize.DOG_SIZE_SMALL -> DogSize.SMALL
            com.mungcle.proto.petprofile.v1.DogSize.DOG_SIZE_MEDIUM -> DogSize.MEDIUM
            com.mungcle.proto.petprofile.v1.DogSize.DOG_SIZE_LARGE -> DogSize.LARGE
            else -> throw IllegalArgumentException("유효하지 않은 견종 크기입니다: $protoSize")
        }

    private fun toProtoSize(domainSize: DogSize): com.mungcle.proto.petprofile.v1.DogSize =
        when (domainSize) {
            DogSize.SMALL -> com.mungcle.proto.petprofile.v1.DogSize.DOG_SIZE_SMALL
            DogSize.MEDIUM -> com.mungcle.proto.petprofile.v1.DogSize.DOG_SIZE_MEDIUM
            DogSize.LARGE -> com.mungcle.proto.petprofile.v1.DogSize.DOG_SIZE_LARGE
        }
}
