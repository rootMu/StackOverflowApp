package com.example.stackoverflowapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.stackoverflowapp.data.repo.UserRepository

class HomeViewModelFactory(
    private val userRepository: UserRepository
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}