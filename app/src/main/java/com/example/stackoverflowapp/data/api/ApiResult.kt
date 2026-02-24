package com.example.stackoverflowapp.data.api

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    sealed interface Error : ApiResult<Nothing> {
        data class Network(val message: String) : Error
        data class Http(val code: Int, val message: String? = null) : Error
        data class Parse(val message: String) : Error
        data object EmptyBody : Error
    }
}