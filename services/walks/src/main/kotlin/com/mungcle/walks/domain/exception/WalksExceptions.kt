package com.mungcle.walks.domain.exception

sealed class WalksException(message: String) : RuntimeException(message)

class WalkNotFoundException(walkId: Long) :
    WalksException("산책을 찾을 수 없습니다: $walkId")

class WalkAlreadyActiveException(dogId: Long) :
    WalksException("이미 활성 산책이 있습니다: dogId=$dogId")

class WalkAlreadyEndedException(walkId: Long) :
    WalksException("이미 종료된 산책입니다: $walkId")

class WalkNotOwnedException(walkId: Long, userId: Long) :
    WalksException("산책 종료 권한이 없습니다: walkId=$walkId, userId=$userId")
