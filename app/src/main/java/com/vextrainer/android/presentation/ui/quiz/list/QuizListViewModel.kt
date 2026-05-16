package com.vextrainer.android.presentation.ui.quiz.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizSummary
import com.vextrainer.android.domain.usecase.quiz.GetQuizzesByCategoryUseCase
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

data class QuizListUiState(
    val isLoading: Boolean = false,
    val categoryName: String = "",
    val quizzes: List<QuizSummary> = emptyList(),
    val error: UiText? = null
)

@HiltViewModel
class QuizListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getQuizzesByCategoryUseCase: GetQuizzesByCategoryUseCase
) : ViewModel() {

    private val categoryId: Int =
        checkNotNull(savedStateHandle[Screen.QuizList.ARG_CATEGORY_ID])

    private val categoryName: String =
        savedStateHandle[Screen.QuizList.ARG_CATEGORY_NAME] ?: ""

    private val _uiState = MutableStateFlow(QuizListUiState(categoryName = categoryName))
    val uiState: StateFlow<QuizListUiState> = _uiState.asStateFlow()

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getQuizzesByCategoryUseCase(categoryId)
                .onSuccess { quizzes ->
                    _uiState.update { it.copy(isLoading = false, quizzes = quizzes) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUiText(R.string.error_load_quizzes)
                        )
                    }
                }
        }
    }
}
