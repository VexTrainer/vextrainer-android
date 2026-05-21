package com.vextrainer.android.presentation.ui.lessons.topics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.TopicSummary
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar

@Composable
fun TopicListScreen(
    onTopicClick: (topicId: Int) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: TopicListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = uiState.lessonTitle.ifBlank { stringResource(R.string.topic_list_title) },
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
                    onRetry = viewModel::loadTopics
                )

                uiState.topics.isEmpty() -> Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(R.string.topic_list_empty),
                         style = MaterialTheme.typography.bodyLarge,
                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                         textAlign = TextAlign.Center)
                }

                else -> LazyColumn(
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.topics, key = { it.topicId }) { topic ->
                        TopicRow(topic = topic, onClick = { onTopicClick(topic.topicId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicRow(topic: TopicSummary, onClick: () -> Unit) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = Modifier
            .fillMaxWidth()
            .padding(start = if (topic.isSubTopic) 16.dp else 0.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = if (topic.isRead)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = topic.topicTitle,
                     style = if (topic.isSubTopic) MaterialTheme.typography.bodyLarge
                             else MaterialTheme.typography.titleLarge,
                     color = MaterialTheme.colorScheme.onSurface,
                     maxLines = 2, overflow = TextOverflow.Ellipsis)
                topic.parentTopicTitle?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(8.dp))
            if (topic.isRead) {
                Icon(imageVector = Icons.Default.CheckCircle,
                     contentDescription = stringResource(R.string.topic_read_label),
                     tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            } else {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                     contentDescription = stringResource(R.string.cd_navigate_forward),
                     tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
