package com.example.stackoverflowapp.domain

import com.example.stackoverflowapp.domain.model.AppError
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A centralized bus for observing app-wide errors.
 * The AppContainer should provide this as a singleton.
 */
class ErrorBus {
    private val _errors = MutableSharedFlow<AppError>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errors: SharedFlow<AppError> = _errors.asSharedFlow()

    /**
     * Posts an error to the global observer.
     */
    suspend fun postError(error: AppError) {
        _errors.emit(error)
    }

    /**
     * Non-suspending version for use in non-coroutine contexts
     */
    fun tryPostError(error: AppError) {
        println("Global Error Logged: $error")
        _errors.tryEmit(error)
    }
}
