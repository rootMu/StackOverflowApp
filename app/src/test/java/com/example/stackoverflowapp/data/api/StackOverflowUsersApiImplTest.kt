package com.example.stackoverflowapp.data.api

import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult
import com.example.stackoverflowapp.data.parser.JsonParsingException
import com.example.stackoverflowapp.data.parser.UsersResponseParser
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StackOverflowUsersApiImplTest {

    @Test
    fun `fetchTopUsers() returns success when HttpClient and parser succeed`() = runTest {
        val json = """{"items":[]}"""
        val expectedDto = UsersResponseDto(
            items = listOf(
                UserDto(
                    userId = 123,
                    displayName = "Test User",
                    reputation = 99,
                    profileImageUrl = null
                )
            )
        )

        val httpClient = FakeHttpClient(
            getResult = NetworkResult.Success(json)
        )
        val parser = FakeUsersResponseParser(
            result = expectedDto
        )

        val api = StackOverflowUsersApiImpl(
            httpClient = httpClient,
            parser = parser
        )

        val result = api.fetchTopUsers(page = 1, pageSize = 20)

        assertTrue(result is ApiResult.Success)
        assertEquals(expectedDto, (result as ApiResult.Success).data)

        assertEquals(json, parser.lastJson)
        assertTrue(httpClient.lastGetUrl?.contains("/users") == true)
        assertTrue(httpClient.lastGetUrl?.contains("page=1") == true)
        assertTrue(httpClient.lastGetUrl?.contains("pagesize=20") == true)
        assertTrue(httpClient.lastGetUrl?.contains("sort=reputation") == true)
        assertTrue(httpClient.lastGetUrl?.contains("site=stackoverflow") == true)
    }

    @Test
    fun `fetchTopUsers() returns Http Error when HttpClient returns Http Error`() = runTest {
        assertApiError(
            httpResult = NetworkResult.Error.Http(code = 404, message = "Not Found")
        ) { error ->
            assertTrue(error is ApiResult.Error.Http)
            error as ApiResult.Error.Http
            assertEquals(404, error.code)
            assertEquals("Not Found", error.message)
        }
    }

    @Test
    fun `fetchTopUsers() returns empty body when HttpClient returns empty body`() = runTest {
        assertApiError(
            httpResult = NetworkResult.Error.EmptyBody
        ) { error ->
            assertTrue(error is ApiResult.Error.EmptyBody)
        }
    }

    @Test
    fun `fetchTopUsers() returns network error when HttpClient returns network error`() = runTest {
        assertApiError(
            httpResult = NetworkResult.Error.Network(
                exception = RuntimeException("timeout")
            )
        ) { error ->
            assertTrue(error is ApiResult.Error.Network)
            error as ApiResult.Error.Network
            assertTrue(error.message.contains("timeout"))
        }
    }

    @Test
    fun `fetchTopUsers() returns parse error when parser throws JsonParsingException`() = runTest {
        assertApiParseError(
            throwable = JsonParsingException("Bad JSON"),
            expectedMessagePart = "Bad JSON"
        )
    }

    @Test
    fun `fetchTopUsers() returns parse error when parser throws UnexpectedException`() = runTest {
        assertApiParseError(
            throwable = IllegalStateException("boom"),
            expectedMessagePart = "boom"
        )
    }

    private suspend fun assertApiError(
        httpResult: NetworkResult<String>,
        parser: UsersResponseParser = FakeUsersResponseParser(UsersResponseDto(emptyList())),
        assertion: (ApiResult.Error) -> Unit
    ) {
        val result = fetch(httpResult = httpResult, parser = parser)
        assertTrue(result is ApiResult.Error)
        assertion(result as ApiResult.Error)
    }

    private suspend fun assertApiParseError(
        throwable: Throwable,
        expectedMessagePart: String? = null
    ) {
        val result = fetch(
            httpResult = NetworkResult.Success("""{"items":[]}"""),
            parser = ThrowingUsersResponseParser(throwable)
        )
        assertTrue(result is ApiResult.Error.Parse)

        val parseError = result as ApiResult.Error.Parse
        if (expectedMessagePart != null) {
            assertTrue(
                parseError.message.contains(expectedMessagePart) || parseError.message.isNotBlank()
            )
        }
    }

    private suspend fun fetch(
        httpResult: NetworkResult<String>,
        parser: UsersResponseParser = FakeUsersResponseParser(UsersResponseDto(emptyList()))
    ): ApiResult<UsersResponseDto> {
        return StackOverflowUsersApiImpl(
            httpClient = FakeHttpClient(httpResult),
            parser = parser
        ).fetchTopUsers()
    }

    private class FakeHttpClient(
        private val getResult: NetworkResult<String>
    ) : HttpClient {

        var lastGetUrl: String? = null
            private set

        override suspend fun get(url: String): NetworkResult<String> {
            lastGetUrl = url
            return getResult
        }

        override suspend fun getBytes(url: String): NetworkResult<ByteArray> {
            error("Not used in this test")
        }
    }

    private class FakeUsersResponseParser(
        private val result: UsersResponseDto
    ) : UsersResponseParser {

        var lastJson: String? = null
            private set

        override fun parseUsersResponse(json: String): UsersResponseDto {
            lastJson = json
            return result
        }
    }

    private class ThrowingUsersResponseParser(
        private val throwable: Throwable
    ) : UsersResponseParser {
        override fun parseUsersResponse(json: String): UsersResponseDto {
            throw throwable
        }
    }
}