package com.vextrainer.android.domain.usecase.contact

import com.vextrainer.android.data.repository.ContactRepository
import javax.inject.Inject

class SendContactMessageUseCase @Inject constructor(
    private val repo: ContactRepository
) {
    suspend operator fun invoke(category: String, message: String): Result<Unit> =
        repo.sendMessage(category, message)
}
