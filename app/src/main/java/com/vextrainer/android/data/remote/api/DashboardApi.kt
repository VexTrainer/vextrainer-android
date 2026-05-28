package com.vextrainer.android.data.remote.api

import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.dashboard.DashboardResponseDto
import retrofit2.http.GET

interface DashboardApi {
    @GET("Lesson/app-dashboard")
    suspend fun getDashboard(): ApiResponse<DashboardResponseDto>
}
