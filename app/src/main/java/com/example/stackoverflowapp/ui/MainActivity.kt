package com.example.stackoverflowapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stackoverflowapp.StackOverflowApp
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.ui.components.LoadingStateView
import com.example.stackoverflowapp.ui.theme.StackOverflowTheme

class MainActivity: ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        (application as StackOverflowApp).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StackOverflowTheme {
                LoadingStateView()
            }
        }
    }
}

