package de.lorenzschl.shelfvr.feature.miniplayer

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import de.lorenzschl.shelfvr.core.designsystem.ShelfVrTheme
import de.lorenzschl.shelfvr.core.model.PlaybackSnapshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Fenstergröße als Annahme für die Mini-Player-Leiste; die reale Mindestgröße klärt Spike S-2. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "de-w640dp-h120dp-xhdpi")
class MiniPlayerBarScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun spielt() = capture(
        PlaybackSnapshot(
            title = "Die unendliche Geschichte – ein sehr langer Titel, der abgeschnitten wird",
            subtitle = "Michael Ende",
            isPlaying = true,
            positionMs = 4_512_000,
            durationMs = 44_100_000,
        ),
    )

    @Test
    fun leer() = capture(PlaybackSnapshot.Empty)

    private fun capture(snapshot: PlaybackSnapshot) {
        compose.setContent {
            ShelfVrTheme { MiniPlayerBar(snapshot, onPlayPause = {}, onSkipBack = {}, onSkipForward = {}, onExpand = {}) }
        }
        compose.onRoot().captureRoboImage()
    }
}
