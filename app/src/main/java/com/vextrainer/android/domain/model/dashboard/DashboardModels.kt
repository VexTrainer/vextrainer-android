package com.vextrainer.android.domain.model.dashboard

import com.vextrainer.android.data.remote.dto.dashboard.ContinueLearningItemDto
import com.vextrainer.android.data.remote.dto.dashboard.DashboardResponseDto
import com.vextrainer.android.data.remote.dto.dashboard.DashboardStatsDto

data class DashboardStats(
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

data class ContinueLearningItem(
    val lessonId: Int,
    val lessonTitle: String,
    val moduleId: Int,
    val moduleName: String,
    val topicsRead: Int,
    val totalTopics: Int,
    val nextTopicId: Int,
    val nextTopicTitle: String
)

data class Dashboard(
    val stats: DashboardStats,
    val continueLearning: List<ContinueLearningItem>
)

fun DashboardStatsDto.toDomain() = DashboardStats(
    totalModules           = totalModules,
    completedModules       = completedModules,
    totalLessons           = totalLessons,
    completedLessons       = completedLessons,
    totalTopics            = totalTopics,
    topicsRead             = topicsRead,
    quizzesAttempted       = quizzesAttempted,
    quizzesCompleted       = quizzesCompleted,
    averageQuizScore       = averageQuizScore,
    bestQuizScore          = bestQuizScore,
    readingStreak          = readingStreak,
    modulesProgressPercent = modulesProgressPercent,
    lessonsProgressPercent = lessonsProgressPercent,
    topicsProgressPercent  = topicsProgressPercent
)

fun ContinueLearningItemDto.toDomain() = ContinueLearningItem(
    lessonId       = lessonId,
    lessonTitle    = lessonTitle,
    moduleId       = moduleId,
    moduleName     = moduleName,
    topicsRead     = topicsRead,
    totalTopics    = totalTopics,
    nextTopicId    = nextTopicId,
    nextTopicTitle = nextTopicTitle
)

fun DashboardResponseDto.toDomain() = Dashboard(
    stats            = stats.toDomain(),
    continueLearning = continueLearning.map { it.toDomain() }
)
