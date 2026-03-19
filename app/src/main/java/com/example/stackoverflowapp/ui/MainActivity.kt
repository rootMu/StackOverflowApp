package com.example.stackoverflowapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.example.stackoverflowapp.StackOverflowApp
import com.example.stackoverflowapp.di.AppContainer
import com.example.stackoverflowapp.di.LocalAppContainer
import com.example.stackoverflowapp.ui.main.MainNavigationHost
import com.example.stackoverflowapp.ui.theme.StackOverflowTheme

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        (application as StackOverflowApp).container
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CompositionLocalProvider(
                LocalAppContainer provides appContainer
            ) {
                StackOverflowTheme {
                    MainNavigationHost()
                }
            }
        }
    }
}

