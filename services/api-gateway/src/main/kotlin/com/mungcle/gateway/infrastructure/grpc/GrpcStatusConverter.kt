package com.mungcle.gateway.infrastructure.grpc

import io.grpc.Status
import org.springframework.http.HttpStatus

object GrpcStatusConverter {

    fun toHttpStatus(grpcStatus: Status.Code): HttpStatus = when (grpcStatus) {
        Status.Code.NOT_FOUND -> HttpStatus.NOT_FOUND
        Status.Code.ALREADY_EXISTS -> HttpStatus.CONFLICT
        Status.Code.PERMISSION_DENIED -> HttpStatus.FORBIDDEN
        Status.Code.INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST
        Status.Code.FAILED_PRECONDITION -> HttpStatus.GONE
        Status.Code.UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED
        Status.Code.RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS
        else -> HttpStatus.INTERNAL_SERVER_ERROR
    }
}
