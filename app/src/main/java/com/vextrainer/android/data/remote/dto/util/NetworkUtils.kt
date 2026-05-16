package com.vextrainer.android.data.remote.util

import com.vextrainer.android.data.remote.dto.ApiResponse

/**
 * Wraps an API call in a try/catch and converts ApiResponse<T> into Result<T>.
 * All repositories use this to avoid repeating the same error-handling boilerplate.
 */
suspend fun <T> safeApiCall(call: suspend () -> ApiResponse<T>): Result<T> {
    return try {
        val response = call()
        if (response.success && response.data != null) {
            Result.success(response.data)
        } else {
            Result.failure(Exception(response.message.ifBlank { "Unknown error" }))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Variant for endpoints that return no meaningful data payload (e.g. complete quiz
 * where we only care about success/failure, not the response body).
 */
suspend fun <T> safeApiCallUnit(call: suspend () -> ApiResponse<T>): Result<Unit> {
    return try {
        val response = call()
        if (response.success) Result.success(Unit)
        else Result.failure(Exception(response.message.ifBlank { "Unknown error" }))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
