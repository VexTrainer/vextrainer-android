package com.vextrainer.android.presentation.ui.lessons.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.ActivityQuiz
import com.vextrainer.android.domain.model.lesson.ActivityTopic
import com.vextrainer.android.domain.model.lesson.DayActivity
import com.vextrainer.android.domain.model.lesson.LessonActivity
import com.vextrainer.android.domain.model.lesson.ModuleActivity
import com.vextrainer.android.domain.model.lesson.StreakBadgeReport
import com.vextrainer.android.domain.usecase.lesson.GetStreakBadgeReportUseCase
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class ActivityReportUiState(
    val isLoading: Boolean      = true,
    val days: List<DayActivity> = emptyList(),
    val error: UiText?          = null
)

@HiltViewModel
class ActivityReportViewModel @Inject constructor(
    private val getStreakBadgeReportUseCase: GetStreakBadgeReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityReportUiState())
    val uiState: StateFlow<ActivityReportUiState> = _uiState.asStateFlow()

    init { loadReport() }

    fun loadReport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Derive the device's UTC offset in minutes at the current instant
            val offsetMinutes = TimeZone.getDefault()
                .getOffset(System.currentTimeMillis()) / 60_000
            getStreakBadgeReportUseCase(offsetMinutes)
                .onSuccess { report ->
                    _uiState.update {
                        it.copy(isLoading = false, days = report.toGroupedDays())
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = e.toUiText(R.string.error_load_activity_report)
                        )
                    }
                }
        }
    }
}

// Grouping: flat lists → date → module → lesson → topic

private fun StreakBadgeReport.toGroupedDays(): List<DayActivity> {
    val topicsByDate  = topics.groupBy { it.readDate }
    val quizzesByDate = quizzes.groupBy { it.attemptDate }
    val allDates = (topicsByDate.keys + quizzesByDate.keys)
        .distinct()
        .sortedDescending()

    return allDates.map { dateKey ->
        val dayTopics  = topicsByDate[dateKey]  ?: emptyList()
        val dayQuizzes = quizzesByDate[dateKey] ?: emptyList()

        val moduleActivities = dayTopics
            .groupBy { it.moduleId to it.moduleName }
            .entries
            .sortedBy { it.key.first }
            .map { (moduleInfo, moduleTopics) ->
                val lessons = moduleTopics
                    .groupBy { it.lessonId to it.lessonTitle }
                    .entries
                    .sortedBy { it.key.first }
                    .map { (lessonInfo, lessonTopics) ->
                        LessonActivity(
                            lessonId    = lessonInfo.first,
                            lessonTitle = lessonInfo.second,
                            topics      = lessonTopics
                        )
                    }
                ModuleActivity(
                    moduleId   = moduleInfo.first,
                    moduleName = moduleInfo.second,
                    lessons    = lessons
                )
            }

        DayActivity(
            dateKey   = dateKey,
            dateLabel = formatDateLabel(dateKey),
            modules   = moduleActivities,
            quizzes   = dayQuizzes
        )
    }
}

private fun formatDateLabel(dateKey: String): String {
    return try {
        val sdf  = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = sdf.parse(dateKey) ?: return dateKey

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val yesterdayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val dateCal = Calendar.getInstance().apply { time = date }

        when {
            !dateCal.before(todayCal)     -> "Today"
            !dateCal.before(yesterdayCal) -> "Yesterday"
            else -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        dateKey
    }
}
