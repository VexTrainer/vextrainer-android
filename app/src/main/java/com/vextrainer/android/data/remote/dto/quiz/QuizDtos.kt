package com.vextrainer.android.data.remote.dto.quiz

import com.squareup.moshi.JsonClass

// ── Categories ────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CategoryDto(
    val categoryId: Int,
    val parentCategoryId: Int?,
    val categoryName: String,
    val description: String?,
    val displayOrder: Int,
    val subcategories: List<CategoryDto>?
)

// ── Quiz list & detail ────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class QuizSummaryDto(
    val quizId: Int,
    val quizTitle: String,
    val quizDescription: String?,
    val totalQuestions: Int,
    val passingScore: Double?,
    val displayOrder: Int,
    val userAttempts: Int,
    val userBestScore: Double?,
    val isCompleted: Boolean
)

@JsonClass(generateAdapter = true)
data class QuizDetailDto(
    val quizId: Int,
    val quizTitle: String,
    val quizDescription: String?,
    val totalQuestions: Int,
    val passingScore: Double?,
    val categoryName: String,
    val userAttempts: Int,
    val userBestScore: Double?
)

// ── Start quiz ────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class StartQuizResponseDto(
    val attemptId: Int,
    val quizId: Int,
    val startedDate: String,
    val totalQuestions: Int
)

// ── Questions ─────────────────────────────────────────────────────────────────

/**
 * Nested inside QuizQuestionsResponseDto.
 * matchSide is "L" or "R" for matching questions, null for all other types.
 */
@JsonClass(generateAdapter = true)
data class AnswerDto(
    val answerId: Int,
    val questionId: Int,
    val answerText: String,
    val answerImagePath: String?,
    val matchSide: String?
)

/**
 * questionTypeId values (from sp_GetQuizQuestions):
 *   1 = Multiple choice — single answer
 *   2 = Multiple choice — multiple answers
 *   3 = Fill in the blank (True/False answers presented as options)
 *   4 = True or False / Matching (confirm with live data)
 */
@JsonClass(generateAdapter = true)
data class QuestionDto(
    val questionId: Int,
    val questionTypeId: Int,
    val questionType: String,
    val questionText: String,
    val questionImagePath: String?,
    val pointValue: Double,
    val answers: List<AnswerDto>
)

/**
 * GET /Quiz/attempts/{id}/questions
 * Wrapper confirmed from live API — data is NOT a flat list, it's this object.
 */
@JsonClass(generateAdapter = true)
data class QuizQuestionsResponseDto(
    val attemptId: Int,
    val questions: List<QuestionDto>
)

// ── Submit answer ─────────────────────────────────────────────────────────────

/**
 * POST /Quiz/attempts/{id}/answer
 *
 * answerJson format (confirmed from live API testing):
 *   Single answer:    {"answerId":1}
 *   Multiple answers: {"answerIds":[1,2,3]}   ← format NOT yet confirmed — test in Stage 4
 *   Matching:         format NOT yet confirmed — test in Stage 4
 *
 * Use AnswerJsonBuilder to construct answerJson — never build it inline.
 */
@JsonClass(generateAdapter = true)
data class SubmitAnswerRequestDto(
    val questionId: Int,
    val answerJson: String
)

/**
 * correctAnswerJson is a raw JSON string using snake_case keys e.g. {"answer_id":1}
 * Parse it only when needed for display — store as String in the domain model.
 */
@JsonClass(generateAdapter = true)
data class SubmitAnswerResponseDto(
    val isCorrect: Boolean,
    val explanation: String?,
    val correctAnswerJson: String?,
    val currentScore: Double,
    val questionsAnswered: Int
)

// ── Complete quiz ─────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CompleteQuizResponseDto(
    val attemptId: Int,
    val finalScore: Double,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val passingScore: Double?,
    val passed: Boolean,
    val completedDate: String
)

// ── Results ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class QuizResultSummaryDto(
    val attemptId: Int,
    val quizTitle: String,
    val startedDate: String,
    val completedDate: String?,
    val score: Double?,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val passingScore: Double?,
    val passed: Boolean
)

/**
 * correctAnswers is a raw JSON string (same snake_case format as correctAnswerJson above).
 */
@JsonClass(generateAdapter = true)
data class QuestionResultDto(
    val questionId: Int,
    val questionText: String,
    val questionImagePath: String?,
    val questionType: String,
    val userAnswerJson: String?,
    val isCorrect: Boolean,
    val explanation: String?,
    val correctAnswers: String?
)

@JsonClass(generateAdapter = true)
data class QuizResultsDto(
    val summary: QuizResultSummaryDto,
    val questions: List<QuestionResultDto>
)

// ── Resume ────────────────────────────────────────────────────────────────────

/**
 * GET /Quiz/attempts/{id}/resume
 * answeredQuestionIds lets QuizSessionViewModel skip already-answered questions.
 */
@JsonClass(generateAdapter = true)
data class ResumeQuizDataDto(
    val attemptId: Int,
    val quizId: Int,
    val startedDate: String,
    val lastQuestionId: Int?,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val score: Double?,
    val answeredQuestionIds: List<Int>
)

// ── History ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class QuizHistoryItemDto(
    val attemptId: Int,
    val quizId: Int,
    val quizTitle: String,
    val categoryName: String,
    val startedDate: String,
    val completedDate: String?,
    val score: Double?,
    val isCompleted: Boolean
)

@JsonClass(generateAdapter = true)
data class QuizHistoryResponseDto(
    val attempts: List<QuizHistoryItemDto>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int
)
