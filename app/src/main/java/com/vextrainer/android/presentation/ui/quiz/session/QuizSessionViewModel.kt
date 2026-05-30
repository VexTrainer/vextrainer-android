package com.vextrainer.android.presentation.ui.quiz.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.AnswerResult
import com.vextrainer.android.domain.model.quiz.QuizQuestion
import com.vextrainer.android.domain.model.quiz.QuestionType
import com.vextrainer.android.domain.usecase.quiz.CompleteQuizUseCase
import com.vextrainer.android.domain.usecase.quiz.GetQuestionsUseCase
import com.vextrainer.android.domain.usecase.quiz.ResumeQuizAttemptUseCase
import com.vextrainer.android.domain.usecase.quiz.SubmitAnswerUseCase
import com.vextrainer.android.presentation.navigation.Screen
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State machine phases

enum class SessionPhase {
    LOADING,
    QUESTION_DISPLAYED,
    ANSWER_SELECTED,
    SUBMITTING,
    ANSWER_REVEALED,
    COMPLETING,
    ERROR
}

// UI State

data class QuizSessionUiState(
    val phase: SessionPhase = SessionPhase.LOADING,

    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,

    // Single / multi-answer selection
    val selectedAnswerIds: List<Int> = emptyList(),

    // Fill-in-blank typed answer
    val fillInBlankText: String = "",

    // Matching question state
    // Each pair is (leftAnswerId, rightAnswerId)
    val matchingPairs: List<Pair<Int, Int>> = emptyList(),
    // The L-side item the user tapped and is waiting to pair with an R item
    val selectedLeftId: Int? = null,

    // After submit
    val answerResult: AnswerResult? = null,

    val questionsAnswered: Int = 0,
    val currentScore: Double = 0.0,

    val error: UiText? = null
) {
    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)

    val totalQuestions: Int
        get() = questions.size

    val isLastQuestion: Boolean
        get() = currentIndex >= questions.size - 1

    val progressDisplay: Int
        get() = currentIndex + 1

    val isFillInBlank: Boolean
        get() = currentQuestion?.questionType == QuestionType.FILL_IN_BLANK

    val isMatchingQuestion: Boolean
        get() = currentQuestion?.answers?.any { it.matchSide != null } == true

    /** For matching: true when all L-side items have been paired. */
    val matchingComplete: Boolean
        get() {
            val q = currentQuestion ?: return false
            val leftCount = q.answers.count { it.matchSide == "L" }
            return matchingPairs.size >= leftCount
        }
}

// Events

sealed class QuizSessionEvent {
    data class NavigateToResults(val attemptId: Int) : QuizSessionEvent()
}

// ViewModel

