package com.vextrainer.android.presentation.components

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.vextrainer.android.R
import io.noties.markwon.Markwon

// Loading / Error
@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text      = message,
                style     = MaterialTheme.typography.bodyLarge,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

// Inline markdown → AnnotatedString

/**
 * Converts a string with simple inline Markdown to an [AnnotatedString] for
 * use with the standard Compose [Text] composable.
 *
 * Supported syntax:
 *  - `` `code` ``  → monospace + light grey background
 *  - `**bold**`    → bold
 *  - `*italic*`    → italic
 *
 * Use this inside clickable containers (buttons, cards) where [MarkdownText]
 * cannot be used because its underlying [android.widget.TextView] intercepts
 * touch events. For answer options, explanations inside buttons, etc.
 *
 * For full Markdown (code blocks, lists, images) outside buttons, use
 * [MarkdownText] instead.
 */
fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val codeStyle   = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = Color(0x22808080)   // subtle grey tint for inline code
    )
    val boldStyle   = SpanStyle(fontWeight = FontWeight.Bold)
    val italicStyle = SpanStyle(fontStyle  = FontStyle.Italic)

    var i = 0
    while (i < text.length) {
        when {
            // Inline code: `code`
            text[i] == '`' -> {
                val end = text.indexOf('`', i + 1)
                if (end > i) {
                    withStyle(codeStyle) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    append(text[i++])
                }
            }
            // Bold: **text**
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end > i) {
                    withStyle(boldStyle) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else {
                    append(text[i++])
                }
            }
            // Italic: *text* (not part of **)
            text[i] == '*' -> {
                val end = text.indexOf('*', i + 1)
                if (end > i) {
                    withStyle(italicStyle) { append(text.substring(i + 1, end)) }
                    i = end + 1
                } else {
                    append(text[i++])
                }
            }
            else -> append(text[i++])
        }
    }
}

// Full Markdown text (outside buttons only)

/**
 * Renders [text] as full Markdown using Markwon inside a native [TextView].
 *
 * Use for question text, explanations, and other longer content that lives
 * OUTSIDE clickable containers. Markwon adds ClickableSpan objects internally
 * which intercept touch events, making it incompatible with buttons or cards
 * that have their own click handlers.
 *
 * For short inline text inside buttons, use [inlineMarkdown] with [Text] instead.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: Float = 16f
) {
    val context       = LocalContext.current
    val resolvedColor = if (color != Color.Unspecified) color
                        else MaterialTheme.colorScheme.onSurface
    val colorInt      = resolvedColor.toArgb()
    val markwon       = remember(context) { Markwon.builder(context).build() }

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                textSize               = fontSize
                setLineSpacing(0f, 1.4f)
                isClickable            = false
                isFocusable            = false
                isFocusableInTouchMode = false
            }
        },
        update  = { tv ->
            tv.setTextColor(colorInt)
            markwon.setMarkdown(tv, text)
        },
        // pointerInteropFilter returning false means "I don't want this touch event"
        // so Compose propagates it up to the parent (e.g. OutlinedButton), making
        // the full answer option area tappable — not just the label badge.
        modifier = modifier.pointerInteropFilter { false }
    )
}

// Top app bar

/**
 * Standard top app bar for VexTrainer.
 *
 * Always shows the app logo + "VexTrainer" brand in the title slot.
 * Tapping the logo/title calls [onLogoClick] — wire this to navigate home.
 *
 * The [onBack] parameter is kept for source compatibility with screens not yet
 * updated, but the back arrow is intentionally not rendered — Android's gesture
 * navigation and system back button handle back navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VexTopAppBar(
    title: String,
    onLogoClick: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = if (onLogoClick != null)
                    Modifier.clickable(onClick = onLogoClick)
                else
                    Modifier
            ) {
                Image(
                    painter            = painterResource(R.drawable.logo_vextrainer),
                    contentDescription = stringResource(R.string.app_name),
                    modifier           = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = stringResource(R.string.app_name),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        navigationIcon = {},
        actions        = actions,
        colors         = TopAppBarDefaults.topAppBarColors(
            containerColor             = MaterialTheme.colorScheme.primary,
            titleContentColor          = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
        )
    )
}
