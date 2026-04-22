package com.mungcle.gateway.infrastructure.exception

import com.mungcle.gateway.infrastructure.grpc.GrpcStatusConverter
import io.grpc.StatusException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(StatusException::class)
    fun handleGrpcStatus(e: StatusException): ResponseEntity<ErrorResponse> {
        val httpStatus = GrpcStatusConverter.toHttpStatus(e.status.code)
        val code = e.status.description?.substringBefore(" ") ?: e.status.code.name
        return ResponseEntity.status(httpStatus).body(
            ErrorResponse(httpStatus.value(), code, e.status.description ?: "")
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(
            ErrorResponse(400, "VALIDATION_ERROR", fieldErrors)
        )
    }
}

data class ErrorResponse(
    val statusCode: Int,
    val code: String,
    val message: String,
)
