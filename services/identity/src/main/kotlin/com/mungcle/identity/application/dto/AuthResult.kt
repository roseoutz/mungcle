package com.mungcle.identity.application.dto

import com.mungcle.identity.domain.model.User

data class AuthResult(
    val accessToken: String,
    val user: User
)
