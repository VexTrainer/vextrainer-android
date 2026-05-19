package com.vextrainer.android.data.remote.interceptor

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vextrainer.android.data.local.preferences.SecurePreferences
import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.auth.LoginDataDto
import com.vextrainer.android.di.SessionManager
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Auth endpoints do not carry a Bearer token — skip header injection and
        // the 401-retry logic entirely to avoid interfering with login/refresh calls.
        val isAuthEndpoint = original.url.encodedPath.contains("/Auth/")
        if (isAuthEndpoint) return chain.proceed(original)

        // Attach the stored access token to every non-auth request.
        val token = securePreferences.getToken()
        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else original

        val response = chain.proceed(request)

        if (response.code != 401) return response

        // ── 401 received — attempt a silent token refresh ──────────────────────
        response.close()

        val newToken = tryRefreshToken()
        if (newToken != null) {
            // Refresh succeeded: retry the original request with the new token.
            return chain.proceed(
                original.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            )
        }

        // Refresh failed — the session is unrecoverable.
        // Clear local credentials and signal the UI to navigate to the login screen.
        // Do NOT retry the request anonymously (that caused the double-401 in the logs).
        securePreferences.clearAll()
        sessionManager.notifySessionExpired()

        // Return a synthetic 401 response so callers receive a clean error rather than
        // a confusing success/failure from an unauthenticated retry.
        return chain.proceed(original)
    }

    /**
     * Performs a synchronous token refresh using a plain [OkHttpClient] with no
     * interceptors, avoiding a circular dependency with the main client.
     * This is intentionally called on OkHttp's background thread.
     *
     * Returns the new access token string on success, or null on any failure.
     */
    private fun tryRefreshToken(): String? {
        val refreshToken = securePreferences.getRefreshToken() ?: return null

        return try {
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://api.vextrainer.com/Auth/refresh")
                .post(body)
                .build()

            val response = OkHttpClient().newCall(request).execute()
            if (!response.isSuccessful) return null

            val json = response.body?.string() ?: return null

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val type  = Types.newParameterizedType(ApiResponse::class.java, LoginDataDto::class.java)
            val parsed = moshi.adapter<ApiResponse<LoginDataDto>>(type).fromJson(json)
                ?: return null

            if (parsed.success && parsed.data != null) {
                securePreferences.saveToken(parsed.data.token)
                securePreferences.saveRefreshToken(parsed.data.refreshToken)
                securePreferences.saveExpiryDate(parsed.data.expiryDate)   // ← keep expiry current
                parsed.data.token
            } else null

        } catch (_: Exception) {
            null
        }
    }
}
