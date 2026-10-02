package com.muttaqi.android.feature.dua

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftIconButton
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.core.text.isArabicScript
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterEffect
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterIntent
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterState
import com.muttaqi.shared.feature.dua.presentation.chapter.DuaChapterViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DuaChapterRoute(chapterId: String, onShare: (SharePassage) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<DuaChapterViewModel> { parametersOf(chapterId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DuaChapterEffect.OpenShare -> onShare(effect.passage)
                is DuaChapterEffect.Copy -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Dua", effect.text))) }
            }
        }
    }
    DuaChapterScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun DuaChapterScreen(state: DuaChapterState, onIntent: (DuaChapterIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val list = rememberLazyListState()
    val chapter = state.chapter
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SoftTopBar(chapter?.title.orEmpty(), showTitle = list.firstVisibleItemIndex > 0, onBack = onBack) },
        ) { padding ->
            LazyColumn(
                state = list,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(chapter?.title.orEmpty(), style = MaterialTheme.typography.headlineSmall, color = soft.appPrimary, textAlign = TextAlign.Center)
                        chapter?.titleArabic?.let { ArabicText(it, fontSize = 18.sp, color = soft.textSecondary, lineSpacing = 0.sp) }
                    }
                }
                items(chapter?.entries.orEmpty(), key = { it.id }) { entry ->
                    DuaEntryCard(
                        entry,
                        onShare = { onIntent(DuaChapterIntent.ShareTapped(entry.id)) },
                        onCopy = { onIntent(DuaChapterIntent.CopyTapped(entry.id)) },
                    )
                }
                if (state.translationCredits.isNotEmpty()) {
                    item {
                        Text(
                            "Translation: " + state.translationCredits.joinToString(", "),
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = soft.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DuaEntryCard(entry: DuaEntry, onShare: () -> Unit, onCopy: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val urdu = entry.translation.isArabicScript()
    SoftCard(cornerRadius = 26.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp)) {
            ArabicText(entry.arabic, Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
            if (entry.transliteration.isNotBlank()) {
                Text(entry.transliteration, Modifier.padding(top = 14.dp), style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = soft.appPrimary)
            }
            TranslationText(
                entry.translation,
                Modifier.fillMaxWidth().padding(top = 12.dp),
                textAlign = if (urdu) TextAlign.Right else TextAlign.Left,
                lineSpacing = 4.sp,
            )
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(entry.source, style = MaterialTheme.typography.labelSmall, color = soft.brandTeal)
                Spacer(Modifier.weight(1f))
                if (entry.repeatCount > 1) {
                    Text(
                        "${entry.repeatCount}×",
                        Modifier.background(soft.tintedSurface, CircleShape).padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = soft.brandTeal,
                    )
                    Spacer(Modifier.height(0.dp).padding(start = 8.dp))
                }
                SoftIconButton(R.drawable.ic_copy_linear, "Copy", onCopy, size = 32.dp, iconSize = 15.dp)
                Spacer(Modifier.padding(start = 8.dp))
                SoftIconButton(R.drawable.ic_export_arrow_01_linear, "Share", onShare, size = 32.dp, iconSize = 15.dp)
            }
            if (entry.reference.isNotBlank()) {
                Text(
                    entry.reference,
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, textDirection = TextDirection.Rtl),
                    color = soft.textSecondary,
                    textAlign = TextAlign.Right,
                )
            }
        }
    }
}
