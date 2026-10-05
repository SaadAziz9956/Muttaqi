package com.muttaqi.android.feature.dua

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.BackButton
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DuaCategoryScreen(state: DuaCategoryState, onIntent: (DuaCategoryIntent) -> Unit, onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(state.category?.title.orEmpty()) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(state.category?.chapters.orEmpty(), key = { it.id }) { chapter ->
                ListItem(
                    headlineContent = { Text(chapter.title) },
                    supportingContent = { Text(if (chapter.entries.size == 1) "1 dua" else "${chapter.entries.size} duas") },
                    trailingContent = { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) },
                    modifier = Modifier.clickable { onIntent(DuaCategoryIntent.ChapterTapped(chapter.id)) },
                )
                HorizontalDivider()
            }
        }
    }
}
