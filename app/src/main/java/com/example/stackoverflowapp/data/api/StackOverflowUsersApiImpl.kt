package com.example.stackoverflowapp.data.api

import com.example.stackoverflowapp.data.network.HttpClient
import com.example.stackoverflowapp.data.network.NetworkResult
import com.example.stackoverflowapp.data.parser.JsonParsingException
import com.example.stackoverflowapp.data.parser.UsersResponseParser

class StackOverflowUsersApiImpl(
    private val httpClient: HttpClient,
    private val parser: UsersResponseParser
): StackOverflowUsersApi {

    override suspend fun fetchTopUsers(
        page: Int,
        pageSize: Int
    ): ApiResult<UsersResponseDto> {
        val url = buildUsersUrl(page = page, pageSize = pageSize)

        return when (val result = httpClient.get(url)) {
            is NetworkResult.Success -> {
                try {
                    val dto = parser.parseUsersResponse(result.data)
                    ApiResult.Success(dto)
                } catch (e: JsonParsingException) {
                    ApiResult.Error.Parse(e.message ?: "Failed to parse response")
                } catch (e: Exception) {
                    ApiResult.Error.Parse(e.message ?: "Unexpected parsing error")
                }
            }

            is NetworkResult.Error.Http -> {
                ApiResult.Error.Http(result.code, result.message)
            }

            is NetworkResult.Error.EmptyBody -> {
                ApiResult.Error.EmptyBody
            }

            is NetworkResult.Error.Network -> {
                ApiResult.Error.Network(
                    result.exception.message ?: "Network error"
                )
            }
        }
    }

    private fun buildUsersUrl(page: Int, pageSize: Int): String {
        return "https://api.stackexchange.com/2.2/users" +
                "?page=$page" +
                "&pagesize=$pageSize" +
                "&order=desc" +
                "&sort=reputation" +
                "&site=stackoverflow"
    }
}