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

// Flat list item types
// Flattening the two-level tree into a single list lets LazyColumn use items()
// with a known count, composing only visible rows rather than iterating the
// full tree to register slots.

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

data class QuizCategoryUiState(
    val isLoading:  Boolean                 = true,   // true initially — single emission on load
    val categories: List<QuizCategory>      = emptyList(),
    val flatItems:  List<CategoryListItem>  = emptyList(),
    val error:      UiText?                 = null,
    val expandedIds: Set<Int>               = emptySet()
)

@HiltViewModel
class QuizCategoryViewModel @Inject constructor(
    private val getQuizCategoriesUseCase: GetQuizCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizCategoryUiState())
    val uiState: StateFlow<QuizCategoryUiState> = _uiState.asStateFlow()

    init { loadCategories() }

    fun loadCategories() {
        viewModelScope.launch {
            // Single update: set loading=true and clear error — no isLoading=true then false
            // emission (avoids an extra recomposition transition from LoadingOverlay→CategoryList).
            _uiState.update { it.copy(isLoading = true, error = null) }
            getQuizCategoriesUseCase()
                .onSuccess { categories ->
                    _uiState.update {
                        it.copy(
                            isLoading   = false,
                            categories  = categories,
                            expandedIds = emptySet(),
                            flatItems   = buildFlatList(categories, emptySet())
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
// Converts the category tree into a flat list for LazyColumn.
// Children only appear when their parent is in expandedIds.

private fun buildFlatList(
    categories: List<QuizCategory>,
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
