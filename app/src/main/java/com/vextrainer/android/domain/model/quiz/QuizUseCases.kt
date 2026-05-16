package com.vextrainer.android.domain.usecase.quiz

import com.vextrainer.android.data.repository.QuizRepository
import com.vextrainer.android.domain.model.quiz.AnswerJsonBuilder
import com.vextrainer.android.domain.model.quiz.AnswerResult
import com.vextrainer.android.domain.model.quiz.CompleteQuizResult
import com.vextrainer.android.domain.model.quiz.QuestionType
import com.vextrainer.android.domain.model.quiz.QuizAttempt
import com.vextrainer.android.domain.model.quiz.QuizCategory
import com.vextrainer.android.domain.model.quiz.QuizDetail
import com.vextrainer.android.domain.model.quiz.QuizHistory
import com.vextrainer.android.domain.model.quiz.QuizQuestionsData
import com.vextrainer.android.domain.model.quiz.QuizResults
import com.vextrainer.android.domain.model.quiz.QuizSummary
import com.vextrainer.android.domain.model.quiz.ResumeQuizData
import javax.inject.Inject

// ── 1. Get categories ─────────────────────────────────────────────────────────

class GetQuizCategoriesUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(): Result<List<QuizCategory>> =
        repo.getCategories()
}

// ── 2. Get quizzes by category ────────────────────────────────────────────────

class GetQuizzesByCategoryUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(categoryId: Int): Result<List<QuizSummary>> =
        repo.getQuizzesByCategory(categoryId)
}

// ── 3. Get quiz detail ────────────────────────────────────────────────────────

class GetQuizDetailUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): Result<QuizDetail> =
        repo.getQuizDetail(quizId)
}

// ── 4. Start quiz ─────────────────────────────────────────────────────────────

class StartQuizUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): Result<QuizAttempt> =
        repo.startQuiz(quizId)
}

// ── 5. Get questions ──────────────────────────────────────────────────────────

class GetQuestionsUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(attemptId: Int): Result<QuizQuestionsData> =
        repo.getQuestions(attemptId)
}

// ── 6. Submit answer ──────────────────────────────────────────────────────────

/**
 * Accepts selected answer IDs and builds the correct answerJson via AnswerJsonBuilder.
 * ViewModels never construct answerJson strings directly — always go through this use case.
 *
 * @param matchingPairs Only required for MATCHING type. Each pair is (leftId, rightId).
 */
class SubmitAnswerUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(
        attemptId: Int,
        questionId: Int,
        questionType: QuestionType,
        selectedIds: List<Int>,
        matchingPairs: List<Pair<Int, Int>> = emptyList()
    ): Result<AnswerResult> {
        val answerJson = AnswerJsonBuilder.build(questionType, selectedIds, matchingPairs)
        return repo.submitAnswer(attemptId, questionId, answerJson)
    }
}

// ── 7. Complete quiz ──────────────────────────────────────────────────────────

class CompleteQuizUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(attemptId: Int): Result<CompleteQuizResult> =
        repo.completeQuiz(attemptId)
}

// ── 8. Get results ────────────────────────────────────────────────────────────

class GetQuizResultsUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(attemptId: Int): Result<QuizResults> =
        repo.getResults(attemptId)
}

// ── 9. Resume attempt ─────────────────────────────────────────────────────────

class ResumeQuizAttemptUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(attemptId: Int): Result<ResumeQuizData> =
        repo.resumeAttempt(attemptId)
}

// ── 10. Get history ───────────────────────────────────────────────────────────

class GetQuizHistoryUseCase @Inject constructor(
    private val repo: QuizRepository
) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 20): Result<QuizHistory> =
        repo.getHistory(page, limit)
}
