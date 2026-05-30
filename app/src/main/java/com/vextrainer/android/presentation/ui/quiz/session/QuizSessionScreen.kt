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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
import com.vextrainer.android.presentation.components.MarkdownText
import com.vextrainer.android.presentation.components.inlineMarkdown
import com.vextrainer.android.presentation.components.OptionState
import com.vextrainer.android.presentation.components.QuizProgressBar
import com.vextrainer.android.presentation.components.VexTopAppBar

private val OPTION_LABELS = listOf("A", "B", "C", "D", "E", "F")

@Composable
fun QuizSessionScreen(
    onNavigateToResults: (attemptId: Int) -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: QuizSessionViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler { showExitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QuizSessionEvent.NavigateToResults -> onNavigateToResults(event.attemptId)
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
                onLogoClick = onHomeClick
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
                            Text(text = stringResource(R.string.quiz_completing),
                                 style = MaterialTheme.typography.bodyLarge)
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
                            question                  = question,
                            uiState                   = uiState,
                            onSelectAnswer            = viewModel::selectAnswer,
                            onSelectMatchingAnswer    = viewModel::selectMatchingAnswer,
                            onResetMatching           = viewModel::resetMatchingPairs,
                            onFillInBlankTextChanged  = viewModel::onFillInBlankTextChanged,
                            onSubmit                  = viewModel::submitAnswer,
                            onNext                    = viewModel::nextQuestion
                        )
                    }
                }
            }
        }
    }
}

// Question content

@Composable
private fun QuizQuestionContent(
    question: QuizQuestion,
    uiState: QuizSessionUiState,
    onSelectAnswer: (Int) -> Unit,
    onSelectMatchingAnswer: (answerId: Int, matchSide: String) -> Unit,
    onResetMatching: () -> Unit,
    onFillInBlankTextChanged: (String) -> Unit,
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

        // Question type hint
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

        // Question text: rendered as Markdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            MarkdownText(
                text     = question.questionText,
                color    = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 16f,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }

        // Answer options
        when {
            uiState.isFillInBlank -> FillInBlankInput(
                text          = uiState.fillInBlankText,
                onTextChanged = onFillInBlankTextChanged,
                isRevealed    = isRevealed,
                onSubmit      = onSubmit
            )
            uiState.isMatchingQuestion -> MatchingQuestionContent(
                answers        = question.answers,
                matchingPairs  = uiState.matchingPairs,
                selectedLeftId = uiState.selectedLeftId,
                isRevealed     = isRevealed,
                correctRIds    = parseCorrectMatchIds(uiState.answerResult?.correctAnswerJson),
                onPairSelected = onSelectMatchingAnswer,
                onReset        = onResetMatching
            )
            else -> question.answers.forEachIndexed { index, answer ->
                val optionState = resolveOptionState(
                    answer       = answer,
                    selectedIds  = uiState.selectedAnswerIds,
                    answerResult = uiState.answerResult,
                    isRevealed   = isRevealed
                )
                AnswerOptionButton(
                    label   = OPTION_LABELS.getOrElse(index) { "${index + 1}" },
                    text    = answer.answerText,
                    state   = optionState,
                    enabled = !isRevealed,
                    onClick = { onSelectAnswer(answer.answerId) }
                )
            }
        }

        // Feedback / explanation: rendered as Markdown
        uiState.answerResult?.let { result ->
            if (isRevealed) FeedbackCard(answerResult = result)
        }

        // Action buttons
        if (!isRevealed) {
            Button(
                onClick  = onSubmit,
                enabled  = when {
                    uiState.isFillInBlank      -> uiState.fillInBlankText.isNotBlank()
                    uiState.isMatchingQuestion -> uiState.matchingComplete
                    else                       -> uiState.selectedAnswerIds.isNotEmpty()
                },
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


// Fill-in-blank input

@Composable
private fun FillInBlankInput(
    text: String,
    onTextChanged: (String) -> Unit,
    isRevealed: Boolean,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value           = text,
        onValueChange   = { if (!isRevealed) onTextChanged(it) },
        label           = { Text(stringResource(R.string.quiz_your_answer)) },
        singleLine      = true,
        enabled         = !isRevealed,
        modifier        = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                if (text.isNotBlank()) onSubmit()
            }
        )
    )
}

