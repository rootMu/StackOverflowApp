package com.example.stackoverflowapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stackoverflowapp.domain.ErrorBus
import com.example.stackoverflowapp.domain.model.AppError
import com.example.stackoverflowapp.domain.model.AppErrorException
import com.example.stackoverflowapp.domain.model.toAppError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S>(
    initialState: S,
    protected val errorBus: ErrorBus? = null
) : ViewModel() {

    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    protected fun launch(
        handleErrorsGlobally: Boolean = false,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                if (handleErrorsGlobally) {
                    val appError = (e as? AppErrorException)?.error ?: e.toAppError()
                    errorBus?.postError(appError)
                } else {
                    throw e
                }
            }
        }
    }

    protected fun postError(error: AppError) {
        launch {
            errorBus?.postError(error)
        }
    }

    protected fun postError(throwable: Throwable) {
        launch {
            val appError = (throwable as? AppErrorException)?.error ?: throwable.toAppError()
            errorBus?.postError(appError)
        }
    }
}
