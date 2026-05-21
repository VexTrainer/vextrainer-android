package com.vextrainer.android.presentation.ui.quiz.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.vextrainer.android.domain.model.quiz.QuizAnswer
import com.vextrainer.android.domain.model.quiz.QuizQuestion
import com.vextrainer.android.presentation.components.AnswerOptionButton
import com.vextrainer.android.presentation.components.ErrorCard
import com.vextrainer.android.presentation.components.LoadingOverlay
import com.vextrainer.android.presentation.components.OptionState
import com.vextrainer.android.presentation.components.QuizProgressBar
import com.vextrainer.android.presentation.components.VexTopAppBar

private val OPTION_LABELS = listOf("A", "B", "C", "D", "E", "F")

@Composable
fun QuizSessionScreen(
    onNavigateToResults: (attemptId: Int) -> Unit,
    onBack: () -> Unit,         // used in exit-dialog confirm — do not remove
    onHomeClick: () -> Unit,
    viewModel: QuizSessionViewModel = hiltViewModel()
) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog   by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QuizSessionEvent.NavigateToResults ->
                    onNavigateToResults(event.attemptId)
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title            = { Text(stringResource(R.string.quiz_exit_dialog_title)) },
            text             = { Text(stringResource(R.string.quiz_exit_dialog_message)) },
            confirmButton    = {
                TextButton(
                    onClick = { showExitDialog = false; onBack() },
                    colors  = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.quiz_exit_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.quiz_exit_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            VexTopAppBar(
                title       = stringResource(
                    R.string.quiz_question_progress,
                    uiState.progressDisplay,
                    uiState.totalQuestions
                ),
                onLogoClick = onHomeClick   // logo tap goes home; back arrow not shown
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.phase) {
                SessionPhase.LOADING -> LoadingOverlay()

                SessionPhase.COMPLETING -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text  = stringResource(R.string.quiz_completing),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                SessionPhase.ERROR -> ErrorCard(
                    message = uiState.error?.asString() ?: stringResource(R.string.error_unknown),
                    onRetry = viewModel::retry
                )

                else -> {
                    uiState.currentQuestion?.let { question ->
                        QuizQuestionContent(
                            question               = question,
                            uiState                = uiState,
                            onSelectAnswer         = viewModel::selectAnswer,
                            onSelectMatchingAnswer = viewModel::selectMatchingAnswer,
                            onSubmit               = viewModel::submitAnswer,
                            onNext                 = viewModel::nextQuestion
                        )
                    }
                }
            }
        }
    }
}

// ── Question content dispatcher ───────────────────────────────────────────────

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
    uiState: QuizSessionUiState,
    onSelectAnswer: (Int) -> Unit,
    onSelectMatchingAnswer: (answerId: Int, matchSide: String) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val isRevealed = uiState.phase == SessionPhase.ANSWER_REVEALED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuizProgressBar(current = uiState.progressDisplay, total = uiState.totalQuestions)

        val hint = when {
            uiState.isMatchingQuestion ->
                stringResource(R.string.quiz_match_instruction)
            question.questionType == com.vextrainer.android.domain.model.quiz.QuestionType.MULTIPLE_ANSWER ->
                stringResource(R.string.quiz_select_all_answers)
            else -> null
        }
        hint?.let {
            Text(text = it, style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.primary)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text      = question.questionText,
                style     = MaterialTheme.typography.bodyLarge,
                modifier  = Modifier.padding(16.dp),
                textAlign = TextAlign.Start,
                color     = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (uiState.isMatchingQuestion) {
            MatchingQuestionContent(
                answers        = question.answers,
                matchingPairs  = uiState.matchingPairs,
                selectedLeftId = uiState.selectedLeftId,
                isRevealed     = isRevealed,
                correctRIds = parseCorrectMatchIds(uiState.answerResult?.correctAnswerJson),
                onPairSelected = onSelectMatchingAnswer
            )
        } else {
            question.answers.forEachIndexed { index, answer ->
                val optionState = resolveOptionState(
                    answer       = answer,
                    selectedIds  = uiState.selectedAnswerIds,
                    answerResult = uiState.answerResult,
                    isRevealed   = isRevealed
                )
                AnswerOptionButton(
                    label       = OPTION_LABELS.getOrElse(index) { "${index + 1}" },
                    text        = answer.answerText,
                    state       = optionState,
                    enabled     = !isRevealed,
                    onClick     = { onSelectAnswer(answer.answerId) }
                )
            }
        }

        uiState.answerResult?.let { result ->
            if (isRevealed) FeedbackCard(answerResult = result)
        }

        if (!isRevealed) {
            Button(
                onClick  = onSubmit,
                enabled  = uiState.selectedAnswerIds.isNotEmpty() ||
                           (uiState.isMatchingQuestion && uiState.matchingPairs.size ==
                            question.answers.count { it.matchSide == "L" }),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text = stringResource(R.string.quiz_submit_button),
                     style = MaterialTheme.typography.titleLarge)
            }
        } else {
            Button(
                onClick  = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text = stringResource(R.string.quiz_next_button),
                     style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

// ── Matching question ─────────────────────────────────────────────────────────

private val PAIR_COLORS = listOf(
    Pair(Color(0xFFE3F2FD), Color(0xFF1565C0)),
    Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A)),
    Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)),
    Pair(Color(0xFFFFF8E1), Color(0xFFF57F17)),
    Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
)

