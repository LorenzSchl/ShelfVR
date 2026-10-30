package de.lorenzschl.shelfvr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.lorenzschl.shelfvr.core.designsystem.ShelfVrTheme
import de.lorenzschl.shelfvr.core.model.PlaybackSnapshot
import de.lorenzschl.shelfvr.feature.miniplayer.MiniPlayerBar
import de.lorenzschl.shelfvr.playback.PlaybackClient
import de.lorenzschl.shelfvr.window.Windows
import de.lorenzschl.shelfvr.window.togglePlayPause
import de.lorenzschl.shelfvr.window.windowSizeLabel

/** Mini-Player als eigenes Panel (Variante A, PROJECT_SPEC.md §7.6). Funktioniert ohne Hauptfenster. */
class MiniPlayerActivity : ComponentActivity() {

    private val playback by lazy { PlaybackClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShelfVrTheme {
                val snapshot by playback.snapshots.collectAsStateWithLifecycle(PlaybackSnapshot.Empty)
                Box(modifier = Modifier.fillMaxSize()) {
                    MiniPlayerBar(
                        snapshot = snapshot,
                        onPlayPause = { playback.controller.value?.togglePlayPause() },
                        onSkipBack = { playback.controller.value?.seekBack() },
                        onSkipForward = { playback.controller.value?.seekForward() },
                        onExpand = {
                            startActivity(Windows.main(this@MiniPlayerActivity))
                            finish()
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    // Nur für Spike S-2: tatsächliche Panelgröße ablesen.
                    Text(
                        windowSizeLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 20.dp, bottom = 4.dp),
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        playback.connect()
    }

    override fun onStop() {
        playback.disconnect()
        super.onStop()
    }
}
