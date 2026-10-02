package com.muttaqi.android.feature.share

import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.component.softClickable
import com.muttaqi.android.designsystem.component.softPressScale
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.share.presentation.ShareEffect
import com.muttaqi.shared.feature.share.presentation.ShareIntent
import com.muttaqi.shared.feature.share.presentation.ShareState
import com.muttaqi.shared.feature.share.presentation.ShareViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ShareRoute(passage: SharePassage, onBack: () -> Unit) {
    val viewModel = koinViewModel<ShareViewModel> { parametersOf(passage) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val image = rememberGraphicsLayer()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun save(fileName: String) = scope.launch {
        val saved = image.toShareBitmap()?.let { context.saveImageToGallery(it, fileName) } ?: false
        snackbar.showSnackbar(if (saved) "Saved to your gallery" else "Couldn't save the image")
    }
    var waitingToSave by rememberSaveable { mutableStateOf<String?>(null) }
    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val fileName = waitingToSave ?: return@rememberLauncherForActivityResult
        waitingToSave = null
        if (granted) save(fileName) else scope.launch { snackbar.showSnackbar("Allow Muttaqi to store photos to save the image") }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ShareEffect.ShareImage -> image.toShareBitmap()?.let { context.shareImage(it, effect.title, effect.fileName) }
                is ShareEffect.SaveImage -> if (context.needsStoragePermissionToSave()) {
                    waitingToSave = effect.fileName
                    storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    save(effect.fileName)
                }
            }
        }
    }
    LightSystemBarIcons()
    Box {
        RecordShareCardImage(state.passage, image)
        ShareScreen(state, viewModel::dispatch, onBack, snackbar)
    }
}

@Composable
fun ShareScreen(
    state: ShareState,
    onIntent: (ShareIntent) -> Unit,
    onBack: () -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val green = MuttaqiTheme.soft.brandGreen
    val navigationBar = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    Scaffold(
        modifier = Modifier.drawBehind { drawRect(green, size = Size(size.width, size.height + navigationBar)) },
        containerColor = green,
        topBar = {
            ShareTopBar(
                onBack = onBack,
                onSave = { onIntent(ShareIntent.SaveTapped) },
                onShare = { onIntent(ShareIntent.ShareTapped) },
            )
        },
        bottomBar = { ShareFooter(state.verse) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Share", Modifier.padding(top = 8.dp), style = MaterialTheme.typography.headlineMedium, color = Color.White)
            ShareCard(state.passage, Modifier.padding(horizontal = 21.dp).padding(top = 28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareTopBar(onBack: () -> Unit, onSave: () -> Unit, onShare: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {},
        navigationIcon = { GlassButton(R.drawable.ic_arrow_left_02_linear, "Back", onBack, Modifier.padding(start = 12.dp)) },
        actions = {
            GlassButton(R.drawable.ic_import_linear, "Save image", onSave)
            Spacer(Modifier.width(8.dp))
            GlassButton(R.drawable.ic_send_2_linear, "Share", onShare, Modifier.padding(end = 12.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent),
    )
}

@Composable
private fun GlassButton(@DrawableRes icon: Int, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dark = MuttaqiTheme.soft.dark
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(44.dp)
            .softPressScale(interaction)
            .clip(CircleShape)
            .background(if (dark) Color(0xFF064837) else Color(0xFF6EAF9F))
            .border(0.75.dp, if (dark) Color(0xFF00815B) else Color(0xFF6BE2DB), CircleShape)
            .softClickable(interaction, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription, Modifier.size(24.dp), tint = Color.White)
    }
}

@Composable
private fun ShareFooter(verse: DisplayedQuote) {
    val text = Color.White.copy(alpha = 0.85f)
    Column(
        Modifier
            .fillMaxWidth()
            .background(MuttaqiTheme.soft.brandGreen)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TranslationText(verse.text, color = text, lineSpacing = 0.sp)
        Text(verse.source, style = MaterialTheme.typography.labelSmall, color = text)
    }
}

@Composable
private fun LightSystemBarIcons() {
    val window = LocalActivity.current?.window ?: return
    DisposableEffect(window) {
        val bars = WindowCompat.getInsetsController(window, window.decorView)
        val status = bars.isAppearanceLightStatusBars
        val navigation = bars.isAppearanceLightNavigationBars
        bars.isAppearanceLightStatusBars = false
        bars.isAppearanceLightNavigationBars = false
        onDispose {
            bars.isAppearanceLightStatusBars = status
            bars.isAppearanceLightNavigationBars = navigation
        }
    }
}
