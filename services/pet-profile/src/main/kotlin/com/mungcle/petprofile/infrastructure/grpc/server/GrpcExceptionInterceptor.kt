package com.mungcle.petprofile.infrastructure.grpc.server

import com.mungcle.petprofile.domain.exception.DogLimitExceededException
import com.mungcle.petprofile.domain.exception.DogNotFoundException
import com.mungcle.petprofile.domain.exception.DogNotOwnedException
import com.mungcle.petprofile.domain.exception.InvalidTemperamentCountException
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.springframework.stereotype.Component

@Component
class GrpcExceptionInterceptor : ServerInterceptor {

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {
        val listener = next.startCall(call, headers)
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            override fun onHalfClose() {
                try {
                    super.onHalfClose()
                } catch (e: Exception) {
                    handleException(e, call, headers)
                }
            }
        }
    }

    private fun <ReqT, RespT> handleException(
        e: Exception,
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
    ) {
        val status = when (e) {
            is DogNotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            is DogNotOwnedException -> Status.PERMISSION_DENIED.withDescription(e.message)
            is DogLimitExceededException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is InvalidTemperamentCountException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            else -> Status.INTERNAL.withDescription(e.message)
        }
        call.close(status, headers)
    }
}
