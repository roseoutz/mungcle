package com.mungcle.gateway.infrastructure.grpc

import io.grpc.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class GrpcStatusConverterTest {

    @Test
    fun `NOT_FOUND는 404로 변환`() {
        assertEquals(HttpStatus.NOT_FOUND, GrpcStatusConverter.toHttpStatus(Status.Code.NOT_FOUND))
    }

    @Test
    fun `ALREADY_EXISTS는 409로 변환`() {
        assertEquals(HttpStatus.CONFLICT, GrpcStatusConverter.toHttpStatus(Status.Code.ALREADY_EXISTS))
    }

    @Test
    fun `PERMISSION_DENIED는 403으로 변환`() {
        assertEquals(HttpStatus.FORBIDDEN, GrpcStatusConverter.toHttpStatus(Status.Code.PERMISSION_DENIED))
    }

    @Test
    fun `INVALID_ARGUMENT는 400으로 변환`() {
        assertEquals(HttpStatus.BAD_REQUEST, GrpcStatusConverter.toHttpStatus(Status.Code.INVALID_ARGUMENT))
    }

    @Test
    fun `FAILED_PRECONDITION은 410으로 변환`() {
        assertEquals(HttpStatus.GONE, GrpcStatusConverter.toHttpStatus(Status.Code.FAILED_PRECONDITION))
    }

    @Test
    fun `UNAUTHENTICATED는 401로 변환`() {
        assertEquals(HttpStatus.UNAUTHORIZED, GrpcStatusConverter.toHttpStatus(Status.Code.UNAUTHENTICATED))
    }

    @Test
    fun `RESOURCE_EXHAUSTED는 429로 변환`() {
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, GrpcStatusConverter.toHttpStatus(Status.Code.RESOURCE_EXHAUSTED))
    }

    @Test
    fun `INTERNAL은 500으로 변환`() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, GrpcStatusConverter.toHttpStatus(Status.Code.INTERNAL))
    }

    @Test
    fun `UNKNOWN은 500으로 변환`() {
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, GrpcStatusConverter.toHttpStatus(Status.Code.UNKNOWN))
    }
}
