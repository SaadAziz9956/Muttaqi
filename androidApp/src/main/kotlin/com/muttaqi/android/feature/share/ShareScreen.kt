package com.muttaqi.android.feature.share

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.PageQuote
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareScreen(
    state: ShareState,
    onIntent: (ShareIntent) -> Unit,
    onBack: () -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Share") },
                navigationIcon = { BackButton(onBack) },
                actions = {
                    IconButton(onClick = { onIntent(ShareIntent.SaveTapped) }) {
                        Icon(painterResource(R.drawable.ic_download), contentDescription = "Save image")
                    }
                    IconButton(onClick = { onIntent(ShareIntent.ShareTapped) }) {
                        Icon(painterResource(R.drawable.ic_share), contentDescription = "Share")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageQuote(state.verse)
            ShareCard(state.passage)
        }
    }
}
