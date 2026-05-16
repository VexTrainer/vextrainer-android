package com.vextrainer.android.domain.model.lesson

import com.vextrainer.android.data.remote.dto.lesson.LessonProgressDto
import com.vextrainer.android.data.remote.dto.lesson.LessonSummaryDto
import com.vextrainer.android.data.remote.dto.lesson.ModuleDto
import com.vextrainer.android.data.remote.dto.lesson.TopicDetailsDto
import com.vextrainer.android.data.remote.dto.lesson.TopicSummaryDto

// ── Domain models ─────────────────────────────────────────────────────────────

data class Module(
    val moduleId: Int,
    val moduleName: String,
    val displayOrder: Int,
    val description: String?,
    val lessonCount: Int,
    val completedLessons: Int
) {
    val progressPercent: Int
        get() = if (lessonCount > 0) (completedLessons * 100) / lessonCount else 0
}

data class LessonSummary(
    val lessonId: Int,
    val lessonTitle: String,
    val displayOrder: Int,
    val topicCount: Int,
    val completedTopics: Int,
    val isCompleted: Boolean
) {
    val progressPercent: Int
        get() = if (topicCount > 0) (completedTopics * 100) / topicCount else 0
}

data class TopicSummary(
    val topicId: Int,
    val topicTitle: String,
    val headingLevel: Int,
    val displayOrder: Int,
    val isRead: Boolean,
    val parentTopicTitle: String?
) {
    val isSubTopic: Boolean get() = headingLevel >= 4
}

data class TopicDetails(
    val topicId: Int,
    val topicTitle: String,
    val headingLevel: Int,
    val fileName: String,
    val isRead: Boolean,
    val previousTopicId: Int?,
    val previousTopicTitle: String?,
    val previousFileName: String?,
    val nextTopicId: Int?,
    val nextTopicTitle: String?,
    val nextFileName: String?,
    val moduleId: Int,
    val moduleName: String,
    val lessonId: Int,
    val lessonTitle: String,
    val parentTopicTitle: String?
) {
    /** URL to fetch markdown content from the web server (not the API). */
    val markdownUrl: String
        get() = "https://vextrainer.com/content/lessons/$fileName.md"
}

data class LessonProgress(
    val totalTopics: Int,
    val completedTopics: Int,
    val percentComplete: Int
)

// ── DTO → Domain mappers ──────────────────────────────────────────────────────

fun ModuleDto.toDomain() = Module(
    moduleId         = moduleId,
    moduleName       = moduleName,
    displayOrder     = displayOrder,
    description      = description,
    lessonCount      = lessonCount,
    completedLessons = completedLessons
)

fun LessonSummaryDto.toDomain() = LessonSummary(
    lessonId        = lessonId,
    lessonTitle     = lessonTitle,
    displayOrder    = displayOrder,
    topicCount      = topicCount,
    completedTopics = completedTopics,
    isCompleted     = isCompleted
)

fun TopicSummaryDto.toDomain() = TopicSummary(
    topicId          = topicId,
    topicTitle       = topicTitle,
    headingLevel     = headingLevel,
    displayOrder     = displayOrder,
    isRead           = isRead,
    parentTopicTitle = parentTopicTitle
)

fun TopicDetailsDto.toDomain() = TopicDetails(
    topicId           = topicId,
    topicTitle        = topicTitle,
    headingLevel      = headingLevel,
    fileName          = fileName,
    isRead            = isRead,
    previousTopicId   = previousTopicId,
    previousTopicTitle = previousTopicTitle,
    previousFileName  = previousFileName,
    nextTopicId       = nextTopicId,
    nextTopicTitle    = nextTopicTitle,
    nextFileName      = nextFileName,
    moduleId          = moduleId,
    moduleName        = moduleName,
    lessonId          = lessonId,
    lessonTitle       = lessonTitle,
    parentTopicTitle  = parentTopicTitle
)

fun LessonProgressDto.toDomain() = LessonProgress(
    totalTopics     = totalTopics,
    completedTopics = completedTopics,
    percentComplete = percentComplete
)
