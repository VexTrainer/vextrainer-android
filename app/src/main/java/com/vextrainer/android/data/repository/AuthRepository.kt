package com.vextrainer.android.data.repository

import com.vextrainer.android.data.local.preferences.SecurePreferences
import com.vextrainer.android.data.remote.api.AuthApi
import com.vextrainer.android.data.remote.dto.auth.ChangePasswordRequestDto
import com.vextrainer.android.data.remote.dto.auth.LoginDataDto
import com.vextrainer.android.data.remote.dto.auth.LoginRequestDto
import com.vextrainer.android.data.remote.dto.auth.RegisterRequestDto
import com.vextrainer.android.data.remote.dto.auth.UpdateProfileRequestDto
import com.vextrainer.android.domain.model.auth.LoginData
import com.vextrainer.android.domain.model.auth.toLoginData
import com.vextrainer.android.data.remote.dto.auth.ForgotPasswordRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val securePreferences: SecurePreferences
) {
    suspend fun login(identifier: String, password: String): Result<LoginData> = safeCall {
        val response = authApi.login(LoginRequestDto(identifier, password))
        if (response.success && response.data != null) {
            saveSession(response.data)
            Result.success(response.data.toLoginData())
        } else Result.failure(Exception(response.message))
    }

    suspend fun register(
        userName: String, email: String, phone: String?, password: String
    ): Result<LoginData> = safeCall {
        val response = authApi.register(RegisterRequestDto(userName, email, phone, password))
        if (response.success && response.data != null) {
            saveSession(response.data)
            Result.success(response.data.toLoginData())
        } else Result.failure(Exception(response.message))
    }

    suspend fun logout(): Result<Unit> = safeCall {
        try { authApi.logout() } catch (_: Exception) { /* best-effort */ }
        securePreferences.clearAll()
        Result.success(Unit)
    }

    suspend fun forgotPassword(email: String): Result<Unit> = safeCall {
        val response = authApi.forgotPassword(ForgotPasswordRequestDto(email))
        if (response.success) Result.success(Unit)
        else Result.failure(Exception(response.message))
    }

    suspend fun updateProfile(email: String?, phone: String?): Result<Unit> = safeCall {
        val response = authApi.updateProfile(UpdateProfileRequestDto(email, phone))
        if (response.success) Result.success(Unit)
        else Result.failure(Exception(response.message))
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<Unit> = safeCall {
        val response = authApi.changePassword(ChangePasswordRequestDto(oldPassword, newPassword))
        if (response.success) Result.success(Unit)
        else Result.failure(Exception(response.message))
    }

    fun isLoggedIn(): Boolean  = securePreferences.isLoggedIn()
    fun getUserName(): String? = securePreferences.getUserName()
    fun getEmail(): String?    = securePreferences.getEmail()

    private fun saveSession(dto: LoginDataDto) {
        securePreferences.saveToken(dto.token)
        securePreferences.saveRefreshToken(dto.refreshToken)
        securePreferences.saveUserId(dto.userId)
        securePreferences.saveUserName(dto.userName)
        securePreferences.saveEmail(dto.email)
    }

    private inline fun <T> safeCall(block: () -> Result<T>): Result<T> = try {
        block()
    } catch (e: Exception) {
        Result.failure(e)
    }
}
