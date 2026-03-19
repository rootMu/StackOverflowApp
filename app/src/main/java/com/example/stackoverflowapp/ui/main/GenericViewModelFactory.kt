package com.example.stackoverflowapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class GenericViewModelFactory<T: ViewModel>(
    private val creator : () -> T
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}