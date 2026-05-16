package com.vextrainer.android.data.remote.interceptor

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vextrainer.android.data.local.preferences.SecurePreferences
import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.auth.LoginDataDto
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val securePreferences: SecurePreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = securePreferences.getToken()
        val original = chain.request()

        val request = if (!token.isNullOrEmpty()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else original

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()
            val newToken = tryRefreshToken() ?: return chain.proceed(original)
            return chain.proceed(
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
            )
        }

        return response
    }

    /**
     * Uses a plain OkHttpClient (no interceptors) to avoid circular dependency.
     * Called on OkHttp's background thread — synchronous execute() is intentional.
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
            val type = Types.newParameterizedType(ApiResponse::class.java, LoginDataDto::class.java)
            val parsed = moshi.adapter<ApiResponse<LoginDataDto>>(type).fromJson(json) ?: return null

            if (parsed.success && parsed.data != null) {
                securePreferences.saveToken(parsed.data.token)
                securePreferences.saveRefreshToken(parsed.data.refreshToken)
                parsed.data.token
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
