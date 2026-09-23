package com.lucilab.surveynext.data.model

data class StatusResponseModel<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

class ApiException(message: String) : Exception(message)

/** Unwraps [StatusResponseModel.data], throwing the backend's message on failure. */
fun <T> StatusResponseModel<T>.requireData(fallbackError: String): T {
    if (success && data != null) return data
    throw ApiException(message ?: fallbackError)
}

/** For endpoints that answer with a message and no data. */
fun StatusResponseModel<*>.requireSuccess(fallbackError: String) {
    if (!success) throw ApiException(message ?: fallbackError)
}
