package com.example.stackoverflowapp

import android.app.Application
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.DefaultAppContainer

class StackOverflowApp: Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}