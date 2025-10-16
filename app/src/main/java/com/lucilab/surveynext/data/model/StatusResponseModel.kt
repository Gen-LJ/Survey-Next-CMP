package com.lucilab.surveynext.data.model

data class StatusResponseModel<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)