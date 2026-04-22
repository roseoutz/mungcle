package com.mungcle.petprofile.domain.exception

import io.grpc.Status
import io.grpc.StatusException

sealed class PetProfileException(message: String) : RuntimeException(message)

class DogNotFoundException(id: Long) :
    PetProfileException("반려견을 찾을 수 없습니다: $id")

class DogNotOwnedException(dogId: Long, requesterId: Long) :
    PetProfileException("반려견($dogId)의 소유자가 아닙니다: $requesterId")

class InvalidTemperamentCountException(count: Int) :
    PetProfileException("성향은 1~3개 선택해야 합니다 (현재: $count)")

class DogLimitExceededException(ownerId: Long) :
    PetProfileException("반려견은 최대 5마리까지 등록할 수 있습니다 (소유자: $ownerId)")

fun PetProfileException.toStatusException(): StatusException = when (this) {
    is DogNotFoundException -> StatusException(Status.NOT_FOUND.withDescription(message))
    is DogNotOwnedException -> StatusException(Status.PERMISSION_DENIED.withDescription(message))
    is DogLimitExceededException -> StatusException(Status.RESOURCE_EXHAUSTED.withDescription(message))
    is InvalidTemperamentCountException -> StatusException(Status.INVALID_ARGUMENT.withDescription(message))
}
