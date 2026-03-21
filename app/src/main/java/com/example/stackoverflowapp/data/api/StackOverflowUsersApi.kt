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
     * @param sort The field to sort by (reputation, creation, name, modified).
     * @param order The order of sorting (asc, desc).
     * @return [ApiResult] containing [UsersResponseDto].
     */
    suspend fun fetchTopUsers(
        page: Int = 1,
        pageSize: Int = 20,
        sort: String = "reputation",
        order: String = "desc"
    ): ApiResult<UsersResponseDto>

    /**
     * Fetches detailed information for a specific user.
     *
     * @param userId The unique ID of the user.
     * @return [ApiResult] containing [UsersResponseDto].
     */
    suspend fun fetchUserDetails(userId: Int): ApiResult<UsersResponseDto>
}
