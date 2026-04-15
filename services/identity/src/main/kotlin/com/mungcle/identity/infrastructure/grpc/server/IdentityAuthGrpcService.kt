package com.mungcle.identity.infrastructure.grpc.server

import com.mungcle.identity.domain.port.`in`.AuthenticateKakaoUseCase
import com.mungcle.identity.domain.port.`in`.CreateBlockUseCase
import com.mungcle.identity.domain.port.`in`.CreateReportUseCase
import com.mungcle.identity.domain.port.`in`.DeleteBlockUseCase
import com.mungcle.identity.domain.port.`in`.DeleteUserUseCase
import com.mungcle.identity.domain.port.`in`.GetBlockedUserIdsUseCase
import com.mungcle.identity.domain.port.`in`.GetUserUseCase
import com.mungcle.identity.domain.port.`in`.GetUsersByIdsUseCase
import com.mungcle.identity.domain.port.`in`.IsBlockedUseCase
import com.mungcle.identity.domain.port.`in`.ListBlocksUseCase
import com.mungcle.identity.domain.port.`in`.LoginEmailUseCase
import com.mungcle.identity.domain.port.`in`.RegisterEmailUseCase
import com.mungcle.identity.domain.port.`in`.UpdatePushTokenUseCase
import com.mungcle.identity.domain.port.`in`.UpdateUserUseCase
import com.mungcle.identity.domain.port.`in`.ValidateTokenUseCase
import com.mungcle.proto.identity.v1.AuthResponse
import com.mungcle.proto.identity.v1.AuthenticateKakaoRequest
import com.mungcle.proto.identity.v1.BlockInfo
import com.mungcle.proto.identity.v1.CreateBlockRequest
import com.mungcle.proto.identity.v1.CreateBlockResponse
import com.mungcle.proto.identity.v1.CreateReportRequest
import com.mungcle.proto.identity.v1.CreateReportResponse
import com.mungcle.proto.identity.v1.DeleteBlockRequest
import com.mungcle.proto.identity.v1.DeleteBlockResponse
import com.mungcle.proto.identity.v1.DeleteUserRequest
import com.mungcle.proto.identity.v1.DeleteUserResponse
import com.mungcle.proto.identity.v1.GetBlockedUserIdsRequest
import com.mungcle.proto.identity.v1.GetBlockedUserIdsResponse
import com.mungcle.proto.identity.v1.GetUserRequest
import com.mungcle.proto.identity.v1.GetUsersByIdsRequest
import com.mungcle.proto.identity.v1.GetUsersByIdsResponse
import com.mungcle.proto.identity.v1.IdentityServiceGrpcKt
import com.mungcle.proto.identity.v1.IsBlockedRequest
import com.mungcle.proto.identity.v1.IsBlockedResponse
import com.mungcle.proto.identity.v1.ListBlocksRequest
import com.mungcle.proto.identity.v1.ListBlocksResponse
import com.mungcle.proto.identity.v1.LoginEmailRequest
import com.mungcle.proto.identity.v1.RegisterEmailRequest
import com.mungcle.proto.identity.v1.UpdatePushTokenRequest
import com.mungcle.proto.identity.v1.UpdatePushTokenResponse
import com.mungcle.proto.identity.v1.UpdateUserRequest
import com.mungcle.proto.identity.v1.UserInfo
import com.mungcle.proto.identity.v1.ValidateTokenRequest
import com.mungcle.proto.identity.v1.ValidateTokenResponse
import com.mungcle.proto.identity.v1.authResponse
import com.mungcle.proto.identity.v1.blockInfo
import com.mungcle.proto.identity.v1.createBlockResponse
import com.mungcle.proto.identity.v1.createReportResponse
import com.mungcle.proto.identity.v1.deleteBlockResponse
import com.mungcle.proto.identity.v1.deleteUserResponse
import com.mungcle.proto.identity.v1.getBlockedUserIdsResponse
import com.mungcle.proto.identity.v1.getUsersByIdsResponse
import com.mungcle.proto.identity.v1.isBlockedResponse
import com.mungcle.proto.identity.v1.listBlocksResponse
import com.mungcle.proto.identity.v1.updatePushTokenResponse
import com.mungcle.proto.identity.v1.userInfo
import com.mungcle.proto.identity.v1.validateTokenResponse
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService
import com.mungcle.identity.application.dto.AuthResult

