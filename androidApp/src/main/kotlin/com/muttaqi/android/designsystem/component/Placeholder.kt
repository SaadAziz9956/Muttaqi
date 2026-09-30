package com.muttaqi.android.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.muttaqi.android.designsystem.MuttaqiTheme

/** Stands in for a feature's screen until it's built in Compose */
@Composable
fun FeaturePlaceholder(title: String, onBack: (() -> Unit)? = null) {
    Box {
        SoftBackdrop()
        Scaffold(containerColor = Color.Transparent, topBar = { SoftTopBar(title, showTitle = false, onBack = onBack) }) { padding ->
            Column(
                Modifier.fillMaxSize().then(Modifier),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = MuttaqiTheme.soft.appPrimary)
            }
        }
    }
}
