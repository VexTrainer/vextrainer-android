package com.vextrainer.android.presentation.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vextrainer.android.R

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
        is ApiError -> message
        is ResourceString -> stringResource(id)
    }
}

/**
 * Converts a Throwable to UiText.
 * - Network errors → generic network error string resource
 * - API errors (non-blank message) → shown directly
 * - Everything else → fallback resource ID passed by the caller
 */
fun Throwable.toUiText(@StringRes fallbackResId: Int): UiText {
    return when {
        this is java.net.UnknownHostException ||
        this is java.net.SocketException ||
        this is java.net.SocketTimeoutException ->
            UiText.ResourceString(R.string.error_network)
        !message.isNullOrBlank() ->
            UiText.ApiError(message!!)
        else ->
            UiText.ResourceString(fallbackResId)
    }
}
