package com.vextrainer.android.presentation.ui.lessons.activity

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.ActivityQuiz
import com.vextrainer.android.domain.model.lesson.ActivityTopic
import com.vextrainer.android.domain.model.lesson.DayActivity
import com.vextrainer.android.domain.model.lesson.LessonActivity
import com.vextrainer.android.domain.model.lesson.ModuleActivity
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar

@Composable
fun ActivityReportScreen(
    onTopicClick: (topicId: Int) -> Unit,
    onQuizClick: (quizId: Int) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: ActivityReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = stringResource(R.string.activity_report_title),
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
                uiState.isLoading  -> LoadingOverlay()
                uiState.error != null -> ErrorCard(
                    message = uiState.error!!.asString(),
                    onRetry = viewModel::loadReport
                )
                uiState.days.isEmpty() -> EmptyState()
                else -> ActivityList(
                    days         = uiState.days,
                    onTopicClick = onTopicClick,
                    onQuizClick  = onQuizClick
                )
            }
        }
    }
}

// List

@Composable
private fun ActivityList(
    days: List<DayActivity>,
    onTopicClick: (Int) -> Unit,
    onQuizClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { day ->
            item(key = "date_${day.dateKey}") {
                DateHeader(label = day.dateLabel)
            }

            // Lessons section
            if (day.modules.isNotEmpty()) {
                item(key = "lessons_hdr_${day.dateKey}") {
                    SectionLabel(
                        icon  = Icons.AutoMirrored.Filled.MenuBook,
                        label = stringResource(R.string.activity_lessons_section)
                    )
                }
                day.modules.forEach { module ->
                    item(key = "module_${day.dateKey}_${module.moduleId}") {
                        ModuleBlock(module = module, onTopicClick = onTopicClick)
                    }
                }
            }

            // Quizzes section
            if (day.quizzes.isNotEmpty()) {
                item(key = "quizzes_hdr_${day.dateKey}") {
                    SectionLabel(
                        icon  = Icons.AutoMirrored.Filled.Grading,
                        label = stringResource(R.string.activity_quizzes_section)
                    )
                }
                items(
                    items = day.quizzes,
                    key   = { "quiz_${day.dateKey}_${it.quizId}" }
                ) { quiz ->
                    QuizRow(quiz = quiz, onQuizClick = onQuizClick)
                }
            }

            item(key = "div_${day.dateKey}") {
                Spacer(Modifier.height(2.dp))
                HorizontalDivider()
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// Date header

@Composable
private fun DateHeader(label: String) {
    Text(
        text       = label,
        style      = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.primary
    )
}

// Section label (Lessons / Quizzes)

@Composable
private fun SectionLabel(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(top = 2.dp, bottom = 1.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Module → Lesson → Topic hierarchy

@Composable
private fun ModuleBlock(module: ModuleActivity, onTopicClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            // Module name
            Text(
                text       = module.moduleName,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            module.lessons.forEach { lesson ->
                LessonBlock(lesson = lesson, onTopicClick = onTopicClick)
            }
        }
    }
}

@Composable
private fun LessonBlock(lesson: LessonActivity, onTopicClick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)) {
        Text(
            text       = lesson.lessonTitle,
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        lesson.topics.forEach { topic ->
            TopicRow(topic = topic, onTopicClick = onTopicClick)
        }
    }
}

@Composable
private fun TopicRow(topic: ActivityTopic, onTopicClick: (Int) -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clickable { onTopicClick(topic.topicId) }
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            Text(
                text  = "•",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = topic.topicTitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Quiz row

@Composable
private fun QuizRow(quiz: ActivityQuiz, onQuizClick: (Int) -> Unit) {
    val displayTitle = if (quiz.attemptCount > 1)
        "${quiz.quizTitle} (${quiz.attemptCount}x)"
    else
        quiz.quizTitle

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { onQuizClick(quiz.quizId) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = if (quiz.isCompleted) Icons.Default.CheckCircle
                                     else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint               = if (quiz.isCompleted) Color(0xFF2E7D32)
                                     else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text     = displayTitle,
                style    = MaterialTheme.typography.bodyMedium,
                color    = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            when {
                quiz.isCompleted && quiz.bestScore != null -> Text(
                    text       = "%.0f%%".format(quiz.bestScore),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = if (quiz.bestScore >= 70.0) Color(0xFF2E7D32)
                                 else Color(0xFFB71C1C)
                )
                else -> Text(
                    text  = stringResource(R.string.quiz_status_incomplete),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Empty state

@Composable
private fun EmptyState() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = stringResource(R.string.activity_report_empty),
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
