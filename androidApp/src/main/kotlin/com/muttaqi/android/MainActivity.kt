package com.muttaqi.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.navigation.MuttaqiApp
import com.muttaqi.shared.feature.home.domain.usecase.PrepareContent
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val prepareContent: PrepareContent by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        var preparing = true
        lifecycleScope.launch {
            prepareContent()
            preparing = false
        }
        splash.setKeepOnScreenCondition { preparing }
        enableEdgeToEdge()
        setContent {
            MuttaqiTheme {
                MuttaqiApp()
            }
        }
    }
}
