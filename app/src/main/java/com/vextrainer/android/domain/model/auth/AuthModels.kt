package com.vextrainer.android.domain.model.auth

import com.vextrainer.android.data.remote.dto.auth.LoginDataDto

data class LoginData(
    val userId: Int,
    val userName: String,
    val email: String,
    val roleName: String
)

fun LoginDataDto.toLoginData() = LoginData(
    userId   = userId,
    userName = userName,
    email    = email,
    roleName = roleName
)
