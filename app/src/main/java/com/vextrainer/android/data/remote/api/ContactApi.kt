package com.vextrainer.android.data.remote.api

import com.vextrainer.android.data.remote.dto.ApiResponse
import com.vextrainer.android.data.remote.dto.contact.ContactRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ContactApi {

    @POST("Contact")
    suspend fun submitContact(
        @Body request: ContactRequestDto
    ): ApiResponse<Any?>
}
