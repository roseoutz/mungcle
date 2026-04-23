package com.mungcle.gateway.infrastructure.exception

import com.mungcle.gateway.infrastructure.grpc.GrpcStatusConverter
import com.mungcle.gateway.infrastructure.resilience.ServiceUnavailableException
import io.grpc.StatusException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

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

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(e: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(
            ErrorResponse(400, "VALIDATION_ERROR", fieldErrors)
        )
    }

    @ExceptionHandler(ServiceUnavailableException::class)
    fun handleServiceUnavailable(e: ServiceUnavailableException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .header("Retry-After", "10")
            .body(ErrorResponse(503, "SERVICE_UNAVAILABLE", e.message ?: "Service temporarily unavailable"))
}

data class ErrorResponse(
    val statusCode: Int,
    val code: String,
    val message: String,
)
