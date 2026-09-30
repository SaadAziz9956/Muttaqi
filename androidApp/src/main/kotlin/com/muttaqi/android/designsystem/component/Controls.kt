package com.muttaqi.android.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.muttaqi.android.R
import com.muttaqi.android.designsystem.MuttaqiTheme

/** A floating search pill with a clear button while there's a query */
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

/** A round floating button with an Iconsax icon; [filled] is the brand green for the main action */
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

/** A floating chip: white with a rim, or the brand green when selected */
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
