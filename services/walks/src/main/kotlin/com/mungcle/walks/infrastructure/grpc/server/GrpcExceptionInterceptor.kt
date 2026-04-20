package com.mungcle.walks.infrastructure.grpc.server

import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.exception.WalkNotOwnedException
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
            is WalkNotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            is WalkAlreadyActiveException -> Status.ALREADY_EXISTS.withDescription(e.message)
            is WalkAlreadyEndedException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is WalkNotOwnedException -> Status.PERMISSION_DENIED.withDescription(e.message)
            else -> Status.INTERNAL.withDescription(e.message)
        }
        call.close(status, headers)
    }
}
