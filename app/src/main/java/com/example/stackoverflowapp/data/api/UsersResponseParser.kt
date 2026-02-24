package com.example.stackoverflowapp.data.api

interface UsersResponseParser {
    fun parseUsersResponse(json: String): UsersResponseDto
}