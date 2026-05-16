package com.vextrainer.android.data.repository

import com.vextrainer.android.data.remote.api.ContactApi
import com.vextrainer.android.data.remote.dto.contact.ContactRequestDto
import com.vextrainer.android.data.remote.util.safeApiCallUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactApi: ContactApi
) {
    suspend fun sendMessage(category: String, message: String): Result<Unit> =
        safeApiCallUnit {
            contactApi.submitContact(
                ContactRequestDto(category = category, message = message)
            )
        }
}
