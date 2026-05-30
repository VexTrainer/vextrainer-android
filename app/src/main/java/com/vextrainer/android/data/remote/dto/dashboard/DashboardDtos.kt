package com.vextrainer.android.data.remote.dto.dashboard

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookmarkItemDto(
    val moduleId: Int,
    val moduleName: String,
    val lessonId: Int,
    val lessonTitle: String,
    val topicId: Int,
    val topicTitle: String
)

@JsonClass(generateAdapter = true)
data class DashboardResponseDto(
    val stats: DashboardStatsDto,
    val continueLearning: List<ContinueLearningItemDto>,
    val bookmarks: List<BookmarkItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class DashboardStatsDto(
    val totalModules: Int,
    val completedModules: Int,
    val totalLessons: Int,
    val completedLessons: Int,
    val totalTopics: Int,
    val topicsRead: Int,
    val quizzesAttempted: Int,
    val quizzesCompleted: Int,
    val averageQuizScore: Double,
    val bestQuizScore: Double,
    val readingStreak: Int,
    val modulesProgressPercent: Double,
    val lessonsProgressPercent: Double,
    val topicsProgressPercent: Double
)

@JsonClass(generateAdapter = true)
data class ContinueLearningItemDto(
    val lessonId: Int,
    val lessonTitle: String,
    val moduleId: Int,
    val moduleName: String,
    val topicsRead: Int,
    val totalTopics: Int,
    val nextTopicId: Int,
    val nextTopicTitle: String
)
