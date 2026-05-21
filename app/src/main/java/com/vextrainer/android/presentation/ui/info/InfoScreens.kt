package com.vextrainer.android.presentation.ui.info

import android.content.Intent
import android.net.Uri
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.BuildConfig
import com.vextrainer.android.R
import com.vextrainer.android.domain.usecase.contact.SendContactMessageUseCase
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

// ── Shared helpers ────────────────────────────────────────────────────────────

private fun openUrl(context: android.content.Context, url: String) {
    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    catch (_: Exception) {}
}

private suspend fun fetchMarkdown(url: String): String? = withContext(Dispatchers.IO) {
    try {
        val response = OkHttpClient()
            .newCall(Request.Builder().url(url).build())
            .execute()
        if (response.isSuccessful) response.body?.string() else null
    } catch (_: Exception) { null }
}

// ── Generic markdown content screen ──────────────────────────────────────────

@Composable
private fun MarkdownContentScreen(
    title: String,
    contentUrl: String,
    fallbackMarkdown: String,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    bottomContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isDark  = isSystemInDarkTheme()

    var markdown  by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }

    val markwon      = remember(context, isDark) { Markwon.builder(context).build() }
    val textColorInt = MaterialTheme.colorScheme.onBackground.toArgb()
    val bgColorInt   = MaterialTheme.colorScheme.background.toArgb()

    LaunchedEffect(contentUrl) {
        isLoading = true
        val fetched = fetchMarkdown(contentUrl)
        if (!fetched.isNullOrBlank()) {
            markdown  = fetched
            isOffline = false
        } else {
            markdown  = fallbackMarkdown
            isOffline = true
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = title,
                onLogoClick = onHomeClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> LoadingOverlay()
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    if (isOffline) {
                        Surface(
                            color    = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text     = stringResource(R.string.content_offline_notice),
                                style    = MaterialTheme.typography.bodySmall,
                                color    = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    AndroidView(
                        factory = { ctx ->
                            TextView(ctx).apply {
                                textSize = 16f
                                setLineSpacing(0f, 1.4f)
                            }
                        },
                        update = { tv ->
                            tv.setTextColor(textColorInt)
                            tv.setBackgroundColor(bgColorInt)
                            markwon.setMarkdown(tv, markdown)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    bottomContent?.let {
                        Spacer(Modifier.height(24.dp))
                        it()
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ── About ─────────────────────────────────────────────────────────────────────

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context    = LocalContext.current
    val fallback   = stringResource(R.string.about_fallback, BuildConfig.VERSION_NAME)
    val contentUrl = stringResource(R.string.about_content_url)
    val websiteUrl = stringResource(R.string.about_website_url)

    MarkdownContentScreen(
        title            = stringResource(R.string.about_title),
        contentUrl       = contentUrl,
        fallbackMarkdown = fallback,
        onBack           = onBack,
        onHomeClick      = onHomeClick,
        bottomContent    = {
            OutlinedButton(
                onClick  = { openUrl(context, websiteUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInBrowser,
                     contentDescription = stringResource(R.string.cd_external_link),
                     modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(websiteUrl)
            }
        }
    )
}

// ── Privacy ───────────────────────────────────────────────────────────────────

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit
) {
    MarkdownContentScreen(
        title            = stringResource(R.string.privacy_title),
        contentUrl       = stringResource(R.string.privacy_content_url),
        fallbackMarkdown = stringResource(R.string.privacy_fallback),
        onBack           = onBack,
        onHomeClick      = onHomeClick
    )
}

// ── Donate ────────────────────────────────────────────────────────────────────

@Composable
fun DonateScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit
) {
    val context   = LocalContext.current
    val donateUrl = stringResource(R.string.donate_url)

    MarkdownContentScreen(
        title            = stringResource(R.string.donate_title),
        contentUrl       = stringResource(R.string.donate_content_url),
        fallbackMarkdown = stringResource(R.string.donate_fallback),
        onBack           = onBack,
        onHomeClick      = onHomeClick,
        bottomContent    = {
            Button(
                onClick  = { openUrl(context, donateUrl) },
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null,
                     modifier = Modifier.size(20.dp))
                Text(text = stringResource(R.string.donate_button_label),
                     modifier = Modifier.padding(start = 8.dp),
                     style = MaterialTheme.typography.titleLarge)
            }
        }
    )
}

// ── Contact Us ────────────────────────────────────────────────────────────────

@Composable
fun ContactUsScreen(
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    sendContactMessage: SendContactMessageUseCase = hiltViewModel<ContactUsViewModel>().sendUseCase
) {
    val focusManager = LocalFocusManager.current
    val snackbar     = remember { SnackbarHostState() }
    val scope        = rememberCoroutineScope()

    val profileViewModel: com.vextrainer.android.presentation.ui.profile.ProfileViewModel =
        hiltViewModel()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

    val categories = listOf("Suggestion", "Correction", "Other")
    var category   by rememberSaveable { mutableStateOf("Suggestion") }
    var message    by rememberSaveable { mutableStateOf("") }
    var msgError   by remember { mutableStateOf<String?>(null) }
    var isSending  by remember { mutableStateOf(false) }
    var sent       by remember { mutableStateOf(false) }

    val maxChars   = 2000
    val errorMsg   = stringResource(R.string.contact_error_message)
    val successMsg = stringResource(R.string.contact_success)
    val unknownErr = stringResource(R.string.error_unknown)

    fun sendMessage() {
        msgError = if (message.isBlank()) errorMsg else null
        if (msgError != null) return
        isSending = true
        scope.launch {
            sendContactMessage(category, message)
                .onSuccess {
                    isSending = false; sent = true; message = ""
                    snackbar.showSnackbar(successMsg)
                }
                .onFailure { e ->
                    isSending = false
                    snackbar.showSnackbar(e.message ?: unknownErr)
                }
        }
    }

    Scaffold(
        topBar       = {
            VexTopAppBar(
                title       = stringResource(R.string.contact_title),
                onLogoClick = onHomeClick
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = profileState.userName.ifBlank { "—" },
                         style = MaterialTheme.typography.bodyLarge,
                         color = MaterialTheme.colorScheme.onSurface)
                    Text(text = profileState.email.ifBlank { "—" },
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column {
                Text(text = stringResource(R.string.contact_field_subject),
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier.padding(bottom = 6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { option ->
                        val selected = category == option
                        OutlinedButton(
                            onClick  = { category = option },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(8.dp),
                            border   = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                            ),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(text = option, style = MaterialTheme.typography.labelLarge,
                                 color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                         else MaterialTheme.colorScheme.onSurface,
                                 fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            OutlinedTextField(
                value         = message,
                onValueChange = { if (it.length <= maxChars) { message = it; msgError = null } },
                label         = { Text(stringResource(R.string.contact_field_message)) },
                isError       = msgError != null,
                supportingText = {
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(msgError ?: "")
                        Text(text = "${message.length} / $maxChars",
                             color = if (message.length >= maxChars)
                                 MaterialTheme.colorScheme.error
                             else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                minLines        = 6,
                maxLines        = 12,
                modifier        = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Button(
                onClick  = { focusManager.clearFocus(); sendMessage() },
                enabled  = !isSending,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (isSending)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                else
                    Text(text = stringResource(R.string.contact_send_button),
                         style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── ContactUsViewModel ────────────────────────────────────────────────────────

@dagger.hilt.android.lifecycle.HiltViewModel
class ContactUsViewModel @Inject constructor(
    val sendUseCase: SendContactMessageUseCase
) : androidx.lifecycle.ViewModel()

// ── Delete Account ────────────────────────────────────────────────────────────

@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,         // used in LaunchedEffect to pop back after URL opens
    onHomeClick: () -> Unit
) {
    val context = LocalContext.current
    val url     = stringResource(R.string.delete_account_url)

    LaunchedEffect(Unit) {
        openUrl(context, url)
        onBack()
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = stringResource(R.string.delete_account_title),
                onLogoClick = onHomeClick
            )
        }
    ) { padding ->
        Box(
            modifier         = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(text  = stringResource(R.string.delete_account_opening_browser),
                 style = MaterialTheme.typography.bodyLarge,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
