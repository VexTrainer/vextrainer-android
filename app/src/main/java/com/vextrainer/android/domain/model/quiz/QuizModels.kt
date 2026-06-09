package com.vextrainer.android.domain.model.quiz

import com.vextrainer.android.data.remote.dto.quiz.AnswerDto
import com.vextrainer.android.data.remote.dto.quiz.CategoryDto
import com.vextrainer.android.data.remote.dto.quiz.CategoryPageDto
import com.vextrainer.android.data.remote.dto.quiz.CompleteQuizResponseDto
import com.vextrainer.android.data.remote.dto.quiz.QuestionDto
import com.vextrainer.android.data.remote.dto.quiz.QuestionResultDto
import com.vextrainer.android.data.remote.dto.quiz.QuizDetailDto
import com.vextrainer.android.data.remote.dto.quiz.QuizHistoryItemDto
import com.vextrainer.android.data.remote.dto.quiz.QuizHistoryResponseDto
import com.vextrainer.android.data.remote.dto.quiz.QuizQuestionsResponseDto
import com.vextrainer.android.data.remote.dto.quiz.QuizResultSummaryDto
import com.vextrainer.android.data.remote.dto.quiz.QuizResultsDto
import com.vextrainer.android.data.remote.dto.quiz.QuizSummaryDto
import com.vextrainer.android.data.remote.dto.quiz.ResumeQuizDataDto
import com.vextrainer.android.data.remote.dto.quiz.StartQuizResponseDto
import com.vextrainer.android.data.remote.dto.quiz.SubmitAnswerResponseDto

// Question type enum

enum class QuestionType(val typeId: Int) {
    SINGLE_ANSWER(1),
    MULTIPLE_ANSWER(2),
    FILL_IN_BLANK(3),
    TRUE_OR_FALSE(4),
    UNKNOWN(-1);

    companion object {
        fun fromId(id: Int) = entries.find { it.typeId == id } ?: UNKNOWN
    }
}

// Domain models

data class QuizCategory(
    val categoryId: Int,
    val parentCategoryId: Int?,
    val categoryName: String,
    // val description: String?,
    val displayOrder: Int,
    val subcategories: List<QuizCategory>
)

