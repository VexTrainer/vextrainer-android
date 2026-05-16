package com.vextrainer.android.presentation.ui.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: (email: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val context            = LocalContext.current
    val focusManager       = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    val credentialManager  = remember { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.Success -> {
                    try {
                        credentialManager.createCredential(
                            context = context,
                            request = CreatePasswordRequest(uiState.email, uiState.password)
                        )
                    } catch (_: Exception) {}
                    onLoginSuccess()
                }
                is LoginEvent.NavigateToRegister -> onNavigateToRegister(event.email)
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text  = "VexTrainer",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text  = when (uiState.step) {
                    LoginStep.INITIAL    -> "Enter your email to get started"
                    LoginStep.PASSWORD   -> "Enter your password"
                    LoginStep.RESET_SENT -> "Check your email"
                },
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            when (uiState.step) {

                // ── INITIAL: email + 3 buttons ────────────────────────────
                LoginStep.INITIAL -> {
                    OutlinedButton_EmailField(
                        value         = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        isError       = uiState.error != null,
                        onDone        = { focusManager.clearFocus(); viewModel.proceedToPassword() }
                    )

                    uiState.error?.let { err ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text     = err,
                            color    = MaterialTheme.colorScheme.error,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Unconfirmed account — offer resend link
                        if (uiState.isUnconfirmed) {
                            TextButton(
                                onClick  = viewModel::resendConfirmation,
                                enabled  = !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.MarkEmailRead,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text     = "  Resend confirmation email",
                                    style    = MaterialTheme.typography.labelLarge,
                                    color    = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick  = { focusManager.clearFocus(); viewModel.proceedToPassword() },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Sign In", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick  = { focusManager.clearFocus(); viewModel.resetPassword() },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (uiState.isLoading)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else
                            Text("Reset Password", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick  = { focusManager.clearFocus(); viewModel.navigateToRegister() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Sign Up", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // ── PASSWORD: back + password field + Sign In ─────────────
                LoginStep.PASSWORD -> {
                    TextButton(
                        onClick  = viewModel::backToInitial,
                        modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier           = Modifier.size(18.dp)
                        )
                        Text(
                            text  = " ${uiState.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    androidx.compose.material3.OutlinedTextField(
                        value           = uiState.password,
                        onValueChange   = viewModel::onPasswordChange,
                        label           = { Text("Password") },
                        singleLine      = true,
                        modifier        = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus(); viewModel.login()
                        }),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    text  = if (passwordVisible) "Hide" else "Show",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        isError = uiState.error != null
                    )

                    uiState.error?.let { err ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text     = err,
                            color    = MaterialTheme.colorScheme.error,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick  = { focusManager.clearFocus(); viewModel.login() },
                        enabled  = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (uiState.isLoading)
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        else
                            Text("Sign In", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // ── RESET_SENT / CONFIRM_SENT ─────────────────────────────
                LoginStep.RESET_SENT -> {
                    Icon(
                        imageVector        = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint               = Color(0xFF2E7D32),
                        modifier           = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text      = uiState.resetMessage ?: "",
                        style     = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(32.dp))
                    TextButton(onClick = viewModel::backToLogin) {
                        Text(
                            text       = "Back to Sign In",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

// ── Extracted text field to reduce nesting ────────────────────────────────────

@Composable
private fun OutlinedButton_EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    onDone: () -> Unit
) {
    androidx.compose.material3.OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        label           = { Text("Email") },
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction    = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        isError         = isError
    )
}
