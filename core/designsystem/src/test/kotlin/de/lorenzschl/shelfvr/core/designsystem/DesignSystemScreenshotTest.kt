package de.lorenzschl.shelfvr.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "de-w800dp-h600dp-xhdpi")
class DesignSystemScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun katalog() {
        compose.setContent {
            ShelfVrTheme {
                Surface {
                    Column(
                        modifier = Modifier.padding(ShelfDimens.ScreenPadding),
                        verticalArrangement = Arrangement.spacedBy(ShelfDimens.ItemSpacing),
                    ) {
                        Text("Überschrift", style = MaterialTheme.typography.headlineMedium)
                        Text("Titel eines Hörbuchs", style = MaterialTheme.typography.titleLarge)
                        Text("Fließtext für längere Beschreibungen, gut lesbar aus Armlänge.")
                        Text("Nebeninfo · 12:34 von 8:15:00", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(ShelfDimens.ItemSpacing)) {
                            ShelfButton("Abspielen", onClick = {})
                            ShelfOutlinedButton("Zurück", onClick = {})
                            ShelfButton("Deaktiviert", onClick = {}, enabled = false)
                            ShelfIconButton(Icons.Filled.PlayArrow, contentDescription = "Abspielen", onClick = {})
                        }
                    }
                }
            }
        }
        compose.onRoot().captureRoboImage()
    }
}
