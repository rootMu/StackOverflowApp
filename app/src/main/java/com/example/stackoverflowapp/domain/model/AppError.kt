package com.example.stackoverflowapp.domain.model

/**
 * Represents a hierarchy of errors that the application can handle gracefully.
 * This separates technical exceptions from user-facing failure concepts.
 */
sealed interface AppError {
    /** Errors related to network connectivity or HTTP protocols. */
    sealed interface Network : AppError {
        data object NoConnection : Network
        data object Timeout : Network
        data object ServerError : Network
        data object Unauthorized : Network
    }

    /** Errors related to data integrity, parsing, or persistence. */
    sealed interface Data : AppError {
        data object MalformedResponse : Data
        data object NotFound : Data
        data object DiskFull : Data
    }

    /** Catch-all for truly unexpected system failures. */
    data class Unknown(val throwable: Throwable) : AppError
}

/**
 * Specialized Exception to wrap [AppError] so it can be passed through [Result.failure].
 */
class AppErrorException(val error: AppError) : Exception(error.toString())

/**
 * Utility to map low-level exceptions into domain-specific [AppError] types.
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppErrorException -> this.error
        is java.net.UnknownHostException -> AppError.Network.NoConnection
        is java.net.SocketTimeoutException -> AppError.Network.Timeout
        is org.json.JSONException -> AppError.Data.MalformedResponse
        else -> AppError.Unknown(this)
    }
}

/**
 * Helper to wrap the mapped [AppError] into an [AppErrorException].
 */
fun Throwable.toAppErrorAsException(): AppErrorException {
    return this as? AppErrorException ?: AppErrorException(this.toAppError())
}
