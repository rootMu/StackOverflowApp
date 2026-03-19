package com.example.stackoverflowapp.data.api

class FakeStackOverflowUsersApi(
    private var result: ApiResult<UsersResponseDto>
) : StackOverflowUsersApi {

    var callCount: Int = 0
        private set

    var lastPage: Int? = null
        private set

    var lastPageSize: Int? = null
        private set

    var lastUserId: Int? = null
        private set

    override suspend fun fetchTopUsers(
        page: Int,
        pageSize: Int
    ): ApiResult<UsersResponseDto> {
        callCount++
        lastPage = page
        lastPageSize = pageSize
        return result
    }

    override suspend fun fetchUserDetails(userId: Int): ApiResult<UsersResponseDto> {
        callCount++
        lastUserId = userId
        return result
    }

    fun setResponse(newResult: ApiResult<UsersResponseDto>) {
        result = newResult
    }
}
