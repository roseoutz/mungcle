package com.mungcle.walks.infrastructure.grpc.server

import com.mungcle.walks.domain.exception.WalkAlreadyActiveException
import com.mungcle.walks.domain.exception.WalkAlreadyEndedException
import com.mungcle.walks.domain.exception.WalkException
import com.mungcle.walks.domain.exception.WalkNotFoundException
import com.mungcle.walks.domain.exception.WalkNotOwnedException
import io.grpc.Status
import io.grpc.StatusException

fun WalkException.toStatusException(): StatusException = when (this) {
    is WalkNotFoundException -> StatusException(Status.NOT_FOUND.withDescription(message))
    is WalkAlreadyActiveException -> StatusException(Status.ALREADY_EXISTS.withDescription(message))
    is WalkAlreadyEndedException -> StatusException(Status.FAILED_PRECONDITION.withDescription(message))
    is WalkNotOwnedException -> StatusException(Status.PERMISSION_DENIED.withDescription(message))
}
