package com.vextrainer.android.domain.usecase.lesson

import com.vextrainer.android.data.repository.LessonRepository
import com.vextrainer.android.domain.model.lesson.LessonProgress
import com.vextrainer.android.domain.model.lesson.LessonSummary
import com.vextrainer.android.domain.model.lesson.Module
import com.vextrainer.android.domain.model.lesson.TopicDetails
import com.vextrainer.android.domain.model.lesson.TopicSummary
import javax.inject.Inject

class GetModulesUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(): Result<List<Module>> = repo.getModules()
}

class GetLessonsByModuleUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(moduleId: Int): Result<List<LessonSummary>> =
        repo.getLessonsByModule(moduleId)
}

class GetTopicsByLessonUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(lessonId: Int): Result<List<TopicSummary>> =
        repo.getTopicsByLesson(lessonId)
}

class GetTopicDetailsUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(topicId: Int): Result<TopicDetails> =
        repo.getTopicDetails(topicId)
}

class MarkTopicReadUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(topicId: Int): Result<Unit> = repo.markTopicRead(topicId)
}

class MarkLessonReadUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(lessonId: Int): Result<Unit> = repo.markLessonRead(lessonId)
}

class FetchMarkdownUseCase @Inject constructor(private val repo: LessonRepository) {
    suspend operator fun invoke(url: String): Result<String> = repo.fetchMarkdownContent(url)
}
