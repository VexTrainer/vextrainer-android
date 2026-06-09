package com.vextrainer.android.presentation.ui.quiz.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.data.repository.QuizRepository
import com.vextrainer.android.domain.model.quiz.QuizCategory
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 14

// Flat list item types

sealed class CategoryListItem {
    data class Parent(
        val category:   QuizCategory,
        val isExpanded: Boolean
    ) : CategoryListItem()

    data class Child(
        val category: QuizCategory,
        val parentId: Int
    ) : CategoryListItem()
}

// UI state

data class QuizCategoryUiState(
    val isLoading:     Boolean                = true,
    val isLoadingMore: Boolean                = false,
    val categories:    List<QuizCategory>     = emptyList(),
    val flatItems:     List<CategoryListItem> = emptyList(),
    val expandedIds:   Set<Int>               = emptySet(),
    val hasMore:       Boolean                = false,
    val currentOffset: Int                    = 0,
    val error:         UiText?                = null
)

// ViewModel

@HiltViewModel
class QuizCategoryViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizCategoryUiState())
    val uiState: StateFlow<QuizCategoryUiState> = _uiState.asStateFlow()

    init { loadCategories() }

    // Initial load (resets all state)

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            quizRepository.getCategoriesPaged(offset = 0, pageSize = PAGE_SIZE)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            isLoading     = false,
                            categories    = page.categories,
                            hasMore       = page.hasMore,
                            currentOffset = PAGE_SIZE,
                            expandedIds   = emptySet(),
                            flatItems     = buildFlatList(page.categories, emptySet())
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = e.toUiText(R.string.error_load_categories)
                        )
                    }
                }
        }
    }

    // Load more (appends next page to existing list)

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoadingMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            quizRepository.getCategoriesPaged(
                offset   = state.currentOffset,
                pageSize = PAGE_SIZE
            )
                .onSuccess { page ->
                    val merged = state.categories + page.categories
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            categories    = merged,
                            hasMore       = page.hasMore,
                            currentOffset = state.currentOffset + PAGE_SIZE,
                            flatItems     = buildFlatList(merged, it.expandedIds)
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            error         = e.toUiText(R.string.error_load_categories)
                        )
                    }
                }
        }
    }

    // Expand / collapse

    fun toggleCategory(categoryId: Int) {
        _uiState.update { state ->
            val newExpanded = if (categoryId in state.expandedIds)
                state.expandedIds - categoryId
            else
                state.expandedIds + categoryId
            state.copy(
                expandedIds = newExpanded,
                flatItems   = buildFlatList(state.categories, newExpanded)
            )
        }
    }
}

// Flat list builder

private fun buildFlatList(
    categories:  List<QuizCategory>,
    expandedIds: Set<Int>
): List<CategoryListItem> {
    val result = ArrayList<CategoryListItem>(categories.size * 2)
    categories.forEach { parent ->
        val isExpanded = parent.categoryId in expandedIds
        result.add(CategoryListItem.Parent(parent, isExpanded))
        if (isExpanded) {
            parent.subcategories.forEach { child ->
                result.add(CategoryListItem.Child(child, parent.categoryId))
            }
        }
    }
    return result
}
