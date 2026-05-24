package com.vextrainer.android.presentation.ui.lessons.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LessonCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.VexTopAppBar

@Composable
fun LessonListScreen(
    // moduleName added so NavGraph can thread it through to TopicList
    onLessonClick: (lessonId: Int, lessonTitle: String, moduleName: String) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: LessonListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = uiState.moduleName.ifBlank { stringResource(R.string.nav_lessons) },
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
                    onRetry = viewModel::loadLessons
                )

                uiState.lessons.isEmpty() -> Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = stringResource(R.string.lesson_list_empty),
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    contentPadding      = PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 4.dp,    // reduced top gap
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Module name heading
                    if (uiState.moduleName.isNotBlank()) {
                        item(key = "heading") {
                            Text(
                                text       = uiState.moduleName,
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.onSurface,
                                modifier   = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    items(uiState.lessons, key = { it.lessonId }) { lesson ->
                        LessonCard(
                            lesson  = lesson,
                            onClick = {
                                onLessonClick(
                                    lesson.lessonId,
                                    lesson.lessonTitle,
                                    uiState.moduleName
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
