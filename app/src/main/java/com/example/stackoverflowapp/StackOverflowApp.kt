package com.example.stackoverflowapp

import android.app.Application
import com.example.stackoverflowapp.di.AppContainer

class StackOverflowApp: Application() {

    val container: AppContainer by lazy {
        AppContainer(this)
    }
}