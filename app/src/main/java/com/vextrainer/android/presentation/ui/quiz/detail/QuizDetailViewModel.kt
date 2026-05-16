package com.vextrainer.android.presentation.ui.quiz.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizDetail
import com.vextrainer.android.domain.usecase.quiz.GetQuizDetailUseCase
import com.vextrainer.android.domain.usecase.quiz.StartQuizUseCase
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

data class QuizDetailUiState(
    val isLoading: Boolean = false,
    val quiz: QuizDetail? = null,
    val isStarting: Boolean = false,
    val error: UiText? = null
)

sealed class QuizDetailEvent {
    data class NavigateToSession(val attemptId: Int) : QuizDetailEvent()
}

@HiltViewModel
class QuizDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuizDetailUseCase: GetQuizDetailUseCase,
    private val startQuizUseCase: StartQuizUseCase
) : ViewModel() {

    private val quizId: Int = checkNotNull(savedStateHandle[Screen.QuizDetail.ARG_QUIZ_ID])

    private val _uiState = MutableStateFlow(QuizDetailUiState())
    val uiState: StateFlow<QuizDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<QuizDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadQuizDetail()
    }

    fun loadQuizDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getQuizDetailUseCase(quizId)
                .onSuccess { quiz ->
                    _uiState.update { it.copy(isLoading = false, quiz = quiz) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUiText(R.string.error_load_quiz_detail)
                        )
                    }
                }
        }
    }

    fun startQuiz() {
        if (_uiState.value.isStarting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isStarting = true, error = null) }
            startQuizUseCase(quizId)
                .onSuccess { attempt ->
                    _uiState.update { it.copy(isStarting = false) }
                    _events.send(QuizDetailEvent.NavigateToSession(attempt.attemptId))
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isStarting = false,
                            error = e.toUiText(R.string.error_start_quiz)
                        )
                    }
                }
        }
    }
}
