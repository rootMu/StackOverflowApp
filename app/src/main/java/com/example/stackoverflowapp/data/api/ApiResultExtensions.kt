package com.example.stackoverflowapp.data.api

/**
 * Converts an [ApiResult] into a standard Kotlin [Result].
 * 
 * Note: While we wrap in [Exception] for the standard Result, 
 * we could also return the raw [ApiResult.Error] if we wanted to preserve full typing.
 */
fun <T> ApiResult<T>.toResult(): Result<T> {
    return when (this) {
        is ApiResult.Success -> Result.success(data)
        is ApiResult.Error -> Result.failure(ApiResultException(this))
    }
}

class ApiResultException(val error: ApiResult.Error) : Exception(
    when (error) {
        is ApiResult.Error.Http -> "HTTP ${error.code}: ${error.message ?: "Request failed"}"
        is ApiResult.Error.EmptyBody -> "Empty response body"
        is ApiResult.Error.Network -> error.message
        is ApiResult.Error.Parse -> error.message
    }
)
