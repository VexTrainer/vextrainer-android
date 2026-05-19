package com.vextrainer.android.di

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton event bus used by [AuthInterceptor] (data layer) to notify the UI layer
 * that the session has expired and the user must re-authenticate.
 *
 * Kept in the `di` package so it sits above both data and presentation layers
 * without creating an upward dependency.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Collected in NavGraph to navigate to the login screen on token expiry. */
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    /**
     * Called from [AuthInterceptor] on a background thread when a 401 is received
     * and the refresh token is also invalid/missing.
     * [MutableSharedFlow.tryEmit] is thread-safe and non-suspending.
     */
    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
