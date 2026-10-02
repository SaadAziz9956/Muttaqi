package com.muttaqi.android.feature.dua

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme
import com.muttaqi.android.designsystem.component.SoftBackdrop
import com.muttaqi.android.designsystem.component.SoftCard
import com.muttaqi.android.designsystem.component.SoftTopBar
import com.muttaqi.shared.feature.dua.presentation.category.DuaCategoryEffect
import com.muttaqi.shared.feature.dua.presentation.category.DuaCategoryIntent
import com.muttaqi.shared.feature.dua.presentation.category.DuaCategoryState
import com.muttaqi.shared.feature.dua.presentation.category.DuaCategoryViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DuaCategoryRoute(categoryId: String, onOpenChapter: (String) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<DuaCategoryViewModel> { parametersOf(categoryId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DuaCategoryEffect.OpenChapter -> onOpenChapter(effect.chapterId)
            }
        }
    }
    DuaCategoryScreen(state, viewModel::dispatch, onBack)
}

@Composable
fun DuaCategoryScreen(state: DuaCategoryState, onIntent: (DuaCategoryIntent) -> Unit, onBack: () -> Unit) {
    val soft = MuttaqiTheme.soft
    val list = rememberLazyListState()
    val title = state.category?.title.orEmpty()
    Box {
        SoftBackdrop()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SoftTopBar(title, showTitle = list.firstVisibleItemIndex > 0, onBack = onBack) },
        ) { padding ->
            LazyColumn(
                state = list,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = padding.calculateTopPadding() + 12.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        title,
                        Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        color = soft.appPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
                items(state.category?.chapters.orEmpty(), key = { it.id }) { chapter ->
                    SoftCard(cornerRadius = 24.dp, onClick = { onIntent(DuaCategoryIntent.ChapterTapped(chapter.id)) }) {
                        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(chapter.title, style = MaterialTheme.typography.titleSmall, color = soft.appPrimary)
                                Text(
                                    if (chapter.entries.size == 1) "1 dua" else "${chapter.entries.size} duas",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = soft.brandTeal,
                                )
                            }
                            Icon(painterResource(R.drawable.ic_arrow_right_02_linear), null, Modifier.size(18.dp), tint = soft.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
