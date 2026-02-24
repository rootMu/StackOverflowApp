package com.example.stackoverflowapp.data.api

interface StackoverflowUsersApi {
    suspend fun fetchTopUsers(page: Int = 1, pageSize: Int = 20): ApiResult<UsersResponseDto>
}