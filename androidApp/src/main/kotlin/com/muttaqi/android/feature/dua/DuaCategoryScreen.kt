package com.muttaqi.android.feature.dua

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.designsystem.oneui.OneUiGroup
import com.muttaqi.android.designsystem.oneui.OneUiListRow
import com.muttaqi.android.designsystem.oneui.OneUiScaffold
import com.muttaqi.android.designsystem.oneui.OneUiSurface
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
    val chapters = state.category?.chapters.orEmpty()
    OneUiSurface {
        OneUiScaffold(title = state.category?.title.orEmpty(), onBack = onBack) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                if (chapters.isNotEmpty()) {
                    item(key = "chapters") {
                        OneUiGroup {
                            chapters.forEachIndexed { index, chapter ->
                                OneUiListRow(
                                    title = chapter.title,
                                    summary = duaCount(chapter.entries.size),
                                    divider = index < chapters.lastIndex,
                                    onClick = { onIntent(DuaCategoryIntent.ChapterTapped(chapter.id)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
