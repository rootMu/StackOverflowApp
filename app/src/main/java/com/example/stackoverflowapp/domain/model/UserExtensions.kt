package com.example.stackoverflowapp.domain.model

fun User.validate(): User {
    require(id > 0) { " User id must be positive "}
    require(displayName.isNotBlank()) { " User name cannot be blank "}
    require(reputation >= 0) { " User reputation must be positive "}
    return this
}