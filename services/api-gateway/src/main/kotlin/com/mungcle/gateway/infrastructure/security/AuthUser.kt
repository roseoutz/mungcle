package com.mungcle.gateway.infrastructure.security

import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthUser

@Component
class AuthUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AuthUser::class.java) && parameter.parameterType == Long::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> =
        ReactiveSecurityContextHolder.getContext()
            .map { ctx ->
                ctx.authentication?.principal as? Long
                    ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
            }
}
