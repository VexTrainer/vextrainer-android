package com.vextrainer.android.data.remote.dto.lesson

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActivityTopicDto(
    val readDate: String,           // "2026-05-30T00:00:00" — date portion used, time discarded
    val moduleId: Int,
    val moduleName: String,
    val lessonId: Int,
    val lessonTitle: String,
    val topicId: Int,
    val topicTitle: String
)

@JsonClass(generateAdapter = true)
data class ActivityQuizDto(
    val attemptDate: String,        // "2026-06-02T00:00:00" — date portion used, time discarded
    val quizId: Int,
    val quizTitle: String,
    val bestScore: Double?,         // null when all attempts on this day are incomplete
    val isCompleted: Boolean,       // true if any attempt completed
    val attemptCount: Int,          // used for "(Nx)" label when > 1
    val latestAttemptId: Int
)

@JsonClass(generateAdapter = true)
data class StreakBadgeReportDto(
    val topics: List<ActivityTopicDto>,
    val quizzes: List<ActivityQuizDto>
)
