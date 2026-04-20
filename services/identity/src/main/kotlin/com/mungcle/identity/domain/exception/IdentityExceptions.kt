package com.mungcle.identity.domain.exception

sealed class IdentityException(message: String) : RuntimeException(message)

class EmailTakenException(email: String) :
    IdentityException("이미 사용 중인 이메일입니다: $email")

class InvalidCredentialsException :
    IdentityException("이메일 또는 비밀번호가 올바르지 않습니다")

class InvalidNicknameException(nickname: String) :
    IdentityException("유효하지 않은 닉네임입니다: $nickname")

class UserNotFoundException(id: Long) :
    IdentityException("사용자를 찾을 수 없습니다: $id")
