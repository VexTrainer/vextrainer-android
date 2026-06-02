package com.vextrainer.android.domain.usecase.lesson

import com.vextrainer.android.data.repository.LessonRepository
import com.vextrainer.android.domain.model.lesson.StreakBadgeReport
import javax.inject.Inject

class GetStreakBadgeReportUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    suspend operator fun invoke(timezoneOffsetMinutes: Int): Result<StreakBadgeReport> =
        lessonRepository.getStreakBadgeReport(timezoneOffsetMinutes)
}
