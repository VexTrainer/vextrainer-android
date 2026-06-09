package com.vextrainer.android.data.remote.api

import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.quiz.CategoryDto
import com.vextrainer.android.data.remote.dto.quiz.CategoryPageDto
import com.vextrainer.android.data.remote.dto.quiz.CompleteQuizResponseDto
import com.vextrainer.android.data.remote.dto.quiz.QuizDetailDto
import com.vextrainer.android.data.remote.dto.quiz.QuizHistoryResponseDto
import com.vextrainer.android.data.remote.dto.quiz.QuizQuestionsResponseDto
import com.vextrainer.android.data.remote.dto.quiz.QuizResultsDto
import com.vextrainer.android.data.remote.dto.quiz.QuizSummaryDto
import com.vextrainer.android.data.remote.dto.quiz.ResumeQuizDataDto
import com.vextrainer.android.data.remote.dto.quiz.StartQuizResponseDto
import com.vextrainer.android.data.remote.dto.quiz.SubmitAnswerRequestDto
import com.vextrainer.android.data.remote.dto.quiz.SubmitAnswerResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizApi {

    // Categories

    @GET("Quiz/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @GET("Quiz/categories/paged")
    suspend fun getCategoriesPaged(
        @Query("offset")   offset:   Int,
        @Query("pageSize") pageSize: Int
    ): ApiResponse<CategoryPageDto>



    @GET("Quiz/categories/{categoryId}/quizzes")
    suspend fun getQuizzesByCategory(
        @Path("categoryId") categoryId: Int
    ): ApiResponse<List<QuizSummaryDto>>

    // Quiz detail

    @GET("Quiz/quizzes/{quizId}")
    suspend fun getQuizDetail(
        @Path("quizId") quizId: Int
    ): ApiResponse<QuizDetailDto>

    // Attempt lifecycle

    @POST("Quiz/quizzes/{quizId}/start")
    suspend fun startQuiz(
        @Path("quizId") quizId: Int
    ): ApiResponse<StartQuizResponseDto>

    @GET("Quiz/attempts/{attemptId}/questions")
    suspend fun getQuestions(
        @Path("attemptId") attemptId: Int
    ): ApiResponse<QuizQuestionsResponseDto>

    @POST("Quiz/attempts/{attemptId}/answer")
    suspend fun submitAnswer(
        @Path("attemptId") attemptId: Int,
        @Body request: SubmitAnswerRequestDto
    ): ApiResponse<SubmitAnswerResponseDto>

    @POST("Quiz/attempts/{attemptId}/complete")
    suspend fun completeQuiz(
        @Path("attemptId") attemptId: Int
    ): ApiResponse<CompleteQuizResponseDto>

    @GET("Quiz/attempts/{attemptId}/results")
    suspend fun getResults(
        @Path("attemptId") attemptId: Int
    ): ApiResponse<QuizResultsDto>

    @GET("Quiz/attempts/{attemptId}/resume")
    suspend fun resumeAttempt(
        @Path("attemptId") attemptId: Int
    ): ApiResponse<ResumeQuizDataDto>

    // History

    /** page and limit match the query params in sp_GetUserQuizHistory */
    @GET("User/history")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<QuizHistoryResponseDto>
}
