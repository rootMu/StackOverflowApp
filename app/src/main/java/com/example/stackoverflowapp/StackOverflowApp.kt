package com.example.stackoverflowapp

import android.app.Application
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.DefaultAppContainer

class StackOverflowApp: Application() {

    val container: AppContainer by lazy {
        DefaultAppContainer()
    }
}