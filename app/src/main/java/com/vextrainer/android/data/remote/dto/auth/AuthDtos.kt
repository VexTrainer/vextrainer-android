package com.vextrainer.android.data.remote.dto.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    val identifier: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequestDto(
    val userName: String,
    val email: String,
    val phone: String?,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequestDto(
    val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequestDto(
    val email: String?,
    val phone: String?
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String
)

@JsonClass(generateAdapter = true)
data class LoginDataDto(
    val userId: Int,
    val userName: String,
    val email: String,
    val token: String,
    val refreshToken: String,
    val expiryDate: String,
    val roleName: String
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequestDto(
    val email: String
)