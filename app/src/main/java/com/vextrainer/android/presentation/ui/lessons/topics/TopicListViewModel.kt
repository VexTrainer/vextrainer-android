package com.vextrainer.android.presentation.ui.lessons.topics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.TopicSummary
import com.vextrainer.android.domain.usecase.lesson.GetTopicsByLessonUseCase
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

data class TopicListUiState(
    val isLoading: Boolean = true,
    val lessonTitle: String = "",
    val topics: List<TopicSummary> = emptyList(),
    val error: UiText? = null
)

@HiltViewModel
class TopicListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTopicsByLessonUseCase: GetTopicsByLessonUseCase
) : ViewModel() {

    private val lessonId: Int = checkNotNull(savedStateHandle[Screen.TopicList.ARG_LESSON_ID])
    private val lessonTitle: String = savedStateHandle[Screen.TopicList.ARG_LESSON_TITLE] ?: ""

    private val _uiState = MutableStateFlow(TopicListUiState(lessonTitle = lessonTitle))
    val uiState: StateFlow<TopicListUiState> = _uiState.asStateFlow()

    init { loadTopics() }

    fun loadTopics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTopicsByLessonUseCase(lessonId)
                .onSuccess { topics ->
                    _uiState.update { it.copy(isLoading = false, topics = topics) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.toUiText(R.string.error_load_topics))
                    }
                }
        }
    }
}
