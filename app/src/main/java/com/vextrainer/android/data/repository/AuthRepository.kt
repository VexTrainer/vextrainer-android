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
        userName: String,
        email: String,
        phone: String?,
        password: String
    ): Result<LoginData> = safeCall {
        val response = authApi.register(RegisterRequestDto(userName, email, phone, password))
        if (response.success && response.data != null) {
            saveSession(response.data)
            Result.success(response.data.toLoginData())
        } else Result.failure(Exception(response.message))
    }

    suspend fun logout(): Result<Unit> = safeCall {
        // Clear the local session immediately — logout must always succeed locally
        // regardless of whether the server-side invalidation call succeeds.
        // This prevents the app from being stuck in a logged-in state if the API
        // returns 401 because the token is already expired.
        securePreferences.clearAll()
        try {
            authApi.logout()
        } catch (_: Exception) {
            // Best-effort: server-side token invalidation. Failure is silently ignored
            // because the local session has already been cleared.
        }
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

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Expiry-aware: returns false if the stored token has passed its expiry timestamp. */
    fun isLoggedIn(): Boolean  = securePreferences.isLoggedIn()
    fun getUserName(): String? = securePreferences.getUserName()
    fun getEmail(): String?    = securePreferences.getEmail()

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun saveSession(dto: LoginDataDto) {
        securePreferences.saveToken(dto.token)
        securePreferences.saveRefreshToken(dto.refreshToken)
        securePreferences.saveExpiryDate(dto.expiryDate)   // ← was previously missing
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
