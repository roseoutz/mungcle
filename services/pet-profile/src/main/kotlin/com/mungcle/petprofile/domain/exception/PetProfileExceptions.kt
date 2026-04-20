package com.mungcle.petprofile.domain.exception

sealed class PetProfileException(message: String) : RuntimeException(message)

class DogNotFoundException(id: Long) :
    PetProfileException("반려견을 찾을 수 없습니다: $id")

class DogNotOwnedException(dogId: Long, requesterId: Long) :
    PetProfileException("반려견($dogId)의 소유자가 아닙니다: $requesterId")

class InvalidTemperamentCountException(count: Int) :
    PetProfileException("성향은 1~3개 선택해야 합니다 (현재: $count)")
