package com.mungcle.gateway.infrastructure.grpc

import com.mungcle.proto.identity.v1.AuthResponse
import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.ListBlocksResponse
import com.mungcle.proto.identity.v1.UserInfo
import com.mungcle.proto.identity.v1.ValidateTokenResponse
import com.mungcle.proto.identity.v1.authenticateKakaoRequest
import com.mungcle.proto.identity.v1.createBlockRequest
import com.mungcle.proto.identity.v1.createReportRequest
import com.mungcle.proto.identity.v1.deleteBlockRequest
import com.mungcle.proto.identity.v1.deleteUserRequest
import com.mungcle.proto.identity.v1.getBlockedUserIdsRequest
import com.mungcle.proto.identity.v1.getUserRequest
import com.mungcle.proto.identity.v1.getUsersByIdsRequest
import com.mungcle.proto.identity.v1.listBlocksRequest
import com.mungcle.proto.identity.v1.loginEmailRequest
import com.mungcle.proto.identity.v1.registerEmailRequest
import com.mungcle.proto.identity.v1.updatePushTokenRequest
import com.mungcle.proto.identity.v1.updateUserRequest
import com.mungcle.proto.identity.v1.validateTokenRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class IdentityClient(
    @GrpcClient("identity") private val stub: IdentityServiceGrpcKt.IdentityServiceCoroutineStub,
) {

    suspend fun authenticateKakao(kakaoAccessToken: String): AuthResponse =
        stub.authenticateKakao(authenticateKakaoRequest {
            this.kakaoAccessToken = kakaoAccessToken
        })

    suspend fun registerEmail(email: String, password: String, nickname: String): AuthResponse =
        stub.registerEmail(registerEmailRequest {
            this.email = email
            this.password = password
            this.nickname = nickname
        })

    suspend fun loginEmail(email: String, password: String): AuthResponse =
        stub.loginEmail(loginEmailRequest {
            this.email = email
            this.password = password
        })

    suspend fun validateToken(accessToken: String): ValidateTokenResponse =
        stub.validateToken(validateTokenRequest {
            this.accessToken = accessToken
        })

    suspend fun updatePushToken(userId: Long, pushToken: String) {
        stub.updatePushToken(updatePushTokenRequest {
            this.userId = userId
            this.pushToken = pushToken
        })
    }

    suspend fun getUser(userId: Long): UserInfo =
        stub.getUser(getUserRequest {
            this.userId = userId
        })

    suspend fun updateUser(
        userId: Long,
        nickname: String? = null,
        neighborhood: String? = null,
        profilePhotoPath: String? = null,
    ): UserInfo =
        stub.updateUser(updateUserRequest {
            this.userId = userId
            if (nickname != null) this.nickname = nickname
            if (neighborhood != null) this.neighborhood = neighborhood
            if (profilePhotoPath != null) this.profilePhotoPath = profilePhotoPath
        })

    suspend fun deleteUser(userId: Long) {
        stub.deleteUser(deleteUserRequest {
            this.userId = userId
        })
    }

    suspend fun createBlock(blockerId: Long, blockedId: Long) {
        stub.createBlock(createBlockRequest {
            this.blockerId = blockerId
            this.blockedId = blockedId
        })
    }

    suspend fun deleteBlock(blockerId: Long, blockedId: Long) {
        stub.deleteBlock(deleteBlockRequest {
            this.blockerId = blockerId
            this.blockedId = blockedId
        })
    }

    suspend fun listBlocks(userId: Long): ListBlocksResponse =
        stub.listBlocks(listBlocksRequest {
            this.userId = userId
        })

    suspend fun createReport(reporterId: Long, reportedId: Long, reason: String) {
        stub.createReport(createReportRequest {
            this.reporterId = reporterId
            this.reportedId = reportedId
            this.reason = reason
        })
    }

    suspend fun getBlockedUserIds(userId: Long): List<Long> =
        stub.getBlockedUserIds(getBlockedUserIdsRequest {
            this.userId = userId
        }).blockedUserIdsList

    suspend fun getUsersByIds(userIds: List<Long>): List<UserInfo> =
        stub.getUsersByIds(getUsersByIdsRequest {
            this.userIds += userIds
        }).usersList
}
