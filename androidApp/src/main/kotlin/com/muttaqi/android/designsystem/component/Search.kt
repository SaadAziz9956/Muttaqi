package com.muttaqi.android.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.muttaqi.android.R
import kotlinx.coroutines.flow.drop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val text = rememberTextFieldState(query)
    val latestOnQueryChange by rememberUpdatedState(onQueryChange)
    LaunchedEffect(text) { snapshotFlow { text.text.toString() }.drop(1).collect { latestOnQueryChange(it) } }
    LaunchedEffect(query) { if (query != text.text.toString()) text.setTextAndPlaceCursorAtEnd(query) }
    val searchBarState = rememberSearchBarState()
    SearchBar(
        state = searchBarState,
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = text,
                searchBarState = searchBarState,
                onSearch = {},
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { text.clearText(); onClear() }) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = "Clear search")
                        }
                    }
                } else {
                    null
                },
            )
        },
        modifier = modifier.fillMaxWidth(),
    )
}
