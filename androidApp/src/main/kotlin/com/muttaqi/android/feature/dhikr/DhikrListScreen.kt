package com.muttaqi.android.feature.dhikr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.component.ArabicText
import com.muttaqi.android.designsystem.component.BackButton
import com.muttaqi.android.designsystem.component.PageQuote
import com.muttaqi.android.designsystem.component.TranslationText
import com.muttaqi.shared.core.text.quoted
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListEffect
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListIntent
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListState
import com.muttaqi.shared.feature.dhikr.presentation.list.DhikrListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DhikrListRoute(onOpenCounter: (String) -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<DhikrListViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DhikrListEffect.OpenCounter -> onOpenCounter(effect.dhikrId)
            }
        }
    }
    DhikrListScreen(state, viewModel::dispatch, onBack)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DhikrListScreen(state: DhikrListState, onIntent: (DhikrListIntent) -> Unit, onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val section = state.selectedSection
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(TITLE) },
                navigationIcon = { BackButton(onBack) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = padding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.header?.let { header -> item(key = "header") { PageQuote(header, Modifier.padding(horizontal = 16.dp)) } }
            if (section != null) {
                item(key = "sections") { SectionChips(state, selectedId = section.id, onIntent) }
                item(key = "subtitle") {
                    Text(
                        section.subtitle,
                        Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(section.dhikr, key = { it.id }) { dhikr ->
                    DhikrRow(
                        dhikr,
                        onClick = { onIntent(DhikrListIntent.DhikrTapped(dhikr.id)) },
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionChips(state: DhikrListState, selectedId: String, onIntent: (DhikrListIntent) -> Unit) {
    val row = rememberLazyListState()
    val selectedIndex = state.sections.indexOfFirst { it.id == selectedId }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < 0) return@LaunchedEffect
        val viewport = row.layoutInfo.viewportSize.width
        val chip = row.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }?.size ?: 0
        row.animateScrollToItem(selectedIndex, scrollOffset = -(viewport - chip) / 2)
    }
    LazyRow(
        state = row,
        modifier = Modifier.selectableGroup(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.sections, key = { it.id }) { section ->
            val selected = section.id == selectedId
            FilterChip(
                selected = selected,
                onClick = { onIntent(DhikrListIntent.SectionTapped(section.id)) },
                label = { Text(section.title) },
                leadingIcon = if (selected) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun DhikrRow(dhikr: Dhikr, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val caption = dhikr.title ?: dhikr.translation?.quoted() ?: dhikr.transliteration.orEmpty()
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {
            contentDescription = dhikr.title ?: dhikr.transliteration ?: dhikr.arabic
            dhikr.translation?.let { stateDescription = it }
        },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ArabicText(dhikr.arabic, Modifier.fillMaxWidth())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TranslationText(
                    caption,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                dhikr.target?.let { target ->
                    Text("$target×", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private const val TITLE = "Zikr o Azkar"
