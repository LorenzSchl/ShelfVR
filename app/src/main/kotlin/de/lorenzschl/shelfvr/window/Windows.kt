package de.lorenzschl.shelfvr.window

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.media3.common.Player
import de.lorenzschl.shelfvr.MainActivity
import de.lorenzschl.shelfvr.MiniPlayerActivity
import kotlin.math.roundToInt

/** Startet Fenster als eigene Panels neben dem aktuellen (PROJECT_SPEC.md §5.3). */
object Windows {

    fun miniPlayer(context: Context): Intent =
        Intent(context, MiniPlayerActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK,
        )

    // Ohne MULTIPLE_TASK: ein bestehendes Hauptfenster wird wiederverwendet statt verdoppelt.
    fun main(context: Context): Intent =
        Intent(context, MainActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK,
        )
}

/** Aktuelle Fenstergröße in dp, für die Auswertung von Spike S-2. */
@Composable
fun windowSizeLabel(): String {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return with(density) { "${size.width.toDp().value.roundToInt()} × ${size.height.toDp().value.roundToInt()} dp" }
}

fun Player.togglePlayPause() {
    if (isPlaying) {
        pause()
    } else {
        if (playbackState == Player.STATE_IDLE) prepare()
        play()
    }
}
