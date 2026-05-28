package com.vextrainer.android.domain.usecase.dashboard

import com.vextrainer.android.data.repository.DashboardRepository
import com.vextrainer.android.domain.model.dashboard.Dashboard
import javax.inject.Inject

class GetDashboardUseCase @Inject constructor(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(): Result<Dashboard> = repository.getDashboard()
}
