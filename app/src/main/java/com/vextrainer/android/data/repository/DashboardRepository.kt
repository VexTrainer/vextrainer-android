package com.vextrainer.android.data.repository

import com.vextrainer.android.data.remote.api.DashboardApi
import com.vextrainer.android.domain.model.dashboard.Dashboard
import com.vextrainer.android.domain.model.dashboard.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val dashboardApi: DashboardApi
) {
    suspend fun getDashboard(): Result<Dashboard> = safeCall {
        val response = dashboardApi.getDashboard()
        if (response.success && response.data != null)
            Result.success(response.data.toDomain())
        else
            Result.failure(Exception(response.message))
    }

    private inline fun <T> safeCall(block: () -> Result<T>): Result<T> = try {
        block()
    } catch (e: Exception) {
        Result.failure(e)
    }
}
