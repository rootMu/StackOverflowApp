package com.example.stackoverflowapp.domain

import com.example.stackoverflowapp.domain.model.AppError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Tests for [GlobalExceptionHandler] ensuring that uncaught exceptions
 * are correctly routed to the [ErrorBus] and the system's default handler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GlobalExceptionHandlerTest {

    @Test
    fun `uncaughtException posts to error bus and calls default handler`() = runTest {
        val errorBus = ErrorBus()
        val defaultHandlerCalled = AtomicBoolean(false)
        val caughtThrowable = AtomicReference<Throwable>()
        
        val defaultHandler = Thread.UncaughtExceptionHandler { _, throwable ->
            defaultHandlerCalled.set(true)
            caughtThrowable.set(throwable)
        }

        val handler = GlobalExceptionHandler(errorBus, defaultHandler)
        val exception = RuntimeException("Test crash")
        
        val busErrorDeferred = async(UnconfinedTestDispatcher(testScheduler)) {
            errorBus.errors.first()
        }

        handler.uncaughtException(Thread.currentThread(), exception)

        val busError = busErrorDeferred.await()
        assertTrue("Expected AppError.Unknown", busError is AppError.Unknown)
        assertEquals(exception, (busError as AppError.Unknown).throwable)

        assertTrue("Default handler should have been called", defaultHandlerCalled.get())
        assertEquals(exception, caughtThrowable.get())
    }

    @Test
    fun `install sets default uncaught exception handler`() {
        val errorBus = ErrorBus()
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        try {
            GlobalExceptionHandler.install(errorBus)
            val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
            assertTrue("Handler should be an instance of GlobalExceptionHandler", currentHandler is GlobalExceptionHandler)
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        }
    }
}
