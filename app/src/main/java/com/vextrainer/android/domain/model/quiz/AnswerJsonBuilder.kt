package com.vextrainer.android.domain.model.quiz

/**
 * Builds the answerJson string passed to POST /Quiz/attempts/{id}/answer.
 *
 * Confirmed formats (from live API testing):
 *   SINGLE_ANSWER   → {"answer_id":1}
 *   TRUE_OR_FALSE   → {"answer_id":1}
 *   FILL_IN_BLANK   → {"text":"user typed answer"}
 *   MULTIPLE_ANSWER → {"answer_ids":[1,2,3]}
 *   MATCHING        → {"matches":[{"left":1,"right":2},...]}
 */
object AnswerJsonBuilder {

    /** Single choice, True/False, Fill-in-blank — all use the same single answerId format. */
    fun singleAnswer(answerId: Int): String =
        """{"answer_id":$answerId}"""

    fun fillInBlank(text: String): String =
        """{"text":"${text.trim()}"}"""

    fun multipleAnswers(answerIds: List<Int>): String {
        val idsJson = answerIds.sorted().joinToString(",", "[", "]")
        return """{"answer_ids":$idsJson}"""
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
        matchingPairs: List<Pair<Int, Int>> = emptyList(),
        fillInText: String = ""
    ): String = when {
        matchingPairs.isNotEmpty()                   -> matching(matchingPairs)
        questionType == QuestionType.FILL_IN_BLANK   -> fillInBlank(fillInText)
        questionType == QuestionType.MULTIPLE_ANSWER -> multipleAnswers(selectedIds)
        selectedIds.isNotEmpty()                     -> singleAnswer(selectedIds.first())
        else -> ""
    }
}
