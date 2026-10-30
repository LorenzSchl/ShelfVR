package de.lorenzschl.shelfvr.spike

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureScreenRoboImage
import de.lorenzschl.shelfvr.core.designsystem.ShelfVrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "de-w1024dp-h640dp-mdpi")
class CleartextDialogScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun warnhinweis() {
        compose.setContent {
            ShelfVrTheme { CleartextDialog("http://192.168.1.20:13378", SpikeScreenScreenshotTest.NoActions) }
        }
        compose.waitForIdle()
        captureScreenRoboImage()
    }
}
