package com.vextrainer.android.data.remote.dto.lesson

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModuleDto(
    val moduleId: Int,
    val moduleName: String,
    val displayOrder: Int,
    val description: String?,
    val lessonCount: Int = 0,        // returned after backend fix, defaults to 0 until then
    val completedLessons: Int = 0    // returned after backend fix, defaults to 0 until then
)

@JsonClass(generateAdapter = true)
data class LessonSummaryDto(
    val lessonId: Int,
    val lessonTitle: String,
    val displayOrder: Int,
    val topicCount: Int,
    val completedTopics: Int,
    val isCompleted: Boolean
)

@JsonClass(generateAdapter = true)
data class TopicSummaryDto(
    val topicId: Int,
    val topicTitle: String,
    val headingLevel: Int,
    val displayOrder: Int,
    val isRead: Boolean,
    val parentTopicTitle: String?
)

/**
 * TopicDetails — fully defined in Swagger.
 * headingLevel: 3 = H3 (main topic), 4 = H4 (sub-topic)
 * fileName: e.g. "00100-00025-00040" — used to fetch markdown from
 *   https://www.vextrainer.com/lessons/{fileName}.md
 */
@JsonClass(generateAdapter = true)
data class TopicDetailsDto(
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
)

@JsonClass(generateAdapter = true)
data class MarkReadResponseDto(
    val hasNextTopic: Boolean,
    val nextTopicUrl: String?
)

@JsonClass(generateAdapter = true)
data class LessonProgressDto(
    val totalTopics: Int,
    val completedTopics: Int,
    val percentComplete: Int,
    val recentlyRead: List<RecentlyReadDto>
)

@JsonClass(generateAdapter = true)
data class RecentlyReadDto(
    val topicId: Int,
    val topicTitle: String,
    val readDate: String
)
