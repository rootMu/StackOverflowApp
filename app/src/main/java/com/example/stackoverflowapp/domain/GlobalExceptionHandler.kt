package com.example.stackoverflowapp.domain

import com.example.stackoverflowapp.domain.model.AppError

/**
 * Intercepts crashes that weren't caught by try-catch blocks.
 */
class GlobalExceptionHandler(
    private val errorBus: ErrorBus,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        errorBus.tryPostError(AppError.Unknown(throwable))
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        fun install(errorBus: ErrorBus) {
            val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(errorBus, oldHandler))
        }
    }
}
