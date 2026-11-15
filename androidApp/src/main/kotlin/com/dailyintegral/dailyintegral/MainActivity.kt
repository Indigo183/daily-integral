package com.dailyintegral.dailyintegral

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    lateinit var dataStore: AndroidDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        dataStore = AndroidDataStore(this)


        setContent {
            val darkTheme = isSystemInDarkTheme()

            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (darkTheme) dynamicDarkColorScheme(this)
                    else dynamicLightColorScheme(this)
                }

                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    App(dataStore)
                }
            }
        }
    }
}