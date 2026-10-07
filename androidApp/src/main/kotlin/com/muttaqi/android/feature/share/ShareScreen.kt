package com.muttaqi.android.feature.share

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.android.designsystem.oneui.OneUi
import com.muttaqi.android.designsystem.oneui.OneUiHeaderQuote
import com.muttaqi.android.designsystem.oneui.OneUiButton
import com.muttaqi.android.designsystem.oneui.OneUiDefaults
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiListRow
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSnackbarHost
import com.muttaqi.android.designsystem.oneui.OneUiSurface
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.quoted
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
    val colors = OneUi.colors
    OneUiSurface {
        OneUiScaffold(
            title = "Share",
            onBack = onBack,
            subtitle = { OneUiHeaderQuote(state.verse.text, state.verse.source) },
            bottomBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(OneUi.colors.background)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    OneUiButton(
                        "Share",
                        onClick = { onIntent(ShareIntent.ShareTapped) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = R.drawable.ic_share,
                    )
                }
            },
            snackbarHost = { OneUiSnackbarHost(snackbar) },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(OneUiDefaults.GroupGap),
            ) {
                ShareCard(state.passage, Modifier.padding(horizontal = OneUiDefaults.ScreenMargin))
                OneUiGroup {
                    OneUiListRow(
                        title = "Save image",
                        summary = "Save a copy to your gallery",
                        onClick = { onIntent(ShareIntent.SaveTapped) },
                        trailing = {
                            Icon(painterResource(R.drawable.ic_download), contentDescription = null, Modifier.size(24.dp), tint = colors.accent)
                        },
                    )
                }
            }
        }
    }
}

