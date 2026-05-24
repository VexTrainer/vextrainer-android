package com.vextrainer.android.presentation.ui.lessons.viewer

import android.graphics.Typeface
import android.text.style.BackgroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.TypefaceSpan
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.TopicDetails
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar
import io.noties.markwon.Markwon
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.Prism4jThemeDefault
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import io.noties.markwon.image.picasso.PicassoImagesPlugin

@Composable
fun TopicViewerScreen(
    onPrevious: (topicId: Int) -> Unit,
    onNext: (topicId: Int) -> Unit,
    onBack: () -> Unit,         // retained for NavGraph compat; VexTopAppBar ignores it
    onHomeClick: () -> Unit,
    viewModel: TopicViewerViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val state   by uiState
    val context  = LocalContext.current
    val isDark   = isSystemInDarkTheme()

    val markwon = remember(context, isDark) {
        val prism4j = Prism4j(object : io.noties.prism4j.GrammarLocator {
            override fun grammar(prism4j: Prism4j, language: String) = null
            override fun languages(): MutableSet<String> = mutableSetOf()
        })
        val codeTheme = if (isDark) Prism4jThemeDarkula.create() else Prism4jThemeDefault.create()
        Markwon.builder(context)
            .usePlugin(PicassoImagesPlugin.create(context))
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, codeTheme))
            .build()
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = state.topic?.topicTitle ?: "",
                onLogoClick = onHomeClick,
                actions     = {
                    if (state.isRead) {
                        Icon(
                            imageVector        = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.topic_read_label),
                            tint               = Color(0xFF2E7D32),
                            modifier           = Modifier.padding(end = 12.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoadingMeta -> LoadingOverlay()

                state.metaError != null -> ErrorCard(
                    message = state.metaError!!.asString(),
                    onRetry = viewModel::retryMeta
                )

                state.topic != null -> TopicContent(
                    topic            = state.topic!!,
                    markdownContent  = state.markdownContent,
                    isLoadingContent = state.isLoadingContent,
                    contentError     = state.contentError?.asString(),
                    isRead           = state.isRead,
                    isMarkingRead    = state.isMarkingRead,
                    markwon          = markwon,
                    isDark           = isDark,
                    onMarkRead       = viewModel::markAsRead,
                    onPrevious       = { state.topic?.previousTopicId?.let(onPrevious) },
                    onNext           = { state.topic?.nextTopicId?.let(onNext) },
                    onRetryContent   = { viewModel.retryContent() }
                )
            }
        }
    }
}

@Composable
private fun TopicContent(
    topic: TopicDetails,
    markdownContent: String,
    isLoadingContent: Boolean,
    contentError: String?,
    isRead: Boolean,
    isMarkingRead: Boolean,
    markwon: Markwon,
    isDark: Boolean,
    onMarkRead: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRetryContent: () -> Unit
) {
    val textColorInt  = MaterialTheme.colorScheme.onBackground.toArgb()
    val bgColorInt    = MaterialTheme.colorScheme.background.toArgb()
    val codeBlockBgInt   = if (isDark) 0xFF1E1E1E.toInt() else 0xFFF5F5F5.toInt()
    val codeBlockTextInt = if (isDark) 0xFFD4D4D4.toInt() else 0xFF212121.toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text  = "${topic.moduleName}  ›  ${topic.lessonTitle}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        when {
            isLoadingContent -> {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            contentError != null -> {
                Text(text = contentError,
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRetryContent) {
                    Text(stringResource(R.string.action_retry))
                }
            }
            markdownContent.isNotBlank() -> {
                val segments = splitMarkdownSegments(markdownContent)
                AndroidView(
                    factory = { ctx ->
                        LinearLayout(ctx).apply {
                            orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                    },
                    update = { container ->
                        container.removeAllViews()
                        segments.forEach { segment ->
                            if (segment.isCode) {
                                val scrollView = HorizontalScrollView(container.context).apply {
                                    isHorizontalScrollBarEnabled = true
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).also { it.setMargins(0, 16, 0, 16) }
                                    setBackgroundColor(codeBlockBgInt)
                                    setPadding(24, 20, 24, 20)
                                }
                                val codeView = TextView(container.context).apply {
                                    textSize = 13f
                                    typeface = Typeface.MONOSPACE
                                    setTextColor(codeBlockTextInt)
                                    setBackgroundColor(codeBlockBgInt)
                                    setSingleLine(false)
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                }
                                markwon.setMarkdown(codeView, segment.text)
                                scrollView.addView(codeView)
                                container.addView(scrollView)
                            } else {
                                val textView = TextView(container.context).apply {
                                    textSize = 16f
                                    setTextColor(textColorInt)
                                    setBackgroundColor(bgColorInt)
                                    setLineSpacing(0f, 1.4f)
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).also { it.setMargins(0, 4, 0, 4) }
                                }
                                markwon.setMarkdown(textView, segment.text)
                                container.addView(textView)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Previous · Mark as Read · Next — all in one row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Previous
            if (topic.previousTopicId != null) {
                OutlinedButton(
                    onClick  = onPrevious,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.topic_prev_button),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            // Mark as Read / Already read indicator
            if (!isRead) {
                Button(
                    onClick  = onMarkRead,
                    enabled  = !isMarkingRead,
                    modifier = Modifier.weight(2f).height(48.dp)
                ) {
                    if (isMarkingRead)
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    else
                        Text(
                            text     = stringResource(R.string.topic_mark_read_button),
                            style    = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                }
            } else {
                Row(
                    modifier            = Modifier.weight(2f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment   = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector        = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint               = Color(0xFF2E7D32),
                        modifier           = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = stringResource(R.string.topic_marked_read),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Next
            if (topic.nextTopicId != null) {
                Button(
                    onClick  = onNext,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.topic_next_button),
                        modifier           = Modifier.size(18.dp)
                    )
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private data class MarkdownSegment(val text: String, val isCode: Boolean)

private fun splitMarkdownSegments(markdown: String): List<MarkdownSegment> {
    val segments  = mutableListOf<MarkdownSegment>()
    val lines     = markdown.lines()
    val buffer    = StringBuilder()
    var inCode    = false
    var codeFence = ""

    for (line in lines) {
        val trimmed = line.trimStart()
        if (!inCode && (trimmed.startsWith("```") || trimmed.startsWith("~~~"))) {
            val normal = buffer.toString().trim()
            if (normal.isNotEmpty()) segments.add(MarkdownSegment(normal, false))
            buffer.clear()
            inCode    = true
            codeFence = if (trimmed.startsWith("```")) "```" else "~~~"
            buffer.appendLine(line)
        } else if (inCode && trimmed.startsWith(codeFence)) {
            buffer.appendLine(line)
            segments.add(MarkdownSegment(buffer.toString().trim(), true))
            buffer.clear()
            inCode = false
        } else {
            buffer.appendLine(line)
        }
    }
    val remaining = buffer.toString().trim()
    if (remaining.isNotEmpty()) segments.add(MarkdownSegment(remaining, inCode))
    return segments
}
