package com.vextrainer.android.data.repository

import com.vextrainer.android.data.remote.api.LessonApi
import com.vextrainer.android.data.remote.util.safeApiCall
import com.vextrainer.android.data.remote.util.safeApiCallUnit
import com.vextrainer.android.domain.model.lesson.LessonProgress
import com.vextrainer.android.domain.model.lesson.LessonSummary
import com.vextrainer.android.domain.model.lesson.Module
import com.vextrainer.android.domain.model.lesson.TopicDetails
import com.vextrainer.android.domain.model.lesson.TopicSummary
import com.vextrainer.android.domain.model.lesson.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepository @Inject constructor(
    private val lessonApi: LessonApi
) {
    suspend fun getModules(): Result<List<Module>> =
        safeApiCall { lessonApi.getModules() }.map { it.map { dto -> dto.toDomain() } }

    suspend fun getLessonsByModule(moduleId: Int): Result<List<LessonSummary>> =
        safeApiCall { lessonApi.getLessonsByModule(moduleId) }.map { it.map { dto -> dto.toDomain() } }

    suspend fun getTopicsByLesson(lessonId: Int): Result<List<TopicSummary>> =
        safeApiCall { lessonApi.getTopicsByLesson(lessonId) }.map { it.map { dto -> dto.toDomain() } }

    suspend fun getTopicDetails(topicId: Int): Result<TopicDetails> =
        safeApiCall { lessonApi.getTopicDetails(topicId) }.map { it.toDomain() }

    suspend fun markTopicRead(topicId: Int): Result<Unit> =
        safeApiCallUnit { lessonApi.markTopicRead(topicId) }

    suspend fun markLessonRead(lessonId: Int): Result<Unit> =
        safeApiCallUnit { lessonApi.markLessonRead(lessonId) }

    suspend fun getProgress(): Result<LessonProgress> =
        safeApiCall { lessonApi.getProgress() }.map { it.toDomain() }

    /**
     * Fetches raw markdown content from the web server (not the API).
     * Uses a plain OkHttp call because this is a static file, not a JSON endpoint.
     */
    suspend fun fetchMarkdownContent(url: String): Result<String> {
        return try {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "")
            } else {
                Result.failure(Exception("HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
