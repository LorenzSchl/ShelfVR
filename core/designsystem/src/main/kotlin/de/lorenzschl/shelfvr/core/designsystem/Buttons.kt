package de.lorenzschl.shelfvr.core.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val ButtonPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)

@Composable
fun ShelfButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = ButtonPadding,
        modifier = modifier.sizeIn(minWidth = ShelfDimens.MinTouchTarget, minHeight = ShelfDimens.MinTouchTarget),
    ) { Text(text) }
}

@Composable
fun ShelfOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = ButtonPadding,
        modifier = modifier.sizeIn(minWidth = ShelfDimens.MinTouchTarget, minHeight = ShelfDimens.MinTouchTarget),
    ) { Text(text) }
}

/** Runder Icon-Button; [contentDescription] ist Pflicht (NFR-06, „Look and Pinch“). */
@Composable
fun ShelfIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(ShelfDimens.MinTouchTarget + 8.dp),
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(32.dp))
    }
}

/** Runder Button mit kurzem Text statt Icon, z. B. „−15“ für einstellbare Sprungweiten. */
@Composable
fun ShelfLabelButton(
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(ShelfDimens.MinTouchTarget + 8.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clearAndSetSemantics {})
    }
}
