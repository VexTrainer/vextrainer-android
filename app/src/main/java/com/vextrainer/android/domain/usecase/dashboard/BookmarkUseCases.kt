package com.vextrainer.android.domain.usecase.dashboard

import com.vextrainer.android.data.repository.DashboardRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(topicId: Int): Result<Unit> = repository.addBookmark(topicId)
}

class DeleteBookmarkUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(topicId: Int): Result<Unit> = repository.deleteBookmark(topicId)
}
