package com.example.stackoverflowapp.data.parser

import com.example.stackoverflowapp.data.api.UsersResponseDto

interface UsersResponseParser {
    fun parseUsersResponse(json: String): UsersResponseDto
}