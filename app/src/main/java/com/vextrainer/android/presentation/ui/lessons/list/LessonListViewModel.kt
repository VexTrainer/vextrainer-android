package com.vextrainer.android.presentation.ui.lessons.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.LessonSummary
import com.vextrainer.android.domain.usecase.lesson.GetLessonsByModuleUseCase
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

data class LessonListUiState(
    val isLoading: Boolean = true,
    val moduleName: String = "",
    val lessons: List<LessonSummary> = emptyList(),
    val error: UiText? = null
)

@HiltViewModel
class LessonListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLessonsByModuleUseCase: GetLessonsByModuleUseCase
) : ViewModel() {

    private val moduleId: Int = checkNotNull(savedStateHandle[Screen.LessonList.ARG_MODULE_ID])
    private val moduleName: String = savedStateHandle[Screen.LessonList.ARG_MODULE_NAME] ?: ""

    private val _uiState = MutableStateFlow(LessonListUiState(moduleName = moduleName))
    val uiState: StateFlow<LessonListUiState> = _uiState.asStateFlow()

    init { loadLessons() }

    fun loadLessons() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getLessonsByModuleUseCase(moduleId)
                .onSuccess { lessons ->
                    _uiState.update { it.copy(isLoading = false, lessons = lessons) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.toUiText(R.string.error_load_lessons))
                    }
                }
        }
    }
}
