package com.mungcle.gateway.infrastructure.grpc

import com.mungcle.proto.petprofile.v1.DogInfo
import com.mungcle.proto.petprofile.v1.DogSize
import com.mungcle.proto.petprofile.v1.PetProfileServiceGrpcKt
import com.mungcle.proto.petprofile.v1.createDogRequest
import com.mungcle.proto.petprofile.v1.deleteDogRequest
import com.mungcle.proto.petprofile.v1.getDogRequest
import com.mungcle.proto.petprofile.v1.getDogsByIdsRequest
import com.mungcle.proto.petprofile.v1.getDogsByOwnerRequest
import com.mungcle.proto.petprofile.v1.updateDogRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class PetProfileClient(
    @GrpcClient("pet-profile") private val stub: PetProfileServiceGrpcKt.PetProfileServiceCoroutineStub,
) {

    suspend fun createDog(
        ownerId: Long,
        dogName: String,
        dogBreed: String,
        dogSize: DogSize,
        dogTemperaments: List<String>,
        dogSociability: Int,
        photoPath: String? = null,
        vaccinationPhotoPath: String? = null,
    ): DogInfo =
        stub.createDog(createDogRequest {
            this.ownerId = ownerId
            this.name = dogName
            this.breed = dogBreed
            this.size = dogSize
            this.temperaments += dogTemperaments
            this.sociability = dogSociability
            if (photoPath != null) this.photoPath = photoPath
            if (vaccinationPhotoPath != null) this.vaccinationPhotoPath = vaccinationPhotoPath
        })

    suspend fun getDog(dogId: Long): DogInfo =
        stub.getDog(getDogRequest {
            this.dogId = dogId
        })

    suspend fun getDogsByOwner(ownerId: Long): List<DogInfo> =
        stub.getDogsByOwner(getDogsByOwnerRequest {
            this.ownerId = ownerId
        }).dogsList

    suspend fun updateDog(
        dogId: Long,
        requesterId: Long,
        dogName: String? = null,
        dogBreed: String? = null,
        dogSize: DogSize? = null,
        dogTemperaments: List<String>? = null,
        dogSociability: Int? = null,
        photoPath: String? = null,
        vaccinationPhotoPath: String? = null,
    ): DogInfo =
        stub.updateDog(updateDogRequest {
            this.dogId = dogId
            this.requesterId = requesterId
            if (dogName != null) this.name = dogName
            if (dogBreed != null) this.breed = dogBreed
            if (dogSize != null) this.size = dogSize
            if (dogTemperaments != null) this.temperaments += dogTemperaments
            if (dogSociability != null) this.sociability = dogSociability
            if (photoPath != null) this.photoPath = photoPath
            if (vaccinationPhotoPath != null) this.vaccinationPhotoPath = vaccinationPhotoPath
        })

    suspend fun getDogsByIds(dogIds: List<Long>): List<DogInfo> =
        stub.getDogsByIds(getDogsByIdsRequest {
            this.dogIds += dogIds
        }).dogsList

    suspend fun deleteDog(dogId: Long, ownerId: Long) {
        stub.deleteDog(deleteDogRequest {
            this.dogId = dogId
            this.requesterId = ownerId
        })
    }
}
