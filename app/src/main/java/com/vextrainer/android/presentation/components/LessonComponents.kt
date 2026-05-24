package com.vextrainer.android.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.LessonSummary
import com.vextrainer.android.domain.model.lesson.Module

// Module card

@Composable
fun ModuleCard(
    module: Module,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)) {

            // Name row — icon removed, arrow kept
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text     = module.moduleName,
                        style    = MaterialTheme.typography.titleLarge,
                        color    = MaterialTheme.colorScheme.onSurface
                    )
                    module.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text     = desc,
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.cd_navigate_forward),
                    tint               = MaterialTheme.colorScheme.primary
                )
            }

            // Progress: tighter spacing
            Spacer(Modifier.height(4.dp))
            Text(
                text  = stringResource(
                    R.string.module_progress,
                    module.completedLessons,
                    module.lessonCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            LinearProgressIndicator(
                progress  = {
                    if (module.lessonCount > 0)
                        module.completedLessons.toFloat() / module.lessonCount.toFloat()
                    else 0f
                },
                modifier   = Modifier.fillMaxWidth().height(6.dp),
                color      = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap  = StrokeCap.Round
            )
        }
    }
}

// Lesson card

@Composable
fun LessonCard(
    lesson: LessonSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = lesson.lessonTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    // "N Topics · N of N read" on one line
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text  = stringResource(R.string.lesson_topics_count, lesson.topicCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!lesson.isCompleted && lesson.topicCount > 0) {
                            Text(
                                text  = "  ·  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = stringResource(
                                    R.string.lesson_progress,
                                    lesson.completedTopics,
                                    lesson.topicCount
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (lesson.isCompleted) {
                    Icon(
                        imageVector        = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.lesson_completed_label),
                        tint               = Color(0xFF2E7D32),
                        modifier           = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.cd_navigate_forward),
                        tint               = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Progress bar: tighter spacing
            if (!lesson.isCompleted && lesson.topicCount > 0) {
                Spacer(Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress   = {
                        lesson.completedTopics.toFloat() / lesson.topicCount.toFloat()
                    },
                    modifier   = Modifier.fillMaxWidth().height(4.dp),
                    color      = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap  = StrokeCap.Round
                )
            }
        }
    }
}
