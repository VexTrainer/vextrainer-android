package com.vextrainer.android.presentation.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vextrainer.android.R
import retrofit2.HttpException

/**
 * Wraps either a live API error message (string) or a local string resource.
 * ViewModels never hold hardcoded strings — they emit UiText and the Composable resolves it.
 */
sealed class UiText {
    /** Error message returned directly from the API response.message field. */
    data class ApiError(val message: String) : UiText()

    /** A string resource ID for locally-generated error messages. */
    data class ResourceString(@StringRes val id: Int) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is ApiError      -> message
        is ResourceString -> stringResource(id)
    }
}

/**
 * Converts a Throwable to a user-friendly UiText.
 *
 * Priority order:
 *  1. No connectivity (UnknownHostException, SocketException) -> "No internet" message
 *  2. Timeout (SocketTimeoutException)                        -> "Request timed out" message
 *  3. HTTP error (HttpException 4xx / 5xx)                    -> mapped to friendly message
 *  4. Non-blank exception message                             -> shown directly (API message)
 *  5. Everything else                                         -> caller-supplied fallback resource
 */
fun Throwable.toUiText(@StringRes fallbackResId: Int): UiText {
    return when {
        // No internet / DNS failure
        this is java.net.UnknownHostException ||
        this is java.net.SocketException ->
            UiText.ResourceString(R.string.error_network)

        // Request timed out
        this is java.net.SocketTimeoutException ->
            UiText.ResourceString(R.string.error_timeout)

        // HTTP error codes from the server
        // HttpException is what Retrofit throws on non-2xx responses.
        // Without this branch, the raw string "HTTP 404 " was shown to the user.
        this is HttpException -> when (this.code()) {
            404       -> UiText.ResourceString(R.string.error_not_found)
            408, 504  -> UiText.ResourceString(R.string.error_timeout)
            in 500..599 -> UiText.ResourceString(R.string.error_server)
            else      -> UiText.ResourceString(fallbackResId)
        }

        // API returned an error with a message
        !message.isNullOrBlank() ->
            UiText.ApiError(message!!)

        // Unknown error: use the caller-supplied fallback
        else ->
            UiText.ResourceString(fallbackResId)
    }
}
