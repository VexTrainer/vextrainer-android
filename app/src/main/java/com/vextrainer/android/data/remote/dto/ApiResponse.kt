package com.vextrainer.android.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String,
    val resultCode: Int
)
