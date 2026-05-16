package com.vextrainer.android.domain.model.quiz

/**
 * Builds the answerJson string passed to POST /Quiz/attempts/{id}/answer.
 *
 * Confirmed formats (from live API testing):
 *   SINGLE_ANSWER  → {"answerId":1}
 *   TRUE_OR_FALSE  → {"answerId":1}   (same — True and False are answer options)
 *   FILL_IN_BLANK  → {"answerId":1}   (answers presented as selectable options)
 *
 * Unconfirmed — TODO: test during Stage 4 before wiring QuizSessionScreen:
 *   MULTIPLE_ANSWER → format TBD (likely {"answerIds":[1,2,3]})
 *   MATCHING        → format TBD (matchSide L/R pairing needed)
 */
object AnswerJsonBuilder {

    /** Single choice, True/False, Fill-in-blank — all use the same single answerId format. */
    fun singleAnswer(answerId: Int): String =
        """{"answerId":$answerId}"""

    /**
     * Multiple choice — multiple answers.
     * Format is assumed — verify against live API in Stage 4 before shipping.
     */
    fun multipleAnswers(answerIds: List<Int>): String {
        val idsJson = answerIds.joinToString(",", "[", "]")
        return """{"answerIds":$idsJson}"""
    }

    /**
     * Matching questions (matchSide L paired with matchSide R).
     * Format is unconfirmed — verify against live API in Stage 4.
     * pairs = list of (leftAnswerId to rightAnswerId)
     */
    fun matching(pairs: List<Pair<Int, Int>>): String {
        val pairsJson = pairs.joinToString(",", "[", "]") {
            "{\"left\":${it.first},\"right\":${it.second}}"
        }
        return "{\"matches\":$pairsJson}"
    }

    /**
     * Convenience dispatcher — choose builder based on question type.
     * For MULTIPLE_ANSWER and MATCHING, selectedIds contains all chosen answer IDs.
     * For MATCHING, pairs must be built separately from left/right selections.
     */
    fun build(
        questionType: QuestionType,
        selectedIds: List<Int>,
        matchingPairs: List<Pair<Int, Int>> = emptyList()
    ): String = when {
        matchingPairs.isNotEmpty()                   -> matching(matchingPairs)
        questionType == QuestionType.MULTIPLE_ANSWER -> multipleAnswers(selectedIds)
        selectedIds.isNotEmpty()                     -> singleAnswer(selectedIds.first())
        else -> ""  // should not happen — guard against empty crash
    }
}
