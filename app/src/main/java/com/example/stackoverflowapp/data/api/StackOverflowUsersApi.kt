package com.example.stackoverflowapp.data.api

interface StackOverflowUsersApi {
    suspend fun fetchTopUsers(page: Int = 1, pageSize: Int = 20): ApiResult<UsersResponseDto>
}