// Matching question

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
    onPairSelected: (answerId: Int, matchSide: String) -> Unit,
    onReset: () -> Unit = {}
) {
    val leftItems     = answers.filter { it.matchSide == "L" }
    val rightItems    = answers.filter { it.matchSide == "R" }
    val pairedLeftIds = matchingPairs.map { it.first }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left column
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.quiz_matching_component), style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 modifier = Modifier.padding(bottom = 2.dp))

            leftItems.forEach { answer ->
                val pairIndex  = pairedLeftIds.indexOf(answer.answerId)
                val isPaired   = pairIndex >= 0
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

                val textColor = if (isPaired || isSelected) borderColor
                                else MaterialTheme.colorScheme.onSurface

                MatchingItemButton(
                    text        = answer.answerText,
                    borderColor = borderColor,
                    bgColor     = bgColor,
                    textColor   = textColor,
                    enabled     = !isRevealed,
                    onClick     = { onPairSelected(answer.answerId, "L") }
                )
            }
        }

        // Right column
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(R.string.quiz_matching_purpose), style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 modifier = Modifier.padding(bottom = 2.dp))

            rightItems.forEach { answer ->
                val pairedLeftId = matchingPairs.find { it.second == answer.answerId }?.first
                val pairIndex    = if (pairedLeftId != null) pairedLeftIds.indexOf(pairedLeftId) else -1
                val isPaired     = pairIndex >= 0
                val isCorrectR   = isRevealed && answer.answerId in correctRIds

                val (bgColor, borderColor) = when {
                    isRevealed && isPaired ->
                        if (isCorrectR) Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                        else Pair(Color(0xFFFFEBEE), Color(0xFFB71C1C))
                    isPaired -> PAIR_COLORS[pairIndex % PAIR_COLORS.size]
                    else     -> Pair(MaterialTheme.colorScheme.surface,
                                     MaterialTheme.colorScheme.outline)
                }

                val textColor = if (isPaired) borderColor else MaterialTheme.colorScheme.onSurface

                MatchingItemButton(
                    text        = answer.answerText,
                    borderColor = borderColor,
                    bgColor     = bgColor,
                    textColor   = textColor,
                    enabled     = !isRevealed,
                    onClick     = { onPairSelected(answer.answerId, "R") }
                )
            }
        }
    }

    if (!isRevealed) {
        val pairedCount = matchingPairs.size
        val leftCount   = leftItems.size
        Row(
            modifier          = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
        Text(text = stringResource(R.string.quiz_matching_paired_count, pairedCount, leftCount),
             style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (selectedLeftId != null) {
            val selName = leftItems.find { it.answerId == selectedLeftId }?.answerText ?: ""
            Text(text = stringResource(R.string.quiz_matching_selected, selName),
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.primary)
                }
            }
            if (matchingPairs.isNotEmpty()) {
                TextButton(onClick = onReset) {
                    Text(
                        text  = stringResource(R.string.quiz_matching_reset),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Button used for matching question items.
 * Answer text is rendered via [MarkdownText] so inline code and formatting display correctly.
 */
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
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        // inlineMarkdown + Text keeps touches fully passable to the OutlinedButton
        Text(
            text     = inlineMarkdown(text),
            style    = MaterialTheme.typography.bodyMedium,
            color    = textColor,
            textAlign = TextAlign.Center
        )
    }
}

// Feedback card

/**
 * Shows correct/incorrect result and explanation after an answer is submitted.
 * Explanation is rendered via [MarkdownText] so code snippets and formatting display correctly.
 */
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
            Text(
                text       = if (isCorrect) stringResource(R.string.quiz_correct)
                             else stringResource(R.string.quiz_incorrect),
                style      = MaterialTheme.typography.titleLarge,
                color      = textColor,
                fontWeight = FontWeight.Bold
            )
            answerResult.explanation?.let { exp ->
                Spacer(Modifier.height(6.dp))
                // Explanation rendered as Markdown — supports inline code, bold, etc.
                MarkdownText(
                    text     = exp,
                    color    = textColor,
                    fontSize = 14f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Option state resolver

private fun resolveOptionState(
    answer: QuizAnswer,
    selectedIds: List<Int>,
    answerResult: com.vextrainer.android.domain.model.quiz.AnswerResult?,
    isRevealed: Boolean
): OptionState {
    if (!isRevealed) {
        return if (answer.answerId in selectedIds) OptionState.SELECTED else OptionState.DEFAULT
    }
    val wasSelected    = answer.answerId in selectedIds
    val apiSaysCorrect = answerResult?.isCorrect == true
    val json           = answerResult?.correctAnswerJson

    // Parse correct IDs — works for all API formats via regex:
    //   Single:         {"answer_id":4306}            -> [4306]
    //   Multiple:       [{"answer_id":4300},...]       -> [4300, 4301, ...]
    //   Fill-in-blank:  {"text":"fiasco"}              -> [] (no answer_id key)
    val correctIds: List<Int> = parseCorrectMatchIds(json)

    return when {
        correctIds.isNotEmpty() && answer.answerId in correctIds  && wasSelected  -> OptionState.CORRECT
        correctIds.isNotEmpty() && answer.answerId in correctIds  && !wasSelected -> OptionState.MISSED
        correctIds.isNotEmpty() && answer.answerId !in correctIds && wasSelected  -> OptionState.INCORRECT
        // No IDs parseable (fill-in-blank, null json) — fall back to API flag
        correctIds.isEmpty() && wasSelected && apiSaysCorrect  -> OptionState.CORRECT
        correctIds.isEmpty() && wasSelected && !apiSaysCorrect -> OptionState.INCORRECT
        else -> OptionState.DEFAULT
    }
}

// JSON parsers

// Regex extracts every answer_id / answerId integer value from any JSON format:
//   single:   {"answer_id":4306}
//   array:    [{"answer_id":4300},{"answer_id":4301},{"answer_id":4302}]
// Splitting on },{ was fragile — it consumed braces and broke toIntOrNull().
private val answerIdRegex = Regex(""""(?:answer_id|answerId)"\s*:\s*(\d+)""")

private fun parseCorrectAnswerId(json: String): Int? =
    answerIdRegex.find(json)?.groupValues?.getOrNull(1)?.toIntOrNull()

private fun parseCorrectMatchIds(json: String?): List<Int> {
    if (json.isNullOrBlank()) return emptyList()
    return answerIdRegex.findAll(json)
        .mapNotNull { it.groupValues.getOrNull(1)?.toIntOrNull() }
        .toList()
}
