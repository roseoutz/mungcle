package com.mungcle.social.infrastructure.grpc.server

import com.mungcle.proto.social.v1.CreateGreetingRequest
import com.mungcle.proto.social.v1.GetGreetingRequest
import com.mungcle.proto.social.v1.GreetingInfo
import com.mungcle.proto.social.v1.GreetingStatus as ProtoGreetingStatus
import com.mungcle.proto.social.v1.GreetingDirection
import com.mungcle.proto.social.v1.ListGreetingsRequest
import com.mungcle.proto.social.v1.ListGreetingsResponse
import com.mungcle.proto.social.v1.ListMessagesRequest
import com.mungcle.proto.social.v1.ListMessagesResponse
import com.mungcle.proto.social.v1.RespondGreetingRequest
import com.mungcle.proto.social.v1.SendMessageRequest
import com.mungcle.proto.social.v1.MessageInfo
import com.mungcle.proto.social.v1.SocialServiceGrpcKt
import com.mungcle.proto.social.v1.greetingInfo
import com.mungcle.proto.social.v1.listGreetingsResponse
import com.mungcle.social.domain.exception.SocialException
import com.mungcle.social.domain.model.Greeting
import com.mungcle.social.domain.model.GreetingStatus
import com.mungcle.social.domain.port.`in`.CreateGreetingUseCase
import com.mungcle.social.domain.port.`in`.GetGreetingUseCase
import com.mungcle.social.domain.port.`in`.ListGreetingsUseCase
import com.mungcle.social.domain.port.`in`.RespondGreetingUseCase
import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class SocialGrpcService(
    private val createGreetingUseCase: CreateGreetingUseCase,
    private val respondGreetingUseCase: RespondGreetingUseCase,
    private val getGreetingUseCase: GetGreetingUseCase,
    private val listGreetingsUseCase: ListGreetingsUseCase,
) : SocialServiceGrpcKt.SocialServiceCoroutineImplBase() {

    override suspend fun createGreeting(request: CreateGreetingRequest): GreetingInfo {
        try {
            val greeting = createGreetingUseCase.execute(
                CreateGreetingUseCase.Command(
                    senderUserId = request.senderUserId,
                    senderDogId = request.senderDogId,
                    receiverWalkId = request.receiverWalkId,
                )
            )
            return greeting.toGreetingInfo()
        } catch (e: SocialException) {
            throw e.toStatusException()
        }
    }

    override suspend fun respondGreeting(request: RespondGreetingRequest): GreetingInfo {
        try {
            val greeting = respondGreetingUseCase.execute(
                RespondGreetingUseCase.Command(
                    greetingId = request.greetingId,
                    responderUserId = request.responderUserId,
                    accept = request.accept,
                )
            )
            return greeting.toGreetingInfo()
        } catch (e: SocialException) {
            throw e.toStatusException()
        }
    }

    override suspend fun getGreeting(request: GetGreetingRequest): GreetingInfo {
        try {
            val greeting = getGreetingUseCase.execute(
                GetGreetingUseCase.Query(
                    greetingId = request.greetingId,
                    userId = request.userId,
                )
            )
            return greeting.toGreetingInfo()
        } catch (e: SocialException) {
            throw e.toStatusException()
        }
    }

    override suspend fun listGreetings(request: ListGreetingsRequest): ListGreetingsResponse {
        try {
            val statusFilter = when {
                request.hasStatusFilter() -> request.statusFilter.toDomain()
                else -> null
            }
            val isSender = when {
                request.hasDirectionFilter() -> request.directionFilter == GreetingDirection.GREETING_DIRECTION_SENT
                else -> null
            }
            val greetings = listGreetingsUseCase.execute(
                ListGreetingsUseCase.Query(
                    userId = request.userId,
                    statusFilter = statusFilter,
                    isSender = isSender,
                )
            )
            return listGreetingsResponse {
                this.greetings += greetings.map { it.toGreetingInfo() }
            }
        } catch (e: SocialException) {
            throw e.toStatusException()
        }
    }

    override suspend fun sendMessage(request: SendMessageRequest): MessageInfo {
        throw StatusException(Status.UNIMPLEMENTED.withDescription("태스크 08에서 구현 예정"))
    }

    override suspend fun listMessages(request: ListMessagesRequest): ListMessagesResponse {
        throw StatusException(Status.UNIMPLEMENTED.withDescription("태스크 08에서 구현 예정"))
    }

    private fun Greeting.toGreetingInfo(): GreetingInfo = greetingInfo {
        id = this@toGreetingInfo.id
        senderUserId = this@toGreetingInfo.senderUserId
        receiverUserId = this@toGreetingInfo.receiverUserId
        senderDogId = this@toGreetingInfo.senderDogId
        receiverDogId = this@toGreetingInfo.receiverDogId
        receiverWalkId = this@toGreetingInfo.receiverWalkId
        status = this@toGreetingInfo.status.toProto()
        createdAt = this@toGreetingInfo.createdAt.epochSecond
        this@toGreetingInfo.respondedAt?.let { respondedAt = it.epochSecond }
        this@toGreetingInfo.expiresAt.let { expiresAt = it.epochSecond }
    }

    private fun GreetingStatus.toProto(): ProtoGreetingStatus = when (this) {
        GreetingStatus.PENDING -> ProtoGreetingStatus.GREETING_STATUS_PENDING
        GreetingStatus.ACCEPTED -> ProtoGreetingStatus.GREETING_STATUS_ACCEPTED
        GreetingStatus.EXPIRED -> ProtoGreetingStatus.GREETING_STATUS_EXPIRED
    }

    private fun ProtoGreetingStatus.toDomain(): GreetingStatus? = when (this) {
        ProtoGreetingStatus.GREETING_STATUS_PENDING -> GreetingStatus.PENDING
        ProtoGreetingStatus.GREETING_STATUS_ACCEPTED -> GreetingStatus.ACCEPTED
        ProtoGreetingStatus.GREETING_STATUS_EXPIRED -> GreetingStatus.EXPIRED
        else -> null
    }
}
