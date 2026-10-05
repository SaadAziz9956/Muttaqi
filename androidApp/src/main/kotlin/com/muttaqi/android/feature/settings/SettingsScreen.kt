package com.muttaqi.android.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.ColorPreference
import com.muttaqi.android.designsystem.ColorSource
import com.muttaqi.android.designsystem.wallpaperColorsAvailable
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
data object SettingsRoute

fun NavGraphBuilder.settingsDestinations(navController: NavController) {
    composable<SettingsRoute> { SettingsRoute(onBack = navController::popBackStack) }
}

@Composable
fun SettingsRoute(onBack: () -> Unit) {
    val colors = koinInject<ColorPreference>()
    val source by colors.source.collectAsStateWithLifecycle()
    SettingsScreen(source, colors::choose, onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    colorSource: ColorSource,
    onColorSource: (ColorSource) -> Unit,
    onBack: () -> Unit,
    wallpaperAvailable: Boolean = wallpaperColorsAvailable,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back") }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(Modifier.selectableGroup(), contentPadding = padding) {
            item {
                Text(
                    "Colors",
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            item {
                ColorChoice(
                    title = "Wallpaper colors",
                    detail = if (wallpaperAvailable) "Match the colors of your wallpaper" else "Needs Android 12 or later",
                    selected = colorSource == ColorSource.Wallpaper && wallpaperAvailable,
                    enabled = wallpaperAvailable,
                    onClick = { onColorSource(ColorSource.Wallpaper) },
                )
            }
            item {
                ColorChoice(
                    title = "Muttaqi green",
                    detail = "The app's own green",
                    selected = colorSource == ColorSource.MuttaqiGreen || !wallpaperAvailable,
                    enabled = true,
                    onClick = { onColorSource(ColorSource.MuttaqiGreen) },
                )
            }
        }
    }
}

@Composable
private fun ColorChoice(title: String, detail: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(detail) },
        leadingContent = { RadioButton(selected = selected, onClick = null, enabled = enabled) },
        modifier = Modifier.selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
    )
}
