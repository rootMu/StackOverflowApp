package com.example.stackoverflowapp.data.api

/**
 * Converts an [ApiResult] into a standard Kotlin [Result].
 * Centralizes error message formatting to reduce duplication in repositories.
 */
fun <T, R> ApiResult<T>.toResult(transform: (T) -> R): Result<R> {
    return when (this) {
        is ApiResult.Success -> Result.success(transform(data))
        is ApiResult.Error.Http -> Result.failure(Exception("HTTP $code: ${message ?: "Request failed"}"))
        is ApiResult.Error.Network -> Result.failure(Exception(message))
        is ApiResult.Error.Parse -> Result.failure(Exception("Parse error: $message"))
        is ApiResult.Error.EmptyBody -> Result.failure(Exception("Empty response body"))
    }
}
