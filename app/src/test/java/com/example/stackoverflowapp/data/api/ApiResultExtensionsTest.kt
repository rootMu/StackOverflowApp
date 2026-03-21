package com.example.stackoverflowapp.data.api

import com.example.stackoverflowapp.domain.model.AppError
import com.example.stackoverflowapp.domain.model.AppErrorException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResultExtensionsTest {

    @Test
    fun `toResult Success returns success Result`() {
        val data = "test data"
        val apiResult = ApiResult.Success(data)
        val result = apiResult.toResult()
        assertTrue(result.isSuccess)
        assertEquals(data, result.getOrNull())
    }

    @Test
    fun `toResult Error returns failure Result with AppErrorException`() {
        val apiResult = ApiResult.Error.Network("No internet")
        val result = apiResult.toResult()
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is AppErrorException)
        assertEquals(AppError.Network.NoConnection, (exception as AppErrorException).error)
    }

    @Test
    fun `toAppError maps Network to NoConnection`() {
        val apiError = ApiResult.Error.Network("No connection")
        assertEquals(AppError.Network.NoConnection, apiError.toAppError())
    }

    @Test
    fun `toAppError maps Parse to MalformedResponse`() {
        val apiError = ApiResult.Error.Parse("Bad JSON")
        assertEquals(AppError.Data.MalformedResponse, apiError.toAppError())
    }

    @Test
    fun `toAppError maps EmptyBody to NotFound`() {
        val apiError = ApiResult.Error.EmptyBody
        assertEquals(AppError.Data.NotFound, apiError.toAppError())
    }

    @Test
    fun `toAppError maps HTTP 401 to Unauthorized`() {
        val apiError = ApiResult.Error.Http(401, "Unauthorized")
        assertEquals(AppError.Network.Unauthorized, apiError.toAppError())
    }

    @Test
    fun `toAppError maps HTTP 5xx to ServerError`() {
        val apiError = ApiResult.Error.Http(500, "Internal Server Error")
        assertEquals(AppError.Network.ServerError, apiError.toAppError())
    }
}
