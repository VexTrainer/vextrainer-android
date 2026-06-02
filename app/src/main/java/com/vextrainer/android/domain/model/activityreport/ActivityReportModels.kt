package com.vextrainer.android.domain.model.lesson

import com.vextrainer.android.data.remote.dto.lesson.ActivityQuizDto
import com.vextrainer.android.data.remote.dto.lesson.ActivityTopicDto
import com.vextrainer.android.data.remote.dto.lesson.StreakBadgeReportDto

// Flat domain models

data class ActivityTopic(
    val readDate: String,       // date portion only: "2026-05-30"
    val moduleId: Int,
    val moduleName: String,
    val lessonId: Int,
    val lessonTitle: String,
    val topicId: Int,
    val topicTitle: String
)

data class ActivityQuiz(
    val attemptDate: String,    // date portion only: "2026-06-02"
    val quizId: Int,
    val quizTitle: String,
    val bestScore: Double?,
    val isCompleted: Boolean,
    val attemptCount: Int,
    val latestAttemptId: Int
)

data class StreakBadgeReport(
    val topics: List<ActivityTopic>,
    val quizzes: List<ActivityQuiz>
)

// Grouped UI models — date -> module -> lesson -> topic
// These live here so ViewModel and Screen can both reference them without
// a circular dependency. They are pure data — no Android/Compose imports.

data class DayActivity(
    val dateKey: String,            // "2026-05-30" — used as stable LazyColumn key
    val dateLabel: String,          // "Today", "Yesterday", or "May 30, 2026"
    val modules: List<ModuleActivity>,
    val quizzes: List<ActivityQuiz>
)

data class ModuleActivity(
    val moduleId: Int,
    val moduleName: String,
    val lessons: List<LessonActivity>
)

data class LessonActivity(
    val lessonId: Int,
    val lessonTitle: String,
    val topics: List<ActivityTopic>
)

// DTO -> Domain mappers

private fun String.toDateKey() = if (length >= 10) substring(0, 10) else this

fun ActivityTopicDto.toDomain() = ActivityTopic(
    readDate    = readDate.toDateKey(),
    moduleId    = moduleId,
    moduleName  = moduleName,
    lessonId    = lessonId,
    lessonTitle = lessonTitle,
    topicId     = topicId,
    topicTitle  = topicTitle
)

fun ActivityQuizDto.toDomain() = ActivityQuiz(
    attemptDate     = attemptDate.toDateKey(),
    quizId          = quizId,
    quizTitle       = quizTitle,
    bestScore       = bestScore,
    isCompleted     = isCompleted,
    attemptCount    = attemptCount,
    latestAttemptId = latestAttemptId
)

fun StreakBadgeReportDto.toDomain() = StreakBadgeReport(
    topics  = topics.map { it.toDomain() },
    quizzes = quizzes.map { it.toDomain() }
)
