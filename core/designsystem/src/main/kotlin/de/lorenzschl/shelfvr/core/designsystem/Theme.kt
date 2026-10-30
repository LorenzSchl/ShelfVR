package de.lorenzschl.shelfvr.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Vorläufiges Theme (Material 3, dunkel). Ob es beim Material 3 bleibt oder das Horizon OS UI Set
 * verwendet wird, entscheidet sich nach Spike S-1 (PROJECT_SPEC.md §13 Punkt 4).
 */
@Composable
fun ShelfVrTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ShelfColors, typography = ShelfTypography, content = content)
}

/** Maße nach NFR-01: Hit-Targets ≥ 56 dp, großzügige Abstände für Ray/Pinch. */
object ShelfDimens {
    val MinTouchTarget = 56.dp
    val ScreenPadding = 32.dp
    val ItemSpacing = 16.dp
}

private val ShelfColors = darkColorScheme(
    primary = Color(0xFFF2B866),
    onPrimary = Color(0xFF2A1800),
    primaryContainer = Color(0xFF5C3F12),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = Color(0xFFD9C3A9),
    onSecondary = Color(0xFF3B2E1C),
    secondaryContainer = Color(0xFF4A3D2C),
    onSecondaryContainer = Color(0xFFF5DFC4),
    tertiary = Color(0xFFB5CEA0),
    onTertiary = Color(0xFF213617),
    background = Color(0xFF17130F),
    onBackground = Color(0xFFF0E6DC),
    surface = Color(0xFF17130F),
    onSurface = Color(0xFFF0E6DC),
    surfaceVariant = Color(0xFF3A322A),
    onSurfaceVariant = Color(0xFFE0D3C5),
    surfaceContainerLowest = Color(0xFF110E0B),
    surfaceContainerLow = Color(0xFF1E1914),
    surfaceContainer = Color(0xFF241E18),
    surfaceContainerHigh = Color(0xFF2E2720),
    surfaceContainerHighest = Color(0xFF39312A),
    outline = Color(0xFFBFAF9E),
    outlineVariant = Color(0xFF5A4F44),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

// Fließtext ≥ 16 sp laut NFR-01; hier bewusst größer, weil Panels oft auf Abstand stehen.
private val ShelfTypography = Typography(
    displaySmall = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 22.sp, lineHeight = 30.sp),
    bodyMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodySmall = TextStyle(fontSize = 18.sp, lineHeight = 24.sp),
    labelLarge = TextStyle(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
)
