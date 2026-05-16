package com.vextrainer.android.presentation.ui.quiz.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizResults
import com.vextrainer.android.domain.usecase.quiz.GetQuizResultsUseCase
import com.vextrainer.android.presentation.navigation.Screen
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizResultUiState(
    val isLoading: Boolean = true,
    val results: QuizResults? = null,
    val error: UiText? = null
)

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuizResultsUseCase: GetQuizResultsUseCase
) : ViewModel() {

    private val attemptId: Int =
        checkNotNull(savedStateHandle[Screen.QuizResult.ARG_ATTEMPT_ID])

    private val _uiState = MutableStateFlow(QuizResultUiState())
    val uiState: StateFlow<QuizResultUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    fun loadResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getQuizResultsUseCase(attemptId)
                .onSuccess { results ->
                    _uiState.update { it.copy(isLoading = false, results = results) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUiText(R.string.error_load_results)
                        )
                    }
                }
        }
    }
}
