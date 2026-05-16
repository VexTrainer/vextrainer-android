package com.vextrainer.android.presentation.ui.auth.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.domain.usecase.auth.RegisterUseCase
import com.vextrainer.android.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val userName:        String  = "",
    val email:           String  = "",
    val password:        String  = "",
    val confirmPassword: String  = "",
    val isLoading:       Boolean = false,
    val error:           String? = null,
    val registered:      Boolean = false   // true after success — shows confirm-email message
)

sealed class RegisterEvent {
    /** No longer used for navigation — success is shown inline. Kept for potential future use. */
    object Success : RegisterEvent()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegisterUiState(
            // Pre-fill email if passed from the login screen's Sign Up button
            email = savedStateHandle[Screen.Register.ARG_EMAIL] ?: ""
        )
    )
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>()
    val events = _events.receiveAsFlow()

    fun onUserNameChange(v: String)        = _uiState.update { it.copy(userName = v, error = null) }
    fun onEmailChange(v: String)           = _uiState.update { it.copy(email = v,    error = null) }
    fun onPasswordChange(v: String)        = _uiState.update { it.copy(password = v, error = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, error = null) }

    fun register() {
        val s = _uiState.value
        val err = when {
            s.userName.isBlank()            -> "Name is required."
            s.email.isBlank()               -> "Email is required."
            s.password.isBlank()            -> "Password is required."
            s.password.length < 8           -> "Password must be at least 8 characters."
            s.password != s.confirmPassword -> "Passwords do not match."
            else                            -> null
        }
        if (err != null) {
            _uiState.update { it.copy(error = err) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            registerUseCase(
                userName = s.userName.trim(),
                email    = s.email.trim(),
                phone    = null,          // phone field removed from UI
                password = s.password
            )
                .onSuccess {
                    // Show inline confirmation message — do NOT navigate to dashboard
                    _uiState.update { it.copy(isLoading = false, registered = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Registration failed.") }
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
