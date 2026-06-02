package com.vextrainer.android.data.remote.api

import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.lesson.LessonProgressDto
import com.vextrainer.android.data.remote.dto.lesson.LessonSummaryDto
import com.vextrainer.android.data.remote.dto.lesson.MarkReadResponseDto
import com.vextrainer.android.data.remote.dto.lesson.ModuleDto
import com.vextrainer.android.data.remote.dto.lesson.TopicDetailsDto
import com.vextrainer.android.data.remote.dto.lesson.StreakBadgeReportDto
import com.vextrainer.android.data.remote.dto.lesson.TopicSummaryDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LessonApi {

    @GET("Lesson/modules")
    suspend fun getModules(): ApiResponse<List<ModuleDto>>

    @GET("Lesson/modules/{moduleId}/lessons")
    suspend fun getLessonsByModule(
        @Path("moduleId") moduleId: Int
    ): ApiResponse<List<LessonSummaryDto>>

    @GET("Lesson/lessons/{lessonId}/topics")
    suspend fun getTopicsByLesson(
        @Path("lessonId") lessonId: Int
    ): ApiResponse<List<TopicSummaryDto>>

    @GET("Lesson/topics/{topicId}/details")
    suspend fun getTopicDetails(
        @Path("topicId") topicId: Int
    ): ApiResponse<TopicDetailsDto>

    @POST("Lesson/topics/{topicId}/mark-read")
    suspend fun markTopicRead(
        @Path("topicId") topicId: Int
    ): ApiResponse<MarkReadResponseDto>

    @POST("Lesson/lessons/{lessonId}/mark-read")
    suspend fun markLessonRead(
        @Path("lessonId") lessonId: Int
    ): ApiResponse<Any?>

    @GET("Lesson/progress")
    suspend fun getProgress(): ApiResponse<LessonProgressDto>

    @GET("Lesson/streak-report")
    suspend fun getStreakBadgeReport(
        @Query("timezoneOffsetMinutes") timezoneOffsetMinutes: Int
    ): ApiResponse<StreakBadgeReportDto>
}
