package com.mungcle.walks.domain.exception

sealed class WalkException(message: String) : RuntimeException(message)

class WalkAlreadyActiveException(dogId: Long) :
    WalkException("해당 반려견은 이미 산책 중입니다: $dogId")

class WalkNotFoundException(walkId: Long) :
    WalkException("산책을 찾을 수 없습니다: $walkId")

class WalkAlreadyEndedException(walkId: Long) :
    WalkException("이미 종료된 산책입니다: $walkId")

class WalkNotOwnedException(walkId: Long, userId: Long) :
    WalkException("해당 산책의 소유자가 아닙니다: walkId=$walkId, userId=$userId")
