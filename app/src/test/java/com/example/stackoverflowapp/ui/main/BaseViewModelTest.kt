package com.example.stackoverflowapp.ui.main

import com.example.stackoverflowapp.domain.ErrorBus
import com.example.stackoverflowapp.domain.model.AppError
import com.example.stackoverflowapp.domain.model.AppErrorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var errorBus: ErrorBus

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        errorBus = ErrorBus()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    class TestViewModel(errorBus: ErrorBus) : BaseViewModel<Unit>(Unit, errorBus) {
        fun testLaunch(handleErrorsGlobally: Boolean, block: suspend () -> Unit) {
            launch(handleErrorsGlobally) { block() }
        }

        fun testPostError(error: AppError) = postError(error)
        fun testPostError(throwable: Throwable) = postError(throwable)
    }

    @Test
    fun `postError with AppError emits to error bus`() = runTest {
        val viewModel = TestViewModel(errorBus)
        val expectedError = AppError.Network.Timeout

        var receivedError: AppError? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            receivedError = errorBus.errors.first()
        }

        viewModel.testPostError(expectedError)
        advanceUntilIdle()

        assertEquals(expectedError, receivedError)
    }

    @Test
    fun `postError with Throwable emits mapped AppError to error bus`() = runTest {
        val viewModel = TestViewModel(errorBus)
        val exception = java.net.UnknownHostException()

        var receivedError: AppError? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            receivedError = errorBus.errors.first()
        }

        viewModel.testPostError(exception)
        advanceUntilIdle()

        assertEquals(AppError.Network.NoConnection, receivedError)
    }

    @Test
    fun `launch with handleErrorsGlobally=true posts error to bus when exception occurs`() = runTest {
        val viewModel = TestViewModel(errorBus)
        val exception = AppErrorException(AppError.Data.DiskFull)

        var receivedError: AppError? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            receivedError = errorBus.errors.first()
        }

        viewModel.testLaunch(handleErrorsGlobally = true) {
            throw exception
        }
        advanceUntilIdle()

        assertEquals(AppError.Data.DiskFull, receivedError)
    }
}
