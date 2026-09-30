package com.muttaqi.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.navigation.MuttaqiApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuttaqiTheme {
                MuttaqiApp()
            }
        }
    }
}
