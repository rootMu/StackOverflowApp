package com.example.stackoverflowapp.domain.model

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AppErrorTest {

    @Test
    fun `toAppError maps UnknownHostException to NoConnection`() {
        val throwable = UnknownHostException("No DNS")
        val appError = throwable.toAppError()
        assertEquals(AppError.Network.NoConnection, appError)
    }

    @Test
    fun `toAppError maps SocketTimeoutException to Timeout`() {
        val throwable = SocketTimeoutException("Slow connection")
        val appError = throwable.toAppError()
        assertEquals(AppError.Network.Timeout, appError)
    }

    @Test
    fun `toAppError maps JSONException to MalformedResponse`() {
        val throwable = JSONException("Bad JSON")
        val appError = throwable.toAppError()
        assertEquals(AppError.Data.MalformedResponse, appError)
    }

    @Test
    fun `toAppError maps AppErrorException correctly`() {
        val expectedError = AppError.Network.Unauthorized
        val appErrorException = AppErrorException(expectedError)
        val appError = appErrorException.toAppError()
        assertEquals(expectedError, appError)
    }

    @Test
    fun `toAppError maps generic Throwable to Unknown`() {
        val throwable = RuntimeException("Unknown error")
        val appError = throwable.toAppError()
        assertTrue(appError is AppError.Unknown)
        assertEquals(throwable, (appError as AppError.Unknown).throwable)
    }

    @Test
    fun `toAppErrorAsException returns AppErrorException`() {
        val throwable = UnknownHostException("No DNS")
        val exception = throwable.toAppErrorAsException()
        assertEquals(AppError.Network.NoConnection, exception.error)
    }

    @Test
    fun `toAppErrorAsException returns original if already AppErrorException`() {
        val original = AppErrorException(AppError.Data.DiskFull)
        val result = original.toAppErrorAsException()
        assertEquals(original, result)
    }
}
