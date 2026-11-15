package com.dailyintegral.dailyintegral

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    lateinit var dataStore: AndroidDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        dataStore = AndroidDataStore(this)

        setContent {
            App(dataStore)
        }
    }
}