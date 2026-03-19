package com.example.stackoverflowapp.data.api

import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult
import com.example.stackoverflowapp.data.parser.UsersResponseParser

class StackOverflowUsersApiImpl(
    private val httpClient: HttpClient,
    private val parser: UsersResponseParser
) : StackOverflowUsersApi {

    private companion object {
        const val BASE_URL = "https://api.stackexchange.com/2.2/users"
        const val SITE = "stackoverflow"
        const val DEFAULT_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 20
        const val USER_DETAILS_FILTER = "Dh6gvXqpGDFcXaUgK"
    }

    override suspend fun fetchTopUsers(
        page: Int,
        pageSize: Int
    ): ApiResult<UsersResponseDto> = executeRequest(
        url = buildUsersUrl(
            page = page,
            pageSize = pageSize
        )
    )


    override suspend fun fetchUserDetails(userId: Int): ApiResult<UsersResponseDto> =
        executeRequest(
            url = buildUsersUrl(
                userId = userId,
                filter = USER_DETAILS_FILTER
            )
        )


    private suspend fun executeRequest(url: String): ApiResult<UsersResponseDto> {
        return when (val result = httpClient.get(url)) {
            is NetworkResult.Success -> parseResponse(result.data)
            is NetworkResult.Error.Http -> ApiResult.Error.Http(result.code, result.message)
            is NetworkResult.Error.EmptyBody -> ApiResult.Error.EmptyBody
            is NetworkResult.Error.Network -> {
                ApiResult.Error.Network(result.exception.message ?: "Network error")
            }
        }
    }

    private fun parseResponse(rawJson: String): ApiResult<UsersResponseDto> {
        return try {
            ApiResult.Success(parser.parseUsersResponse(rawJson))
        } catch (e: Exception) {
            ApiResult.Error.Parse(e.message ?: "Failed to parse response")
        }
    }

    private fun buildUsersUrl(
        page: Int = DEFAULT_PAGE,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        userId: Int? = null,
        filter: String? = null
    ): String {
        val path = buildString {
            append(BASE_URL)
            userId?.let { append("/$it") }
        }

        val queryParams = buildList {
            add("site=$SITE")

            if (userId == null) {
                add("page=$page")
                add("pagesize=$pageSize")
                add("order=desc")
                add("sort=reputation")
            }

            filter?.let { add("filter=$it") }
        }

        return "$path?${queryParams.joinToString("&")}"
    }
}