data class QuizSummary(
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

data class QuizDetail(
    val quizId: Int,
    val quizTitle: String,
    val quizDescription: String?,
    val totalQuestions: Int,
    val passingScore: Double?,
    val categoryName: String,
    val userAttempts: Int,
    val userBestScore: Double?
)

data class QuizAttempt(
    val attemptId: Int,
    val quizId: Int,
    val startedDate: String,
    val totalQuestions: Int
)

data class QuizAnswer(
    val answerId: Int,
    val questionId: Int,
    val answerText: String,
    val answerImagePath: String?,
    val matchSide: String?
)

data class QuizQuestion(
    val questionId: Int,
    val questionTypeId: Int,
    val questionType: QuestionType,
    val questionTypeLabel: String,
    val questionText: String,
    val questionImagePath: String?,
    val pointValue: Double,
    val answers: List<QuizAnswer>
)

data class QuizQuestionsData(
    val attemptId: Int,
    val questions: List<QuizQuestion>
)

data class AnswerResult(
    val isCorrect: Boolean,
    val explanation: String?,
    val correctAnswerJson: String?,
    val currentScore: Double,
    val questionsAnswered: Int
)

data class CompleteQuizResult(
    val attemptId: Int,
    val finalScore: Double,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val passingScore: Double?,
    val passed: Boolean,
    val completedDate: String
)

data class QuizResultSummary(
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

data class QuestionResult(
    val questionId: Int,
    val questionText: String,
    val questionType: String,
    val userAnswerJson: String?,
    val isCorrect: Boolean,
    val explanation: String?,
    val correctAnswers: String?
)

data class QuizResults(
    val summary: QuizResultSummary,
    val questions: List<QuestionResult>
)

data class ResumeQuizData(
    val attemptId: Int,
    val quizId: Int,
    val startedDate: String,
    val lastQuestionId: Int?,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val score: Double?,
    val answeredQuestionIds: List<Int>
)

data class QuizHistoryItem(
    val attemptId: Int,
    val quizId: Int,
    val quizTitle: String,
    val categoryName: String,
    val startedDate: String,
    val completedDate: String?,
    val score: Double?,
    val isCompleted: Boolean
)

data class QuizHistory(
    val attempts: List<QuizHistoryItem>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int
)

// DTO → Domain mappers

fun CategoryDto.toDomain(): QuizCategory = QuizCategory(
    categoryId      = categoryId,
    parentCategoryId = parentCategoryId,
    categoryName    = categoryName,
    // description     = description,
    displayOrder    = displayOrder,
    subcategories   = subcategories?.map { it.toDomain() } ?: emptyList()
)

fun QuizSummaryDto.toDomain(): QuizSummary = QuizSummary(
    quizId          = quizId,
    quizTitle       = quizTitle,
    quizDescription = quizDescription,
    totalQuestions  = totalQuestions,
    passingScore    = passingScore,
    displayOrder    = displayOrder,
    userAttempts    = userAttempts,
    userBestScore   = userBestScore,
    isCompleted     = isCompleted
)

fun QuizDetailDto.toDomain(): QuizDetail = QuizDetail(
    quizId          = quizId,
    quizTitle       = quizTitle,
    quizDescription = quizDescription,
    totalQuestions  = totalQuestions,
    passingScore    = passingScore,
    categoryName    = categoryName,
    userAttempts    = userAttempts,
    userBestScore   = userBestScore
)

fun StartQuizResponseDto.toDomain(): QuizAttempt = QuizAttempt(
    attemptId      = attemptId,
    quizId         = quizId,
    startedDate    = startedDate,
    totalQuestions = totalQuestions
)

fun AnswerDto.toDomain(): QuizAnswer = QuizAnswer(
    answerId        = answerId,
    questionId      = questionId,
    answerText      = answerText,
    answerImagePath = answerImagePath,
    matchSide       = matchSide
)

fun QuestionDto.toDomain(): QuizQuestion = QuizQuestion(
    questionId      = questionId,
    questionTypeId  = questionTypeId,
    questionType    = QuestionType.fromId(questionTypeId),
    questionTypeLabel = questionType,
    questionText    = questionText,
    questionImagePath = questionImagePath,
    pointValue      = pointValue,
    answers         = answers.map { it.toDomain() }
)

fun QuizQuestionsResponseDto.toDomain(): QuizQuestionsData = QuizQuestionsData(
    attemptId = attemptId,
    questions = questions.map { it.toDomain() }
)

fun SubmitAnswerResponseDto.toDomain(): AnswerResult = AnswerResult(
    isCorrect        = isCorrect,
    explanation      = explanation,
    correctAnswerJson = correctAnswerJson,
    currentScore     = currentScore,
    questionsAnswered = questionsAnswered
)

fun CompleteQuizResponseDto.toDomain(): CompleteQuizResult = CompleteQuizResult(
    attemptId      = attemptId,
    finalScore     = finalScore,
    correctAnswers = correctAnswers,
    totalQuestions = totalQuestions,
    passingScore   = passingScore,
    passed         = passed,
    completedDate  = completedDate
)

fun QuizResultSummaryDto.toDomain(): QuizResultSummary = QuizResultSummary(
    attemptId      = attemptId,
    quizTitle      = quizTitle,
    startedDate    = startedDate,
    completedDate  = completedDate,
    score          = score,
    correctAnswers = correctAnswers,
    totalQuestions = totalQuestions,
    passingScore   = passingScore,
    passed         = passed
)

fun QuestionResultDto.toDomain(): QuestionResult = QuestionResult(
    questionId      = questionId,
    questionText    = questionText,
    questionType    = questionType,
    userAnswerJson  = userAnswerJson,
    isCorrect       = isCorrect,
    explanation     = explanation,
    correctAnswers  = correctAnswers
)

fun QuizResultsDto.toDomain(): QuizResults = QuizResults(
    summary   = summary.toDomain(),
    questions = questions.map { it.toDomain() }
)

fun ResumeQuizDataDto.toDomain(): ResumeQuizData = ResumeQuizData(
    attemptId          = attemptId,
    quizId             = quizId,
    startedDate        = startedDate,
    lastQuestionId     = lastQuestionId,
    correctAnswers     = correctAnswers,
    totalQuestions     = totalQuestions,
    score              = score,
    answeredQuestionIds = answeredQuestionIds
)

fun QuizHistoryItemDto.toDomain(): QuizHistoryItem = QuizHistoryItem(
    attemptId     = attemptId,
    quizId        = quizId,
    quizTitle     = quizTitle,
    categoryName  = categoryName,
    startedDate   = startedDate,
    completedDate = completedDate,
    score         = score,
    isCompleted   = isCompleted
)

fun QuizHistoryResponseDto.toDomain(): QuizHistory = QuizHistory(
    attempts   = attempts.map { it.toDomain() },
    totalCount = totalCount,
    page       = page,
    pageSize   = pageSize
)

/// Domain model for a paged category response
data class CategoryPage(
    val categories: List<QuizCategory>,
    val hasMore:    Boolean
)

/// Maps CategoryPageDto → CategoryPage
fun CategoryPageDto.toDomain() = CategoryPage(
    categories = categories.map { it.toDomain() },
    hasMore    = hasMore
)