@Composable
private fun MatchingQuestionContent(
    answers: List<QuizAnswer>,
    matchingPairs: List<Pair<Int, Int>>,
    selectedLeftId: Int?,
    isRevealed: Boolean,
    correctRIds: List<Int>,
    onPairSelected: (answerId: Int, matchSide: String) -> Unit
) {
    val leftItems    = answers.filter { it.matchSide == "L" }
    val rightItems   = answers.filter { it.matchSide == "R" }
    val pairedLeftIds = matchingPairs.map { it.first }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Component", style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 modifier = Modifier.padding(bottom = 2.dp))
            leftItems.forEach { answer ->
                val pairIndex = pairedLeftIds.indexOf(answer.answerId)
                val isPaired  = pairIndex >= 0
                val isSelected = selectedLeftId == answer.answerId

                val (bgColor, borderColor) = when {
                    isRevealed && isPaired -> {
                        val rId = matchingPairs.find { it.first == answer.answerId }?.second
                        if (rId != null && rId in correctRIds)
                            Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        else Pair(Color(0xFFFFEBEE), Color(0xFFB71C1C))
                    }
                    isPaired   -> PAIR_COLORS[pairIndex % PAIR_COLORS.size]
                    isSelected -> Pair(MaterialTheme.colorScheme.primaryContainer,
                                       MaterialTheme.colorScheme.primary)
                    else       -> Pair(MaterialTheme.colorScheme.surface,
                                       MaterialTheme.colorScheme.outline)
                }

                MatchingItemButton(
                    text        = answer.answerText,
                    borderColor = borderColor,
                    bgColor     = bgColor,
                    textColor   = if (isPaired || isSelected) borderColor
                                  else MaterialTheme.colorScheme.onSurface,
                    enabled     = !isRevealed,
                    onClick     = { onPairSelected(answer.answerId, "L") }
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Purpose", style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 modifier = Modifier.padding(bottom = 2.dp))
            rightItems.forEach { answer ->
                val pairedLeftId = matchingPairs.find { it.second == answer.answerId }?.first
                val pairIndex    = if (pairedLeftId != null) pairedLeftIds.indexOf(pairedLeftId) else -1
                val isPaired     = pairIndex >= 0
                val isCorrectR   = isRevealed && answer.answerId in correctRIds

                val (bgColor, borderColor) = when {
                    isRevealed && isPaired -> {
                        if (isCorrectR) Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        else Pair(Color(0xFFFFEBEE), Color(0xFFB71C1C))
                    }
                    isPaired -> PAIR_COLORS[pairIndex % PAIR_COLORS.size]
                    else     -> Pair(MaterialTheme.colorScheme.surface,
                                     MaterialTheme.colorScheme.outline)
                }

                MatchingItemButton(
                    text        = answer.answerText,
                    borderColor = borderColor,
                    bgColor     = bgColor,
                    textColor   = if (isPaired) borderColor else MaterialTheme.colorScheme.onSurface,
                    enabled     = !isRevealed,
                    onClick     = { onPairSelected(answer.answerId, "R") }
                )
            }
        }
    }

    if (!isRevealed) {
        val pairedCount = matchingPairs.size
        val leftCount   = leftItems.size
        Text(text = "$pairedCount of $leftCount paired",
             style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (selectedLeftId != null) {
            val selName = leftItems.find { it.answerId == selectedLeftId }?.answerText ?: ""
            Text(text = "\"$selName\" selected — now tap a Purpose",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun MatchingItemButton(
    text: String,
    borderColor: Color,
    bgColor: Color,
    textColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(8.dp),
        border   = BorderStroke(2.dp, borderColor),
        colors   = ButtonDefaults.outlinedButtonColors(
            containerColor         = bgColor,
            disabledContainerColor = bgColor,
            contentColor           = textColor,
            disabledContentColor   = textColor
        ),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium,
             textAlign = TextAlign.Center, color = textColor, maxLines = 2)
    }
}

// ── Feedback card ─────────────────────────────────────────────────────────────

@Composable
private fun FeedbackCard(
    answerResult: com.vextrainer.android.domain.model.quiz.AnswerResult
) {
    val isCorrect = answerResult.isCorrect
    val bgColor   = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (isCorrect) Color(0xFF1B5E20)  else Color(0xFF7F0000)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = if (isCorrect) stringResource(R.string.quiz_correct)
                        else stringResource(R.string.quiz_incorrect),
                 style = MaterialTheme.typography.titleLarge,
                 color = textColor, fontWeight = FontWeight.Bold)
            answerResult.explanation?.let { exp ->
                Spacer(Modifier.height(6.dp))
                Text(text = exp, style = MaterialTheme.typography.bodyMedium, color = textColor)
            }
        }
    }
}

