package com.example.stackoverflowapp.domain.model

data class User(
    val id: Int,
    val displayName: String,
    val reputation: Int,
    val profileImageUrl: String?,
    val location: String?,
    val websiteUrl: String?
)