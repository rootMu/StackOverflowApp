package com.example.stackoverflowapp.data.api

/**
 * Interface defining the Stack Overflow Users API operations.
 */
interface StackOverflowUsersApi {
    /**
     * Fetches the top users from Stack Overflow.
     *
     * @param page The page number to fetch.
     * @param pageSize The number of users per page.
     * @return [ApiResult] containing [UsersResponseDto].
     */
    suspend fun fetchTopUsers(page: Int = 1, pageSize: Int = 20): ApiResult<UsersResponseDto>

    /**
     * Fetches detailed information for a specific user.
     *
     * @param userId The unique ID of the user.
     * @return [ApiResult] containing [UsersResponseDto].
     */
    suspend fun fetchUserDetails(userId: Int): ApiResult<UsersResponseDto>
}
