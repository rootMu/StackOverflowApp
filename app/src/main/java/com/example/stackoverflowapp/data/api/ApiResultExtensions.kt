package com.example.stackoverflowapp.data.api

import com.example.stackoverflowapp.domain.model.AppError
import com.example.stackoverflowapp.domain.model.AppErrorException

/**
 * Converts an [ApiResult] into a standard Kotlin [Result].
 * Maps [ApiResult.Error] to domain-level [AppError] wrapped in [AppErrorException].
 */
fun <T> ApiResult<T>.toResult(): Result<T> {
    return when (this) {
        is ApiResult.Success -> Result.success(data)
        is ApiResult.Error -> Result.failure(AppErrorException(this.toAppError()))
    }
}

/**
 * Maps [ApiResult.Error] to domain-level [AppError].
 */
fun ApiResult.Error.toAppError(): AppError {
    return when (this) {
        is ApiResult.Error.Http -> {
            when (code) {
                401, 403 -> AppError.Network.Unauthorized
                in 500..599 -> AppError.Network.ServerError
                else -> AppError.Network.ServerError
            }
        }
        is ApiResult.Error.Network -> AppError.Network.NoConnection
        is ApiResult.Error.Parse -> AppError.Data.MalformedResponse
        is ApiResult.Error.EmptyBody -> AppError.Data.NotFound
    }
}