@HiltViewModel
class QuizSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val submitAnswerUseCase: SubmitAnswerUseCase,
    private val completeQuizUseCase: CompleteQuizUseCase,
    private val resumeQuizAttemptUseCase: ResumeQuizAttemptUseCase
) : ViewModel() {

    val attemptId: Int = checkNotNull(savedStateHandle[Screen.QuizSession.ARG_ATTEMPT_ID])
    val isResume: Boolean = savedStateHandle[EXTRA_IS_RESUME] ?: false

    private val _uiState = MutableStateFlow(QuizSessionUiState())
    val uiState: StateFlow<QuizSessionUiState> = _uiState.asStateFlow()

    private val _events = Channel<QuizSessionEvent>()
    val events = _events.receiveAsFlow()

    init {
        if (isResume) resumeSession() else loadQuestions()
    }

    // Load / Resume

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = SessionPhase.LOADING, error = null) }
            getQuestionsUseCase(attemptId)
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            phase        = SessionPhase.QUESTION_DISPLAYED,
                            questions    = data.questions,
                            currentIndex = 0
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            phase = SessionPhase.ERROR,
                            error = e.toUiText(R.string.error_load_questions)
                        )
                    }
                }
        }
    }

    private fun resumeSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = SessionPhase.LOADING, error = null) }
            val resumeResult = resumeQuizAttemptUseCase(attemptId)
            resumeResult.onFailure { loadQuestions(); return@launch }
            val resumeData = resumeResult.getOrNull()!!

            getQuestionsUseCase(attemptId)
                .onSuccess { data ->
                    val startIndex = if (resumeData.answeredQuestionIds.isEmpty()) 0
                    else {
                        val first = data.questions.indexOfFirst {
                            it.questionId !in resumeData.answeredQuestionIds
                        }
                        if (first == -1) data.questions.size else first
                    }
                    if (startIndex >= data.questions.size) {
                        completeQuiz()
                    } else {
                        _uiState.update {
                            it.copy(
                                phase             = SessionPhase.QUESTION_DISPLAYED,
                                questions         = data.questions,
                                currentIndex      = startIndex,
                                questionsAnswered = resumeData.answeredQuestionIds.size,
                                currentScore      = resumeData.score ?: 0.0
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            phase = SessionPhase.ERROR,
                            error = e.toUiText(R.string.error_load_questions)
                        )
                    }
                }
        }
    }

    // Answer selection — single / multi

    fun selectAnswer(answerId: Int) {
        val phase = _uiState.value.phase
        if (phase != SessionPhase.QUESTION_DISPLAYED &&
            phase != SessionPhase.ANSWER_SELECTED) return

        val question = _uiState.value.currentQuestion ?: return

        val newSelection = when (question.questionType) {
            QuestionType.MULTIPLE_ANSWER -> {
                val current = _uiState.value.selectedAnswerIds.toMutableList()
                if (answerId in current) current.remove(answerId) else current.add(answerId)
                current
            }
            else -> listOf(answerId)
        }

        _uiState.update {
            it.copy(
                phase             = if (newSelection.isEmpty()) SessionPhase.QUESTION_DISPLAYED
                                    else SessionPhase.ANSWER_SELECTED,
                selectedAnswerIds = newSelection
            )
        }
    }

    // Answer input — fill-in-blank

    fun onFillInBlankTextChanged(text: String) {
        _uiState.update {
            it.copy(
                fillInBlankText = text,
                phase           = if (text.isNotBlank()) SessionPhase.ANSWER_SELECTED
                                  else SessionPhase.QUESTION_DISPLAYED
            )
        }
    }

    // Answer selection — matching

    /**
     * Called when the user taps any answer in a matching question.
     * matchSide is "L" or "R".
     *
     * Interaction model:
     *   1. Tap an L item  → it becomes selectedLeftId (highlighted blue)
     *   2. Tap an R item  → pair is formed (both highlight green)
     *   3. Tap a paired item → removes that pair (allows re-matching)
     *   4. Tap a different L while one is selected → switches selection
     */
    fun selectMatchingAnswer(answerId: Int, matchSide: String) {
        val phase = _uiState.value.phase
        if (phase != SessionPhase.QUESTION_DISPLAYED &&
            phase != SessionPhase.ANSWER_SELECTED) return

        val state = _uiState.value
        val pairs = state.matchingPairs.toMutableList()

        if (matchSide == "L") {
            // Check if already paired — tap to unpair
            val existingPair = pairs.find { it.first == answerId }
            if (existingPair != null) {
                pairs.remove(existingPair)
                _uiState.update {
                    it.copy(
                        matchingPairs = pairs,
                        selectedLeftId = answerId,
                        phase = if (pairs.isEmpty()) SessionPhase.QUESTION_DISPLAYED
                                else SessionPhase.ANSWER_SELECTED
                    )
                }
            } else {
                // Select this L item (replaces any previous L selection)
                _uiState.update { it.copy(selectedLeftId = answerId) }
            }
        } else {
            val leftId = state.selectedLeftId ?: return

            // Check if this R is already paired — tap to unpair
            val existingPair = pairs.find { it.second == answerId }
            if (existingPair != null) {
                pairs.remove(existingPair)
                _uiState.update {
                    it.copy(
                        matchingPairs  = pairs,
                        selectedLeftId = leftId,
                        phase = if (pairs.isEmpty()) SessionPhase.QUESTION_DISPLAYED
                                else SessionPhase.ANSWER_SELECTED
                    )
                }
                return
            }

            // Remove any existing pair that used this L item
            pairs.removeAll { it.first == leftId }
            // Add new pair
            pairs.add(Pair(leftId, answerId))

            val leftCount = state.currentQuestion?.answers?.count { it.matchSide == "L" } ?: 0
            val allPaired = pairs.size >= leftCount

            _uiState.update {
                it.copy(
                    matchingPairs  = pairs,
                    selectedLeftId = null,
                    phase = if (allPaired) SessionPhase.ANSWER_SELECTED
                            else SessionPhase.QUESTION_DISPLAYED
                )
            }
        }
    }

    // Submit

    /** Clears all matching pairs so the user can start pairing from scratch. */
    fun resetMatchingPairs() {
        _uiState.update {
            it.copy(
                matchingPairs  = emptyList(),
                selectedLeftId = null,
                phase          = SessionPhase.QUESTION_DISPLAYED
            )
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (state.phase != SessionPhase.ANSWER_SELECTED) return
        val question = state.currentQuestion ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(phase = SessionPhase.SUBMITTING, error = null) }

            val result = when {
                state.isMatchingQuestion -> submitAnswerUseCase(
                    attemptId    = attemptId,
                    questionId   = question.questionId,
                    questionType = question.questionType,
                    selectedIds  = emptyList(),        // not used for matching -- sortedRIds,
                    matchingPairs = state.matchingPairs
                )
                state.isFillInBlank -> submitAnswerUseCase(
                    attemptId    = attemptId,
                    questionId   = question.questionId,
                    questionType = question.questionType,
                    selectedIds  = emptyList(),
                    fillInText   = state.fillInBlankText
                )
                else -> submitAnswerUseCase(
                    attemptId    = attemptId,
                    questionId   = question.questionId,
                    questionType = question.questionType,
                    selectedIds  = state.selectedAnswerIds
                )
            }

            result
                .onSuccess { answerResult ->
                    _uiState.update {
                        it.copy(
                            phase             = SessionPhase.ANSWER_REVEALED,
                            answerResult      = answerResult,
                            questionsAnswered = answerResult.questionsAnswered,
                            currentScore      = answerResult.currentScore
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            phase = SessionPhase.ERROR,
                            error = e.toUiText(R.string.error_submit_answer)
                        )
                    }
                }
        }
    }

    // Advance

    fun nextQuestion() {
        if (_uiState.value.phase != SessionPhase.ANSWER_REVEALED) return
        if (_uiState.value.isLastQuestion) {
            completeQuiz()
        } else {
            _uiState.update {
                it.copy(
                    phase             = SessionPhase.QUESTION_DISPLAYED,
                    currentIndex      = it.currentIndex + 1,
                    selectedAnswerIds = emptyList(),
                    fillInBlankText   = "",
                    matchingPairs     = emptyList(),
                    selectedLeftId    = null,
                    answerResult      = null
                )
            }
        }
    }

    // Complete

    private fun completeQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = SessionPhase.COMPLETING, error = null) }
            completeQuizUseCase(attemptId)
                .onSuccess { _events.send(QuizSessionEvent.NavigateToResults(attemptId)) }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            phase = SessionPhase.ERROR,
                            error = e.toUiText(R.string.error_complete_quiz)
                        )
                    }
                }
        }
    }

    // Retry

    fun retry() {
        when (_uiState.value.phase) {
            SessionPhase.ERROR -> {
                if (_uiState.value.questions.isEmpty()) {
                    if (isResume) resumeSession() else loadQuestions()
                } else if (_uiState.value.answerResult == null) {
                    _uiState.update {
                        it.copy(phase = SessionPhase.ANSWER_SELECTED, error = null)
                    }
                } else {
                    completeQuiz()
                }
            }
            else -> {}
        }
    }

    companion object {
        const val EXTRA_IS_RESUME = "isResume"
    }
}
