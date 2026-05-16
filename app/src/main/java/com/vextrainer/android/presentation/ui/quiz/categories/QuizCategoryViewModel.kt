package com.vextrainer.android.presentation.ui.quiz.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizCategory
import com.vextrainer.android.domain.usecase.quiz.GetQuizCategoriesUseCase
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizCategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<QuizCategory> = emptyList(),
    val error: UiText? = null
)

@HiltViewModel
class QuizCategoryViewModel @Inject constructor(
    private val getQuizCategoriesUseCase: GetQuizCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizCategoryUiState())
    val uiState: StateFlow<QuizCategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getQuizCategoriesUseCase()
                .onSuccess { categories ->
                    _uiState.update { it.copy(isLoading = false, categories = categories) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUiText(R.string.error_load_categories)
                        )
                    }
                }
        }
    }
}
