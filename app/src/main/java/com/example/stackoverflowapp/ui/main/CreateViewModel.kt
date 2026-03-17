package com.example.stackoverflowapp.ui.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.LocalAppContainer

@Composable
inline fun <reified VM : ViewModel> createViewModel(
    key: String? = VM::class.qualifiedName,
    noinline creator: (AppContainer) -> VM
): VM {
    val container = LocalAppContainer.current

    return viewModel(
        key = key,
        factory = GenericViewModelFactory {
            creator(container)
        }
    )
}