@GrpcService
class IdentityAuthGrpcService(
    private val authenticateKakaoUseCase: AuthenticateKakaoUseCase,
    private val registerEmailUseCase: RegisterEmailUseCase,
    private val loginEmailUseCase: LoginEmailUseCase,
    private val validateTokenUseCase: ValidateTokenUseCase,
    private val updatePushTokenUseCase: UpdatePushTokenUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getUsersByIdsUseCase: GetUsersByIdsUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val createBlockUseCase: CreateBlockUseCase,
    private val deleteBlockUseCase: DeleteBlockUseCase,
    private val listBlocksUseCase: ListBlocksUseCase,
    private val getBlockedUserIdsUseCase: GetBlockedUserIdsUseCase,
    private val isBlockedUseCase: IsBlockedUseCase,
    private val createReportUseCase: CreateReportUseCase,
) : IdentityServiceGrpcKt.IdentityServiceCoroutineImplBase() {

    override suspend fun authenticateKakao(request: AuthenticateKakaoRequest): AuthResponse {
        val result = authenticateKakaoUseCase.execute(
            AuthenticateKakaoUseCase.Command(kakaoAccessToken = request.kakaoAccessToken)
        )
        return result.toAuthResponse()
    }

    override suspend fun registerEmail(request: RegisterEmailRequest): AuthResponse {
        val result = registerEmailUseCase.execute(
            RegisterEmailUseCase.Command(
                email = request.email,
                password = request.password,
                nickname = request.nickname,
            )
        )
        return result.toAuthResponse()
    }

    override suspend fun loginEmail(request: LoginEmailRequest): AuthResponse {
        val result = loginEmailUseCase.execute(
            LoginEmailUseCase.Command(
                email = request.email,
                password = request.password,
            )
        )
        return result.toAuthResponse()
    }

    override suspend fun validateToken(request: ValidateTokenRequest): ValidateTokenResponse {
        val userId = validateTokenUseCase.execute(
            ValidateTokenUseCase.Query(accessToken = request.accessToken)
        ) ?: throw StatusException(Status.UNAUTHENTICATED.withDescription("유효하지 않은 토큰입니다"))
        return validateTokenResponse {
            this.userId = userId
            this.valid = true
        }
    }

    override suspend fun updatePushToken(request: UpdatePushTokenRequest): UpdatePushTokenResponse {
        updatePushTokenUseCase.execute(
            UpdatePushTokenUseCase.Command(
                userId = request.userId,
                pushToken = request.pushToken,
            )
        )
        return updatePushTokenResponse { }
    }

    override suspend fun getUser(request: GetUserRequest): UserInfo {
        val user = getUserUseCase.execute(request.userId)
        return userInfo {
            id = user.id
            nickname = user.nickname
            neighborhood = user.neighborhood ?: ""
            profilePhotoUrl = user.profilePhotoPath ?: ""
        }
    }

    override suspend fun getUsersByIds(request: GetUsersByIdsRequest): GetUsersByIdsResponse {
        val users = getUsersByIdsUseCase.execute(request.userIdsList)
        return getUsersByIdsResponse {
            this.users += users.map { u ->
                userInfo {
                    id = u.id
                    nickname = u.nickname
                    neighborhood = u.neighborhood ?: ""
                    profilePhotoUrl = u.profilePhotoPath ?: ""
                }
            }
        }
    }

    override suspend fun updateUser(request: UpdateUserRequest): UserInfo {
        val user = updateUserUseCase.execute(
            UpdateUserUseCase.Command(
                userId = request.userId,
                nickname = if (request.hasNickname()) request.nickname else null,
                neighborhood = if (request.hasNeighborhood()) request.neighborhood else null,
                profilePhotoPath = if (request.hasProfilePhotoPath()) request.profilePhotoPath else null,
            )
        )
        return userInfo {
            id = user.id
            nickname = user.nickname
            neighborhood = user.neighborhood ?: ""
            profilePhotoUrl = user.profilePhotoPath ?: ""
        }
    }

    override suspend fun deleteUser(request: DeleteUserRequest): DeleteUserResponse {
        deleteUserUseCase.execute(request.userId)
        return deleteUserResponse { }
    }

    override suspend fun createBlock(request: CreateBlockRequest): CreateBlockResponse {
        createBlockUseCase.execute(request.blockerId, request.blockedId)
        return createBlockResponse { }
    }

    override suspend fun deleteBlock(request: DeleteBlockRequest): DeleteBlockResponse {
        deleteBlockUseCase.execute(request.blockerId, request.blockedId)
        return deleteBlockResponse { }
    }

    override suspend fun listBlocks(request: ListBlocksRequest): ListBlocksResponse {
        val blocks = listBlocksUseCase.execute(request.userId)
        return listBlocksResponse {
            this.blocks += blocks.map { b ->
                blockInfo {
                    blockedUserId = b.blockedId
                    blockedNickname = ""
                    createdAt = b.createdAt.epochSecond
                }
            }
        }
    }

    override suspend fun getBlockedUserIds(request: GetBlockedUserIdsRequest): GetBlockedUserIdsResponse {
        val ids = getBlockedUserIdsUseCase.execute(request.userId)
        return getBlockedUserIdsResponse {
            this.blockedUserIds += ids
        }
    }

    override suspend fun isBlocked(request: IsBlockedRequest): IsBlockedResponse {
        val blocked = isBlockedUseCase.execute(request.userIdA, request.userIdB)
        return isBlockedResponse { this.blocked = blocked }
    }

    override suspend fun createReport(request: CreateReportRequest): CreateReportResponse {
        createReportUseCase.execute(request.reporterId, request.reportedId, request.reason)
        return createReportResponse { }
    }

    private fun AuthResult.toAuthResponse(): AuthResponse = authResponse {
        accessToken = this@toAuthResponse.accessToken
        user = userInfo {
            id = this@toAuthResponse.user.id
            nickname = this@toAuthResponse.user.nickname
        }
    }
}
