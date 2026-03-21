package com.example.stackoverflowapp.data.api

data class UserDto(
    val userId: Int,
    val displayName: String,
    val reputation: Int,
    val profileImageUrl: String?,
    val badgeCounts: BadgeCountsDto? = null,
    val location: String? = null,
    val websiteUrl: String? = null,
    val aboutMe: String? = null,
    val creationDate: Long? = null,
    val lastModifiedDate: Long? = null
)
