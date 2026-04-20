package com.mungcle.identity.infrastructure.grpc.server

import com.mungcle.identity.domain.port.`in`.AuthenticateKakaoUseCase
import com.mungcle.identity.domain.port.`in`.LoginEmailUseCase
import com.mungcle.identity.domain.port.`in`.RegisterEmailUseCase
import com.mungcle.identity.domain.port.`in`.UpdatePushTokenUseCase
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

    // 나머지 RPC는 task 02에서 구현 예정
    override suspend fun getUser(request: GetUserRequest): UserInfo =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun getUsersByIds(request: GetUsersByIdsRequest): GetUsersByIdsResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun updateUser(request: UpdateUserRequest): UserInfo =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun deleteUser(request: DeleteUserRequest): DeleteUserResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun createBlock(request: CreateBlockRequest): CreateBlockResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun deleteBlock(request: DeleteBlockRequest): DeleteBlockResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun listBlocks(request: ListBlocksRequest): ListBlocksResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun getBlockedUserIds(request: GetBlockedUserIdsRequest): GetBlockedUserIdsResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun isBlocked(request: IsBlockedRequest): IsBlockedResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    override suspend fun createReport(request: CreateReportRequest): CreateReportResponse =
        throw StatusException(Status.UNIMPLEMENTED)

    private fun AuthResult.toAuthResponse(): AuthResponse = authResponse {
        accessToken = this@toAuthResponse.accessToken
        user = userInfo {
            id = this@toAuthResponse.user.id
            nickname = this@toAuthResponse.user.nickname
        }
    }
}
