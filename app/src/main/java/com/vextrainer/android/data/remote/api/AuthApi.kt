package com.vextrainer.android.data.remote.api

import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.auth.ForgotPasswordRequestDto
import com.vextrainer.android.data.remote.dto.auth.LoginDataDto
import com.vextrainer.android.data.remote.dto.auth.LoginRequestDto
import com.vextrainer.android.data.remote.dto.auth.RefreshTokenRequestDto
import com.vextrainer.android.data.remote.dto.auth.RegisterRequestDto
import com.vextrainer.android.data.remote.dto.auth.UpdateProfileRequestDto
import com.vextrainer.android.data.remote.dto.auth.ChangePasswordRequestDto
import retrofit2.http.PUT
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("Auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): ApiResponse<LoginDataDto>

    @POST("Auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): ApiResponse<LoginDataDto>

    @POST("Auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto
    ): ApiResponse<LoginDataDto>

    @POST("Auth/logout")
    suspend fun logout(): ApiResponse<Any?>

    @POST("Auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequestDto
    ): ApiResponse<Any?>

    @PUT("Auth/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequestDto
    ): ApiResponse<Any?>

    @POST("Auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequestDto
    ): ApiResponse<Any?>
}