// ── Option state resolver ─────────────────────────────────────────────────────

private fun resolveOptionState(
    answer: QuizAnswer,
    selectedIds: List<Int>,
    answerResult: com.vextrainer.android.domain.model.quiz.AnswerResult?,
    isRevealed: Boolean
): OptionState {
    if (!isRevealed) {
        return if (answer.answerId in selectedIds) OptionState.SELECTED else OptionState.DEFAULT
    }
    val wasSelected = answer.answerId in selectedIds
    val correctId   = answerResult?.correctAnswerJson?.let { parseCorrectAnswerId(it) }
    return when {
        correctId != null && answer.answerId == correctId && wasSelected  -> OptionState.CORRECT
        correctId != null && answer.answerId == correctId && !wasSelected -> OptionState.MISSED
        wasSelected && (correctId == null || answer.answerId != correctId) -> OptionState.INCORRECT
        else -> OptionState.DEFAULT
    }
}

private fun parseCorrectAnswerId(json: String): Int? {
    return try {
        val cleaned = json.trim().removeSurrounding("{", "}")
        val parts   = cleaned.split(",")
        for (part in parts) {
            val kv  = part.split(":")
            if (kv.size == 2) {
                val key = kv[0].trim().removeSurrounding("\"")
                if (key == "answer_id" || key == "answerId") return kv[1].trim().toIntOrNull()
            }
        }
        null
    } catch (e: Exception) { null }
}

private fun parseCorrectMatchIds(json: String?): List<Int> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val objects = json.trim().split("},{", "}, {")
        objects.mapNotNull { obj ->
            val cleaned = obj.trim().removeSurrounding("{", "}")
            val parts   = cleaned.split(",")
            var id: Int? = null
            for (part in parts) {
                val kv  = part.split(":")
                if (kv.size == 2) {
                    val key = kv[0].trim().removeSurrounding("\"")
                    if (key == "answer_id" || key == "answerId") id = kv[1].trim().toIntOrNull()
                }
            }
            id
        }
    } catch (e: Exception) { emptyList() }
}
