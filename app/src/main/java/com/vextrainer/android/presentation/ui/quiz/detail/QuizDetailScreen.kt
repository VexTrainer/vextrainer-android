package com.vextrainer.android.presentation.ui.quiz.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizDetail
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.MarkdownText
import com.vextrainer.android.presentation.components.VexTopAppBar
import com.vextrainer.android.presentation.components.inlineMarkdown

@Composable
fun QuizDetailScreen(
    onStartQuiz: (attemptId: Int) -> Unit,
    onHomeClick: () -> Unit,
    viewModel: QuizDetailViewModel = hiltViewModel()
) {
    val uiState           = viewModel.uiState.collectAsStateWithLifecycle()
    val state             by uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QuizDetailEvent.NavigateToSession -> onStartQuiz(event.attemptId)
            }
        }
    }

    val errorMessage = state.error?.asString()
    LaunchedEffect(state.error) {
        if (state.quiz != null && state.error != null && errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = state.quiz?.quizTitle ?: stringResource(R.string.nav_quizzes),
                onLogoClick = onHomeClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> LoadingOverlay()

                state.error != null && state.quiz == null -> ErrorCard(
                    message = state.error!!.asString(),
                    onRetry = viewModel::loadQuizDetail
                )

                state.quiz != null -> QuizDetailContent(
                    quiz        = state.quiz!!,
                    isStarting  = state.isStarting,
                    onStartQuiz = viewModel::startQuiz
                )
            }
        }
    }
}

@Composable
private fun QuizDetailContent(
    quiz: QuizDetail,
    isStarting: Boolean,
    onStartQuiz: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Title — rendered with inline markdown
                Text(
                    text  = inlineMarkdown(quiz.quizTitle),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text  = stringResource(R.string.quiz_detail_category, quiz.categoryName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Description — full markdown (may contain code blocks)
                quiz.quizDescription?.let { desc ->
                    Spacer(Modifier.height(12.dp))
                    MarkdownText(
                        text     = desc,
                        color    = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = stringResource(R.string.quiz_questions_count, quiz.totalQuestions)
                    )
                    quiz.passingScore?.let { score ->
                        StatItem(
                            label = stringResource(
                                R.string.quiz_passing_score, "%.0f".format(score)
                            )
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text  = stringResource(R.string.quiz_detail_your_stats),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                if (quiz.userAttempts == 0) {
                    Text(
                        text  = stringResource(R.string.quiz_detail_not_attempted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text  = stringResource(R.string.quiz_attempts_count, quiz.userAttempts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    quiz.userBestScore?.let { best ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = stringResource(
                                R.string.quiz_best_score, "%.0f".format(best)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick  = onStartQuiz,
            enabled  = !isStarting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            if (isStarting)
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            else
                Text(
                    text  = stringResource(R.string.quiz_start_button),
                    style = MaterialTheme.typography.titleLarge
                )
        }
    }
}

// sublabel removed — all callers passed null, so the parameter was dead code
@Composable
private fun StatItem(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
