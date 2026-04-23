package com.mungcle.gateway.api

import com.mungcle.gateway.dto.UpdateUserRequest
import com.mungcle.gateway.dto.UserDetailResponse
import com.mungcle.gateway.dto.UserResponse
import com.mungcle.gateway.infrastructure.grpc.IdentityClient
import com.mungcle.gateway.infrastructure.grpc.PetProfileClient
import com.mungcle.gateway.infrastructure.security.AuthUser
import jakarta.validation.Valid
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val identityClient: IdentityClient,
    private val petProfileClient: PetProfileClient,
) {

    @GetMapping("/me")
    fun getMe(@AuthUser userId: Long): UserDetailResponse = runBlocking {
        val userDeferred = async { identityClient.getUser(userId) }
        val dogsDeferred = async { petProfileClient.getDogsByOwner(userId) }
        val user = userDeferred.await()
        val dogs = dogsDeferred.await()
        UserDetailResponse(
            id = user.id,
            nickname = user.nickname,
            neighborhood = user.neighborhood,
            profilePhotoUrl = user.profilePhotoUrl,
            dogCount = dogs.size,
        )
    }

    @PatchMapping("/me")
    fun updateMe(
        @AuthUser userId: Long,
        @Valid @RequestBody req: UpdateUserRequest,
    ): UserResponse = runBlocking {
        val user = identityClient.updateUser(
            userId = userId,
            nickname = req.nickname,
            neighborhood = req.neighborhood,
            profilePhotoPath = req.profilePhotoPath,
        )
        UserResponse(
            id = user.id,
            nickname = user.nickname,
            neighborhood = user.neighborhood,
            profilePhotoUrl = user.profilePhotoUrl,
        )
    }

    @DeleteMapping("/me")
    fun deleteMe(@AuthUser userId: Long): Unit = runBlocking {
        identityClient.deleteUser(userId)
    }
}
