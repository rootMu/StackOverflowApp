package com.example.stackoverflowapp.data.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>

    sealed interface Error : NetworkResult<Nothing> {
        data class Http(val code: Int, val message: String? = null) : Error
        data object EmptyBody : Error
        data class Network(val exception: Throwable) : Error
    }
}