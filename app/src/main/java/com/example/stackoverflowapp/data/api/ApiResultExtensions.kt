package com.example.stackoverflowapp.data.api

/**
 * Converts an [ApiResult] into a standard Kotlin [Result].
 */
fun <T> ApiResult<T>.toResult(): Result<T> {
    return when (this) {
        is ApiResult.Success -> Result.success(data)
        is ApiResult.Error.Http -> failure("HTTP $code: ${message ?: "Request failed"}")
        is ApiResult.Error.EmptyBody -> failure("Empty response body")
        is ApiResult.Error.Network -> failure(message)
        is ApiResult.Error.Parse -> failure(message)
    }
}

private fun failure(message: String): Result<Nothing> = Result.failure(Exception(message))
