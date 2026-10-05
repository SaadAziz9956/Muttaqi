package com.muttaqi.android.feature.dua

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.QuranText
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.share.SharePassage
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DuaChapterScreen(state: DuaChapterState, onIntent: (DuaChapterIntent) -> Unit, onBack: () -> Unit) {
    val chapter = state.chapter
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(chapter?.title.orEmpty()) },
                subtitle = chapter?.titleArabic?.let { arabic -> { Text(arabic) } },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                        Modifier.fillMaxWidth().padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun DuaEntryCard(entry: DuaEntry, onShare: () -> Unit, onCopy: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (entry.isQuran) {
                QuranText(entry.arabic, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            } else {
                ArabicText(entry.arabic, Modifier.fillMaxWidth())
            }
            if (entry.transliteration.isNotBlank()) {
                Text(entry.transliteration, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            TranslationText(entry.translation, Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(entry.source, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    if (entry.repeatCount > 1) {
                        Text("Repeat ${entry.repeatCount}×", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onCopy) { Icon(painterResource(R.drawable.ic_content_copy), contentDescription = "Copy") }
                IconButton(onClick = onShare) { Icon(painterResource(R.drawable.ic_share), contentDescription = "Share") }
            }
            entry.grade?.let { grade ->
                TranslationText(
                    grade,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.reference.isNotBlank()) {
                ArabicText(
                    entry.reference,
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
