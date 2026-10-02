package com.muttaqi.android.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme

@Composable
fun SoftSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    val soft = MuttaqiTheme.soft
    SoftPillSurface(modifier.fillMaxWidth().height(50.dp)) {
        Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_search_normal_linear), null, Modifier.size(18.dp), tint = soft.textSecondary)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = soft.textPrimary),
                cursorBrush = SolidColor(soft.appPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                decorationBox = { field ->
                    if (query.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = soft.textSecondary)
                    field()
                },
            )
            if (query.isNotEmpty()) {
                SoftIconButton(R.drawable.ic_close_circle_bold, "Clear search", onClear, size = 28.dp, iconSize = 18.dp, rim = false)
            }
        }
    }
}

@Composable
fun SoftIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    filled: Boolean = false,
    rim: Boolean = !filled,
) {
    val soft = MuttaqiTheme.soft
    SoftPillSurface(
        modifier.size(size),
        fill = if (filled) soft.brandGreen else soft.surface,
        rim = rim,
        onClick = onClick,
    ) {
        Icon(
            painterResource(icon),
            contentDescription,
            Modifier.size(iconSize),
            tint = if (filled) androidx.compose.ui.graphics.Color.White else soft.appPrimary,
        )
    }
}

@Composable
fun SoftChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val soft = MuttaqiTheme.soft
    SoftPillSurface(
        modifier.height(36.dp),
        fill = if (selected) soft.brandGreen else soft.surface,
        rim = !selected,
        onClick = onClick,
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 18.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) androidx.compose.ui.graphics.Color.White else soft.appPrimary,
        )
    }
}

enum class SoftButtonKind { Primary, Secondary }

@Composable
fun SoftButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: SoftButtonKind = SoftButtonKind.Primary) {
    val soft = MuttaqiTheme.soft
    val primary = kind == SoftButtonKind.Primary
    SoftPillSurface(
        modifier.heightIn(min = if (primary) 52.dp else 42.dp).widthIn(min = if (primary) 200.dp else 0.dp),
        fill = if (primary) soft.brandGreen else soft.surface,
        rim = !primary,
        onClick = onClick,
    ) {
        Text(
            label,
            Modifier.padding(horizontal = if (primary) 36.dp else 24.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = if (primary) 16.sp else 14.sp),
            color = if (primary) androidx.compose.ui.graphics.Color.White else soft.appPrimary,
        )
    }
}

@Composable
fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {},
) {
    val soft = MuttaqiTheme.soft
    SoftPillSurface(modifier.fillMaxWidth().height(52.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = soft.textPrimary, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(soft.appPrimary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = soft.textSecondary, textAlign = TextAlign.Center)
                    }
                    field()
                }
            },
        )
    }
}

