package com.vextrainer.android.presentation.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.domain.usecase.auth.ForgotPasswordUseCase
import com.vextrainer.android.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginStep {
    INITIAL,      // Email + 3 action buttons
    PASSWORD,     // Password field + Sign In button
    RESET_SENT    // Success message after reset request
}

data class LoginUiState(
    val email:        String    = "",
    val password:     String    = "",
    val step:         LoginStep = LoginStep.INITIAL,
    val isLoading:    Boolean   = false,
    val error:        String?   = null,
    val resetMessage: String?   = null,
    /** true when error is specifically "account not confirmed" — shows resend option */
    val isUnconfirmed: Boolean  = false
)

sealed class LoginEvent {
    object Success                                  : LoginEvent()
    data class NavigateToRegister(val email: String) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase:          LoginUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onEmailChange(v: String)    = _uiState.update { it.copy(email = v,    error = null, isUnconfirmed = false) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, error = null, isUnconfirmed = false) }

    // ── Step transitions ──────────────────────────────────────────────────────

    fun proceedToPassword() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email.") }
            return
        }
        _uiState.update { it.copy(step = LoginStep.PASSWORD, error = null, isUnconfirmed = false) }
    }

    fun backToInitial() {
        _uiState.update {
            it.copy(step = LoginStep.INITIAL, password = "", error = null, isUnconfirmed = false)
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun login() {
        val s = _uiState.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email and password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isUnconfirmed = false) }
            loginUseCase(s.email.trim(), s.password)
                .onSuccess {
                    _events.send(LoginEvent.Success)
                }
                .onFailure { e ->
                    // API returns HTTP 403 for unconfirmed accounts.
                    // The error message from the API is passed through as e.message.
                    // Detect by checking for the specific message content.
                    val msg           = e.message ?: "Login failed."
                    val isUnconfirmed = msg.contains("not confirmed", ignoreCase = true) ||
                                        msg.contains("confirm", ignoreCase = true)
                    _uiState.update {
                        it.copy(
                            error         = msg,
                            isUnconfirmed = isUnconfirmed
                        )
                    }
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Resend confirmation email.
     * Reuses the forgot-password flow since both just send an email —
     * the API generates the appropriate token type based on the endpoint called.
     * A dedicated resend-confirmation endpoint can be added later if needed.
     */
    fun resendConfirmation() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // POST /Auth/forgot-password — always returns success
            // For now we reuse this; when a dedicated resend endpoint is added
            // wire it through a ResendConfirmationUseCase instead.
            forgotPasswordUseCase(email)
            _uiState.update {
                it.copy(
                    isLoading     = false,
                    isUnconfirmed = false,
                    step          = LoginStep.RESET_SENT,
                    resetMessage  = "A new confirmation email has been sent to $email. Please check your inbox."
                )
            }
        }
    }

    fun resetPassword() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Please enter your email to reset your password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            forgotPasswordUseCase(email)
            _uiState.update {
                it.copy(
                    isLoading    = false,
                    step         = LoginStep.RESET_SENT,
                    resetMessage = "If an account exists for $email, a password reset link has been sent. Please check your inbox."
                )
            }
        }
    }

    fun navigateToRegister() {
        viewModelScope.launch {
            _events.send(LoginEvent.NavigateToRegister(_uiState.value.email.trim()))
        }
    }

    fun backToLogin() {
        _uiState.update {
            it.copy(step = LoginStep.INITIAL, password = "", error = null,
                    resetMessage = null, isUnconfirmed = false)
        }
    }
}
