package de.lorenzschl.shelfvr.feature.miniplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import de.lorenzschl.shelfvr.core.designsystem.ShelfIconButton
import de.lorenzschl.shelfvr.core.designsystem.ShelfLabelButton
import de.lorenzschl.shelfvr.core.model.PlaybackSnapshot

/**
 * Kompakte Player-Leiste (FR-51, vorläufig). Kapitelinfos und Kapitelsprünge folgen mit M3,
 * sobald Spike S-2 die Fenstervariante festgelegt hat.
 */
@Composable
fun MiniPlayerBar(
    snapshot: PlaybackSnapshot,
    onPlayPause: () -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    /** `null` blendet den Button aus, z. B. wenn die Leiste im Hauptfenster steckt. */
    onExpand: (() -> Unit)?,
    modifier: Modifier = Modifier,
    skipBackSeconds: Int = 15,
    skipForwardSeconds: Int = 30,
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = snapshot.title ?: stringResource(R.string.miniplayer_nothing_playing),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (snapshot.hasMedia) {
                    Text(
                        text = formatPosition(snapshot.positionMs, snapshot.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    LinearProgressIndicator(
                        progress = {
                            if (snapshot.durationMs > 0) snapshot.positionMs.toFloat() / snapshot.durationMs else 0f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        drawStopIndicator = {},
                    )
                }
            }
            ShelfLabelButton(
                "−$skipBackSeconds",
                pluralStringResource(R.plurals.miniplayer_skip_back, skipBackSeconds, skipBackSeconds),
                onSkipBack,
                enabled = snapshot.hasMedia,
            )
            ShelfIconButton(
                if (snapshot.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                stringResource(if (snapshot.isPlaying) R.string.miniplayer_pause else R.string.miniplayer_play),
                onPlayPause,
                enabled = snapshot.hasMedia,
            )
            ShelfLabelButton(
                "+$skipForwardSeconds",
                pluralStringResource(R.plurals.miniplayer_skip_forward, skipForwardSeconds, skipForwardSeconds),
                onSkipForward,
                enabled = snapshot.hasMedia,
            )
            if (onExpand != null) {
                ShelfIconButton(Icons.AutoMirrored.Filled.OpenInNew, stringResource(R.string.miniplayer_expand), onExpand)
            }
        }
    }
}

internal fun formatPosition(positionMs: Long, durationMs: Long): String =
    "${formatTime(positionMs)} / ${formatTime(durationMs)}"

internal fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format(Locale.ROOT, "%d:%02d:%02d", h, m, s) else String.format(Locale.ROOT, "%d:%02d", m, s)
}
