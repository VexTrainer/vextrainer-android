package com.vextrainer.android.domain.usecase.auth

import com.vextrainer.android.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(identifier: String, password: String) =
        repo.login(identifier, password)
}

class RegisterUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(
        userName: String,
        email:    String,
        phone:    String?,
        password: String
    ) = repo.register(userName, email, phone, password)
}

class LogoutUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.logout()
}

class ForgotPasswordUseCase @Inject constructor(private val repo: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> =
        repo.forgotPassword(email)
}
