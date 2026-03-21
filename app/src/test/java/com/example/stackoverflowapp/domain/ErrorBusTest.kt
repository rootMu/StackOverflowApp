package com.example.stackoverflowapp.domain

import com.example.stackoverflowapp.domain.model.AppError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [ErrorBus] ensuring reliable event propagation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ErrorBusTest {

    @Test
    fun `postError emits error to flow when collector is ready`() = runTest {
        val errorBus = ErrorBus()
        val expectedError = AppError.Network.NoConnection

        val deferredError = async(UnconfinedTestDispatcher(testScheduler)) {
            errorBus.errors.first()
        }

        errorBus.postError(expectedError)

        assertEquals(expectedError, deferredError.await())
    }

    @Test
    fun `tryPostError emits error to flow for non-suspending contexts`() = runTest {
        val errorBus = ErrorBus()
        val expectedError = AppError.Network.ServerError

        val collectedErrors = mutableListOf<AppError>()
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            errorBus.errors.collect { collectedErrors.add(it) }
        }

        errorBus.tryPostError(expectedError)

        assertEquals(1, collectedErrors.size)
        assertEquals(expectedError, collectedErrors[0])
    }
}
