package com.vextrainer.android.data.remote.dto.contact

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContactRequestDto(
    val category: String,
    val message:  String
)
