package com.mungcle.identity.domain.exception

import com.mungcle.identity.domain.model.SocialProvider

sealed class IdentityException(message: String) : RuntimeException(message)

class EmailTakenException(email: String) :
    IdentityException("이미 사용 중인 이메일입니다: $email")

class InvalidCredentialsException :
    IdentityException("이메일 또는 비밀번호가 올바르지 않습니다")

class InvalidNicknameException(nickname: String) :
    IdentityException("유효하지 않은 닉네임입니다: $nickname")

class UserNotFoundException(id: Long) :
    IdentityException("사용자를 찾을 수 없습니다: $id")

class BlockSelfException :
    IdentityException("자기 자신을 차단할 수 없습니다")

class ReportSelfException :
    IdentityException("자기 자신을 신고할 수 없습니다")

class InvalidReportReasonException :
    IdentityException("신고 사유는 1~500자여야 합니다")

class UnsupportedProviderException(provider: SocialProvider) :
    IdentityException("지원하지 않는 소셜 로그인 프로바이더입니다: $provider")

class SocialAuthFailedException(provider: SocialProvider, reason: String) :
    IdentityException("소셜 로그인 실패 [$provider]: $reason")
