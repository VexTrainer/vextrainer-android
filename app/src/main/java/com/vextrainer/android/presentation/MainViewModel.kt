package com.vextrainer.android.presentation

import androidx.lifecycle.ViewModel
import com.vextrainer.android.data.local.preferences.SecurePreferences
import com.vextrainer.android.di.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    securePreferences: SecurePreferences,
    sessionManager: SessionManager
) : ViewModel() {

    val isLoggedIn: Boolean = securePreferences.isLoggedIn()

    /** Collected in NavGraph to navigate to Login when the interceptor detects an expired session. */
    val sessionExpired = sessionManager.sessionExpired
}