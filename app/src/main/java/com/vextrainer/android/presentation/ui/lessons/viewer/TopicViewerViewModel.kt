package com.vextrainer.android.presentation.ui.lessons.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.TopicDetails
import com.vextrainer.android.domain.usecase.dashboard.AddBookmarkUseCase
import com.vextrainer.android.domain.usecase.dashboard.DeleteBookmarkUseCase
import com.vextrainer.android.domain.usecase.lesson.FetchMarkdownUseCase
import com.vextrainer.android.domain.usecase.lesson.GetTopicDetailsUseCase
import com.vextrainer.android.domain.usecase.lesson.MarkTopicReadUseCase
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

data class TopicViewerUiState(
    val isLoadingMeta: Boolean    = true,
    val isLoadingContent: Boolean = false,
    val isMarkingRead: Boolean    = false,
    val isBookmarked: Boolean     = false,
    val isTogglingBookmark: Boolean = false,
    val topic: TopicDetails?      = null,
    val markdownContent: String   = "",
    val isRead: Boolean           = false,
    val metaError: UiText?        = null,
    val contentError: UiText?     = null
)

@HiltViewModel
class TopicViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTopicDetailsUseCase: GetTopicDetailsUseCase,
    private val fetchMarkdownUseCase: FetchMarkdownUseCase,
    private val markTopicReadUseCase: MarkTopicReadUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase
) : ViewModel() {

    private val topicId: Int = checkNotNull(savedStateHandle[Screen.TopicViewer.ARG_TOPIC_ID])

    private val _uiState = MutableStateFlow(TopicViewerUiState())
    val uiState: StateFlow<TopicViewerUiState> = _uiState.asStateFlow()

    init { loadTopic() }

    fun loadTopic() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMeta    = true,
                    isLoadingContent = false,
                    metaError        = null,
                    contentError     = null,
                    markdownContent  = "",
                    topic            = null
                )
            }
            getTopicDetailsUseCase(topicId)
                .onSuccess { topic ->
                    _uiState.update {
                        it.copy(
                            isLoadingMeta    = false,
                            topic            = topic,
                            isRead           = topic.isRead,
                            isBookmarked     = topic.isBookmarked,
                            isLoadingContent = true
                        )
                    }
                    fetchContent(topic.markdownUrl)
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingMeta = false,
                            metaError     = e.toUiText(R.string.error_load_topic)
                        )
                    }
                }
        }
    }

    private fun fetchContent(url: String) {
        viewModelScope.launch {
            fetchMarkdownUseCase(url)
                .onSuccess { md ->
                    _uiState.update { it.copy(isLoadingContent = false, markdownContent = md) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingContent = false,
                            contentError     = e.toUiText(R.string.topic_content_error)
                        )
                    }
                }
        }
    }

    fun markAsRead() {
        if (_uiState.value.isRead || _uiState.value.isMarkingRead) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingRead = true) }
            markTopicReadUseCase(topicId)
                .onSuccess { _uiState.update { it.copy(isRead = true, isMarkingRead = false) } }
                .onFailure { _uiState.update { it.copy(isMarkingRead = false) } }
        }
    }

    /**
     * Toggles bookmark state optimistically — UI updates immediately,
     * reverts silently if the API call fails.
     */
    fun toggleBookmark() {
        if (_uiState.value.isTogglingBookmark) return
        val currentlyBookmarked = _uiState.value.isBookmarked
        viewModelScope.launch {
            _uiState.update { it.copy(isBookmarked = !currentlyBookmarked, isTogglingBookmark = true) }
            val result = if (currentlyBookmarked) deleteBookmarkUseCase(topicId)
                         else                     addBookmarkUseCase(topicId)
            result.onFailure {
                // Revert optimistic update on failure
                _uiState.update { it.copy(isBookmarked = currentlyBookmarked) }
            }
            _uiState.update { it.copy(isTogglingBookmark = false) }
        }
    }

    fun retryMeta()    = loadTopic()
    fun retryContent() = _uiState.value.topic?.let { fetchContent(it.markdownUrl) }
}
