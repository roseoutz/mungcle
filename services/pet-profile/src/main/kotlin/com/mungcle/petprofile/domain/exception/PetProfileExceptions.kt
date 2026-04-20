package com.mungcle.petprofile.domain.exception

sealed class PetProfileException(message: String) : RuntimeException(message)

class DogNotFoundException(id: Long) :
    PetProfileException("반려견을 찾을 수 없습니다: $id")

class DogNotOwnedException(dogId: Long, requesterId: Long) :
    PetProfileException("반려견(${dogId})에 대한 권한이 없습니다: 요청자=$requesterId")

class DogLimitExceededException(ownerId: Long) :
    PetProfileException("반려견 등록 한도(5마리)를 초과했습니다: 유저=$ownerId")

class InvalidTemperamentCountException(count: Int) :
    PetProfileException("성향은 1~3개여야 합니다. 현재: $count")
