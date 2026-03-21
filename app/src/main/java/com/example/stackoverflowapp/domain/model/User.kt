package com.example.stackoverflowapp.domain.model

data class User(
    val id: Int,
    val displayName: String,
    val reputation: Int,
    val profileImageUrl: String?,
    val badgeCounts: BadgeCounts?,
    val location: String?,
    val websiteUrl: String?,
    val aboutMe: String? = null,
    val creationDate: Long? = null,
    val lastModifiedDate: Long? = null
)
