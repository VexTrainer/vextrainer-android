package com.vextrainer.android.data.repository

import com.vextrainer.android.data.remote.api.QuizApi
import com.vextrainer.android.data.remote.dto.quiz.SubmitAnswerRequestDto
import com.vextrainer.android.data.remote.util.safeApiCall
import com.vextrainer.android.domain.model.quiz.AnswerResult
import com.vextrainer.android.domain.model.quiz.CompleteQuizResult
import com.vextrainer.android.domain.model.quiz.QuizAttempt
import com.vextrainer.android.domain.model.quiz.CategoryPage
import com.vextrainer.android.domain.model.quiz.QuizCategory
import com.vextrainer.android.domain.model.quiz.QuizDetail
import com.vextrainer.android.domain.model.quiz.QuizHistory
import com.vextrainer.android.domain.model.quiz.QuizQuestionsData
import com.vextrainer.android.domain.model.quiz.QuizResults
import com.vextrainer.android.domain.model.quiz.QuizSummary
import com.vextrainer.android.domain.model.quiz.ResumeQuizData
import com.vextrainer.android.domain.model.quiz.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val quizApi: QuizApi
) {

    // Categories

    suspend fun getCategories(): Result<List<QuizCategory>> =
        safeApiCall { quizApi.getCategories() }.map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getCategoriesPaged(offset: Int, pageSize: Int): Result<CategoryPage> =
        safeApiCall { quizApi.getCategoriesPaged(offset, pageSize) }.map { it.toDomain() }



    suspend fun getQuizzesByCategory(categoryId: Int): Result<List<QuizSummary>> =
        safeApiCall { quizApi.getQuizzesByCategory(categoryId) }.map { list ->
            list.map { it.toDomain() }
        }

    // Quiz detail

    suspend fun getQuizDetail(quizId: Int): Result<QuizDetail> =
        safeApiCall { quizApi.getQuizDetail(quizId) }.map { it.toDomain() }

    // Attempt lifecycle

    suspend fun startQuiz(quizId: Int): Result<QuizAttempt> =
        safeApiCall { quizApi.startQuiz(quizId) }.map { it.toDomain() }

    suspend fun getQuestions(attemptId: Int): Result<QuizQuestionsData> =
        safeApiCall { quizApi.getQuestions(attemptId) }.map { it.toDomain() }

    /**
     * answerJson must be built using AnswerJsonBuilder — never pass a raw string directly.
     * Confirmed format for single answer: {"answerId":N}
     */
    suspend fun submitAnswer(
        attemptId: Int,
        questionId: Int,
        answerJson: String
    ): Result<AnswerResult> =
        safeApiCall {
            quizApi.submitAnswer(
                attemptId,
                SubmitAnswerRequestDto(questionId = questionId, answerJson = answerJson)
            )
        }.map { it.toDomain() }

    suspend fun completeQuiz(attemptId: Int): Result<CompleteQuizResult> =
        safeApiCall { quizApi.completeQuiz(attemptId) }.map { it.toDomain() }

    suspend fun getResults(attemptId: Int): Result<QuizResults> =
        safeApiCall { quizApi.getResults(attemptId) }.map { it.toDomain() }

    suspend fun resumeAttempt(attemptId: Int): Result<ResumeQuizData> =
        safeApiCall { quizApi.resumeAttempt(attemptId) }.map { it.toDomain() }

    // History

    suspend fun getHistory(page: Int = 1, limit: Int = 20): Result<QuizHistory> =
        safeApiCall { quizApi.getHistory(page, limit) }.map { it.toDomain() }
}
