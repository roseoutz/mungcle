package com.mungcle.social.domain.exception

sealed class SocialException(message: String) : RuntimeException(message)

class GreetingNotFoundException(id: Long) : SocialException("Greeting $id not found")

class GreetingDuplicateException(senderUserId: Long, receiverWalkId: Long) :
    SocialException("Duplicate greeting from user $senderUserId to walk $receiverWalkId")

class GreetingExpiredException(id: Long) : SocialException("Greeting $id is expired")

class GreetingNotPendingException(id: Long) : SocialException("Greeting $id is not pending")

class ForbiddenBlockedException(userId: Long) : SocialException("User $userId is blocked")

class SelfGreetingException(userId: Long) : SocialException("User $userId cannot greet themselves")

class GreetingAccessDeniedException(id: Long) : SocialException("Access denied to greeting $id")

class GreetingNotAcceptedException(id: Long) : SocialException("Greeting $id is not accepted or has expired")

class MessageTooLongException(length: Int) : SocialException("Message length $length exceeds limit of 140 characters")
