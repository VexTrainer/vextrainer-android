package com.vextrainer.android.presentation.ui.quiz.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.quiz.QuizHistoryItem
import com.vextrainer.android.domain.usecase.quiz.GetQuizHistoryUseCase
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizHistoryUiState(
    val isLoading: Boolean = true,
    val attempts: List<QuizHistoryItem> = emptyList(),
    val error: UiText? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false
)

@HiltViewModel
class QuizHistoryViewModel @Inject constructor(
    private val getQuizHistoryUseCase: GetQuizHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizHistoryUiState())
    val uiState: StateFlow<QuizHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, page = 1) }
            getQuizHistoryUseCase(page = 1, limit = 20)
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            attempts  = history.attempts,
                            hasMore   = history.attempts.size < history.totalCount,
                            page      = 1
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.toUiText(R.string.error_load_history)
                        )
                    }
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoadingMore) return

        val nextPage = state.page + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            getQuizHistoryUseCase(page = nextPage, limit = 20)
                .onSuccess { history ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            attempts = it.attempts + history.attempts,
                            hasMore  = (it.attempts.size + history.attempts.size) < history.totalCount,
                            page     = nextPage
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }
}
