package de.lorenzschl.shelfvr.spike

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

/** Breite = Default-Breite des Hauptfensters (1024 dp); Höhe so gewählt, dass alles sichtbar ist. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "de-w1024dp-h1640dp-mdpi")
class SpikeScreenScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun angemeldetUndSpielt() {
        compose.setContent {
            ShelfVrTheme {
                SpikeScreen(
                    windowSize = "1024 × 640 dp",
                    snapshot = PlaybackSnapshot("Multi-MP3 Buch", "Test Autor", true, 42_000, 300_000),
                    s3 = S3UiState(
                        serverUrl = "https://abs.example.org",
                        username = "lorenz",
                        status = "Angemeldet als lorenz · ABS 2.37.1 · Testbücher",
                        playing = "Multi-MP3 Buch · 3 Track(s) · playMethod 0 · Start bei 0 s",
                        lastSync = "Sync 21:34:05 · Buchzeit 42 s · gehört 15 s",
                        items = listOf(
                            S3Item("1", "M4B mit Kapiteln", "Test Autor", 1),
                            S3Item("2", "Multi-MP3 Buch", "Test Autor", 3),
                            S3Item("3", "Ohne Tags & Sonderzeichen", "Ünïcödé Äutor", 3),
                        ),
                    ),
                    actions = NoActions,
                )
            }
        }
        compose.onRoot().captureRoboImage()
    }

    private object NoActions : SpikeActions {
        override fun playTestTone() = Unit
        override fun stop() = Unit
        override fun requestNotifications() = Unit
        override fun openMiniPlayer(closeMainWindow: Boolean) = Unit
        override fun playPause() = Unit
        override fun skipBack() = Unit
        override fun skipForward() = Unit
        override fun onServerUrlChange(value: String) = Unit
        override fun onUsernameChange(value: String) = Unit
        override fun onPasswordChange(value: String) = Unit
        override fun login() = Unit
        override fun playItem(id: String) = Unit
        override fun closeSession() = Unit
    }
}
