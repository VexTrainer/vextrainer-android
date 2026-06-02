package com.vextrainer.android.presentation.ui.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.compose.LocalLifecycleOwner
//import androidx.lifecycle.repeatOnLifecycle
//import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.dashboard.BookmarkItem
import com.vextrainer.android.domain.model.dashboard.ContinueLearningItem
import com.vextrainer.android.domain.model.dashboard.DashboardStats
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onTopicClick: (topicId: Int) -> Unit,
    onNavigateToQuizzes: () -> Unit,
    onNavigateToLessons: () -> Unit,
    onStreakClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-refresh every time this screen becomes visible.
    // repeatOnLifecycle(RESUMED) fires on first show AND when returning
    // from a topic/lesson — so streak, progress etc. stay up-to-date.
//    val lifecycleOwner = LocalLifecycleOwner.current
//    androidx.compose.runtime.LaunchedEffect(lifecycleOwner) {
//        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
//            viewModel.loadDashboard()
//        }
//    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title     = stringResource(R.string.dashboard_title),
                // Dashboard IS home — logo tap is a no-op here
                onLogoClick = {}
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = { viewModel.loadDashboard(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> LoadingOverlay()

                uiState.error != null -> ErrorCard(
                    message = uiState.error!!.asString(),
                    onRetry = { viewModel.loadDashboard() }
                )

                uiState.dashboard != null -> DashboardContent(
                    userName         = uiState.userName,
                    stats            = uiState.dashboard!!.stats,
                    continueLearning = uiState.dashboard!!.continueLearning,
                    bookmarks        = uiState.dashboard!!.bookmarks,
                    onTopicClick     = onTopicClick,
                    onDeleteBookmark = viewModel::deleteBookmark,
                    onStreakClick    = onStreakClick
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    userName: String,
    stats: DashboardStats,
    continueLearning: List<ContinueLearningItem>,
    bookmarks: List<BookmarkItem>,
    onTopicClick: (topicId: Int) -> Unit,
    onDeleteBookmark: (topicId: Int) -> Unit,
    onStreakClick: () -> Unit = {}
) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome
        item {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text  = if (userName.isNotBlank()) stringResource(R.string.dashboard_welcome_back)
                        else stringResource(R.string.dashboard_welcome),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (userName.isNotBlank()) {
                        Text(
                            text       = userName,
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                StreakBadge(
                    streak  = stats.readingStreak,
                    onClick = onStreakClick
                )
            }
        }

        // Progress card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = stringResource(R.string.dashboard_progress_title),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    ProgressRow(
                        label    = stringResource(R.string.dashboard_modules),
                        done     = stats.completedModules,
                        total    = stats.totalModules,
                        percent  = (stats.modulesProgressPercent / 100f).toFloat(),
                        color    = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    ProgressRow(
                        label    = stringResource(R.string.dashboard_lessons),
                        done     = stats.completedLessons,
                        total    = stats.totalLessons,
                        percent  = (stats.lessonsProgressPercent / 100f).toFloat(),
                        color    = Color(0xFF7B61FF)
                    )
                    Spacer(Modifier.height(8.dp))
                    ProgressRow(
                        label    = stringResource(R.string.dashboard_topics),
                        done     = stats.topicsRead,
                        total    = stats.totalTopics,
                        percent  = (stats.topicsProgressPercent / 100f).toFloat(),
                        color    = Color(0xFF00897B)
                    )
                }
            }
        }

        // Add this item BEFORE the Row with three StatCards:
        item {
            Text(
                text       = stringResource(R.string.dashboard_quizzes_section),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }

        // Quiz stats
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label    = stringResource(R.string.dashboard_quizzes_attempted),
                    value    = stats.quizzesAttempted.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label    = stringResource(R.string.dashboard_quizzes_completed),
                    value    = stats.quizzesCompleted.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label    = stringResource(R.string.dashboard_avg_score),
                    value    = if (stats.averageQuizScore > 0)
                                   "%.0f%%".format(stats.averageQuizScore)
                               else stringResource(R.string.dashboard_no_score),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Bookmarks
        if (bookmarks.isNotEmpty()) {
            item {
                Text(
                    text       = stringResource(R.string.dashboard_bookmarks),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            items(bookmarks, key = { it.topicId }) { bookmark ->
                BookmarkRow(
                    bookmark         = bookmark,
                    onTopicClick     = { onTopicClick(bookmark.topicId) },
                    onDeleteBookmark = { onDeleteBookmark(bookmark.topicId) }
                )
            }
        }

        // Continue Learning
        if (continueLearning.isNotEmpty()) {
            item {
                Text(
                    text       = stringResource(R.string.dashboard_continue_learning),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
            }
            items(continueLearning, key = { it.lessonId }) { item ->
                ContinueLearningCard(
                    item    = item,
                    onClick = { onTopicClick(item.nextTopicId) }
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

// Progress row

@Composable
private fun ProgressRow(
    label: String,
    done: Int,
    total: Int,
    percent: Float,
    color: Color
) {
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text  = stringResource(R.string.dashboard_progress_fraction, done, total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress  = { percent.coerceIn(0f, 1f) },
            modifier  = Modifier.fillMaxWidth().height(6.dp),
            color     = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

// Stat card

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Continue Learning card

@Composable
private fun ContinueLearningCard(
    item: ContinueLearningItem,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = item.moduleName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text       = item.lessonTitle,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(
                        R.string.dashboard_next_topic, item.nextTopicTitle
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress  = {
                        if (item.totalTopics > 0)
                            item.topicsRead.toFloat() / item.totalTopics.toFloat()
                        else 0f
                    },
                    modifier  = Modifier.fillMaxWidth().height(4.dp),
                    color     = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text  = stringResource(R.string.dashboard_topics_read_progress, item.topicsRead, item.totalTopics),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

// Bookmark row

@Composable
private fun BookmarkRow(
    bookmark: BookmarkItem,
    onTopicClick: () -> Unit,
    onDeleteBookmark: () -> Unit
) {
    ElevatedCard(
        onClick   = onTopicClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text     = "${bookmark.moduleName}: ${bookmark.lessonTitle}: ${bookmark.topicTitle}",
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteBookmark) {
                Icon(
                    imageVector        = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.bookmark_delete),
                    tint               = MaterialTheme.colorScheme.error,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Streak badge — always visible; muted style when streak = 0
// onClick wires to ActivityReportScreen via NavGraph

@Composable
private fun StreakBadge(streak: Int, onClick: () -> Unit = {}) {
    val active  = streak > 0
    val bgColor = if (active) Color(0xFFFFF3E0)
                  else MaterialTheme.colorScheme.surfaceVariant
    val fgColor = if (active) Color(0xFFE65100)
                  else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape   = MaterialTheme.shapes.medium,
        color   = bgColor,
        onClick = onClick
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint               = fgColor,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text       = streak.toString(),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = fgColor
            )
        }
    }
}
