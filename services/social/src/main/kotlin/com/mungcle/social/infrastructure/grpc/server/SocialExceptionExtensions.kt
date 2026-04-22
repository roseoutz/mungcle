package com.mungcle.social.infrastructure.grpc.server

import com.mungcle.social.domain.exception.ForbiddenBlockedException
import com.mungcle.social.domain.exception.GreetingAccessDeniedException
import com.mungcle.social.domain.exception.GreetingDuplicateException
import com.mungcle.social.domain.exception.GreetingExpiredException
import com.mungcle.social.domain.exception.GreetingNotFoundException
import com.mungcle.social.domain.exception.GreetingNotPendingException
import com.mungcle.social.domain.exception.SelfGreetingException
import com.mungcle.social.domain.exception.SocialException
import io.grpc.Status
import io.grpc.StatusException

fun SocialException.toStatusException(): StatusException = when (this) {
    is GreetingNotFoundException -> StatusException(Status.NOT_FOUND.withDescription(message))
    is GreetingDuplicateException -> StatusException(Status.ALREADY_EXISTS.withDescription(message))
    is GreetingExpiredException -> StatusException(Status.FAILED_PRECONDITION.withDescription(message))
    is GreetingNotPendingException -> StatusException(Status.FAILED_PRECONDITION.withDescription(message))
    is ForbiddenBlockedException -> StatusException(Status.PERMISSION_DENIED.withDescription(message))
    is GreetingAccessDeniedException -> StatusException(Status.PERMISSION_DENIED.withDescription(message))
    is SelfGreetingException -> StatusException(Status.INVALID_ARGUMENT.withDescription(message))
}
