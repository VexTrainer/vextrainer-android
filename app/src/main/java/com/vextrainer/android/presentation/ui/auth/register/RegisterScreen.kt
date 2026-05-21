package com.vextrainer.android.presentation.ui.auth.register

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
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.vextrainer.android.R

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager     = LocalFocusManager.current
    var passwordVisible  by remember { mutableStateOf(false) }

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

            if (uiState.registered) {
                // ── Success / confirm-email state ─────────────────────────
                Icon(
                    imageVector        = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(72.dp)
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text       = "Check Your Email",
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text      = "We've sent a confirmation link to\n${uiState.email}\n\n" +
                                "Please check your inbox and click the link to activate your account.",
                    style     = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick  = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Go to Sign In", style = MaterialTheme.typography.labelLarge)
                }

            } else {
                // ── Logo ──────────────────────────────────────────────────
                Image(
                    painter            = painterResource(R.drawable.logo_vextrainer),
                    contentDescription = "VexTrainer",
                    modifier           = Modifier.size(88.dp)
                )

                Spacer(Modifier.height(16.dp))

                // ── Registration form ─────────────────────────────────────
                Text(
                    text  = "Create Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text     = "Join VexTrainer today",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                OutlinedTextField(
                    value           = uiState.userName,
                    onValueChange   = viewModel::onUserNameChange,
                    label           = { Text("Full Name") },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.error != null
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value           = uiState.email,
                    onValueChange   = viewModel::onEmailChange,
                    label           = { Text("Email") },
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = uiState.error != null
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value                = uiState.password,
                    onValueChange        = viewModel::onPasswordChange,
                    label                = { Text("Password") },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
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

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value                = uiState.confirmPassword,
                    onValueChange        = viewModel::onConfirmPasswordChange,
                    label                = { Text("Confirm Password") },
                    singleLine           = true,
                    modifier             = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions      = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction    = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus(); viewModel.register()
                    }),
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
                    onClick  = { focusManager.clearFocus(); viewModel.register() },
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
                        Text("Create Account", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text("Already have an account? Sign In")
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
