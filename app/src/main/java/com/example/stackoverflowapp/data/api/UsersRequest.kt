package com.example.stackoverflowapp.data.api

data class UsersRequest (
    val page: Int = 1,
    val pageSize: Int = 20,
    val order: String = "desc",
    val sort: String = "reputation",
    val site: String = "stackoverflow"
)