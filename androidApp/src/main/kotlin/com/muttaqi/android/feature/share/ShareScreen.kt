package com.muttaqi.android.feature.share

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.PageHeader
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.SoftTopBar
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
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val scrolled by remember(scroll) { derivedStateOf { scroll.value > with(density) { 72.dp.roundToPx() } } }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SoftTopBar("Share", showTitle = scrolled, onBack = onBack) {
                    SoftIconButton(R.drawable.ic_import_linear, "Save image", { onIntent(ShareIntent.SaveTapped) }, size = 44.dp, iconSize = 22.dp)
                    Spacer(Modifier.width(8.dp))
                    SoftIconButton(R.drawable.ic_send_2_linear, "Share", { onIntent(ShareIntent.ShareTapped) }, Modifier.padding(end = 12.dp), size = 44.dp, iconSize = 22.dp)
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(top = padding.calculateTopPadding(), bottom = 32.dp + bottomInset)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PageHeader("Share", state.verse, Modifier.padding(top = 8.dp))
                ShareCard(state.passage, Modifier.padding(top = 32.dp))
            }
        }
    }
}
