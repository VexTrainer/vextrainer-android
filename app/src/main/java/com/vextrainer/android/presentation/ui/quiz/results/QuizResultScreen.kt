package com.vextrainer.android.presentation.ui.quiz.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.vextrainer.android.domain.model.quiz.QuizResults
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.PassFailChip
import com.vextrainer.android.presentation.components.QuestionResultRow
import com.vextrainer.android.presentation.components.ScoreBadge
import com.vextrainer.android.presentation.components.VexTopAppBar

@Composable
fun QuizResultScreen(
    onRetakeQuiz: () -> Unit,
    onDone: () -> Unit,
    viewModel: QuizResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VexTopAppBar(title = stringResource(R.string.quiz_result_title))
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
                    onRetry = viewModel::loadResults
                )

                uiState.results != null -> ResultContent(
                    results      = uiState.results!!,
                    onRetakeQuiz = onRetakeQuiz,
                    onDone       = onDone
                )
            }
        }
    }
}

@Composable
private fun ResultContent(
    results: QuizResults,
    onRetakeQuiz: () -> Unit,
    onDone: () -> Unit
) {
    val summary = results.summary

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Score summary header ──────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = summary.quizTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(20.dp))

                ScoreBadge(
                    score        = summary.score ?: 0.0,
                    passingScore = summary.passingScore
                )

                Spacer(Modifier.height(12.dp))

                PassFailChip(passed = summary.passed)

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        R.string.quiz_correct_answers,
                        summary.correctAnswers,
                        summary.totalQuestions
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                // ── Action buttons ────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDone,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.quiz_done_button))
                    }
                    Button(
                        onClick = onRetakeQuiz,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.quiz_retake_button))
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.quiz_review_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }

        // ── Per-question breakdown ────────────────────────────────────────
        itemsIndexed(
            items = results.questions,
            key   = { _, q -> q.questionId }
        ) { index, question ->
            QuestionResultRow(result = question, index = index)
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
