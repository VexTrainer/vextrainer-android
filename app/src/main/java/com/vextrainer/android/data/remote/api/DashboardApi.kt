package com.vextrainer.android.data.remote.api

import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.dashboard.DashboardResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DashboardApi {

    @GET("Lesson/app-dashboard")
    suspend fun getDashboard(): ApiResponse<DashboardResponseDto>

    // Response<Unit> avoids Moshi trying to deserialize the null data field.
    // Retrofit treats any 2xx as success without parsing the body.
    @POST("Lesson/topics/{topicId}/bookmark")
    suspend fun addBookmark(@Path("topicId") topicId: Int): Response<Unit>

    @DELETE("Lesson/topics/{topicId}/bookmark")
    suspend fun deleteBookmark(@Path("topicId") topicId: Int): Response<Unit>
}
