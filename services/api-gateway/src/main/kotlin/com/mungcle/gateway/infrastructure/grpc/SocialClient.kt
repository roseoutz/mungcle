package com.mungcle.gateway.infrastructure.grpc

import com.mungcle.proto.social.v1.GreetingDirection
import com.mungcle.proto.social.v1.GreetingInfo
import com.mungcle.proto.social.v1.GreetingStatus
import com.mungcle.proto.social.v1.MessageInfo
import com.mungcle.proto.social.v1.SocialServiceGrpcKt
import com.mungcle.proto.social.v1.createGreetingRequest
import com.mungcle.proto.social.v1.getGreetingRequest
import com.mungcle.proto.social.v1.listGreetingsRequest
import com.mungcle.proto.social.v1.listMessagesRequest
import com.mungcle.proto.social.v1.respondGreetingRequest
import com.mungcle.proto.social.v1.sendMessageRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class SocialClient(
    @GrpcClient("social") private val stub: SocialServiceGrpcKt.SocialServiceCoroutineStub,
) {

    suspend fun createGreeting(senderUserId: Long, senderDogId: Long, receiverWalkId: Long): GreetingInfo =
        stub.createGreeting(createGreetingRequest {
            this.senderUserId = senderUserId
            this.senderDogId = senderDogId
            this.receiverWalkId = receiverWalkId
        })

    suspend fun respondGreeting(greetingId: Long, responderUserId: Long, accept: Boolean): GreetingInfo =
        stub.respondGreeting(respondGreetingRequest {
            this.greetingId = greetingId
            this.responderUserId = responderUserId
            this.accept = accept
        })

    suspend fun getGreeting(greetingId: Long, userId: Long): GreetingInfo =
        stub.getGreeting(getGreetingRequest {
            this.greetingId = greetingId
            this.userId = userId
        })

    suspend fun listGreetings(
        userId: Long,
        statusFilter: GreetingStatus? = null,
        directionFilter: GreetingDirection? = null,
    ): List<GreetingInfo> =
        stub.listGreetings(listGreetingsRequest {
            this.userId = userId
            if (statusFilter != null) this.statusFilter = statusFilter
            if (directionFilter != null) this.directionFilter = directionFilter
        }).greetingsList

    suspend fun sendMessage(greetingId: Long, senderUserId: Long, body: String): MessageInfo =
        stub.sendMessage(sendMessageRequest {
            this.greetingId = greetingId
            this.senderUserId = senderUserId
            this.body = body
        })

    suspend fun listMessages(greetingId: Long, userId: Long): List<MessageInfo> =
        stub.listMessages(listMessagesRequest {
            this.greetingId = greetingId
            this.userId = userId
        }).messagesList
}
