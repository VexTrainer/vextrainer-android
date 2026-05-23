package com.vextrainer.android.presentation.ui.quiz.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizHistoryItem
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar

// "2026-05-21T23:42:34.270000" → "2026-05-21 23:42"
private fun formatDateTime(iso: String): String {
    return try {
        val utcFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        utcFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = utcFormat.parse(iso.take(19)) ?: return iso.take(16).replace("T", " ")
        val localFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        localFormat.timeZone = java.util.TimeZone.getDefault()
        localFormat.format(date)
    } catch (e: Exception) {
        iso.take(16).replace("T", " ")
    }
}

@Composable
fun QuizHistoryScreen(
    onAttemptClick: (attemptId: Int) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: QuizHistoryViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 && uiState.hasMore && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = stringResource(R.string.quiz_history_title),
                onLogoClick = onHomeClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> LoadingOverlay()

                uiState.error != null -> ErrorCard(
                    message = uiState.error!!.asString(),
                    onRetry = viewModel::loadHistory
                )

                uiState.attempts.isEmpty() -> Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = stringResource(R.string.quiz_history_empty),
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier  = Modifier.padding(24.dp)
                    )
                }

                else -> LazyColumn(
                    state               = listState,
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = uiState.attempts,
                        key   = { index, item -> "${item.attemptId}_$index" }
                    ) { _, attempt ->
                        HistoryItemCard(
                            attempt = attempt,
                            onClick = { onAttemptClick(attempt.attemptId) }
                        )
                    }
                    if (uiState.isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(attempt: QuizHistoryItem, onClick: () -> Unit) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text  = attempt.quizTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = attempt.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth()
            ) {
                // ── Status + score ────────────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (attempt.isCompleted) {
                        StatusLabel(label = "Complete", positive = true)
                        attempt.score?.let { score ->
                            Text(
                                text  = "%.0f%%".format(score),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        StatusLabel(label = "Incomplete", positive = false)
                    }
                }

                // ── Date/time ─────────────────────────────────────────────
                // Always show startedDate — completedDate is null for incomplete.
                Text(
                    text  = formatDateTime(attempt.startedDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusLabel(label: String, positive: Boolean) {
    val color   = if (positive) Color(0xFF2E7D32) else Color(0xFF9E6C00)
    val bgColor = if (positive) Color(0xFFE8F5E9)  else Color(0xFFFFF8E1)

    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = bgColor,
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelLarge,
            color      = color,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
