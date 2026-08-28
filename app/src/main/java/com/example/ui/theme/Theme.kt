package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

/**
 * ==========================================
 * IMPERIAL ISLAMIC LUXURY 2026 COMPOSE THEME
 * ==========================================
 *
 * Integrates:
 * - Malachite Emerald & Saffron Gold Color Tokens
 * - Sculpted Imperial Geometric Shapes and Corners
 * - Spacious calligraphic & modern legible typography pairings
 */

private val AppColorScheme = darkColorScheme(
    primary            = StaticGoldPrimary,
    onPrimary          = StaticDeepNight,
    primaryContainer   = StaticGoldGlow,
    onPrimaryContainer = StaticGoldLight,
    secondary          = StaticTealAccent,
    onSecondary        = StaticDeepNight,
    secondaryContainer = StaticTealDim,
    onSecondaryContainer = StaticTextPrimary,
    tertiary           = StaticGoldLight,
    onTertiary         = StaticDeepNight,
    background         = StaticDeepNight,
    onBackground       = StaticTextPrimary,
    surface            = StaticCardSurface,
    onSurface          = StaticTextPrimary,
    surfaceVariant     = StaticCardElevated,
    onSurfaceVariant   = StaticTextSecondary,
    outline            = StaticDividerLine,
    error              = StaticErrorRed,
    onError            = StaticDeepNight
)

private val AppLightColorScheme = lightColorScheme(
    primary            = Color(0xFFC29D38),
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFFAF9F6),
    onPrimaryContainer = Color(0xFF826315),
    secondary          = Color(0xFF008D80),
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4FAF5),
    onSecondaryContainer = Color(0xFF0C241B),
    tertiary           = Color(0xFF6E5311),
    onTertiary         = Color(0xFFFFFFFF),
    background         = Color(0xFFFAF9F6),
    onBackground       = Color(0xFF0C241B),
    surface            = Color(0xFFFFFFFF),
    onSurface          = Color(0xFF0C241B),
    surfaceVariant     = Color(0xFFFAF9F6),
    onSurfaceVariant   = Color(0xFF426354),
    outline            = Color(0xFFEDEAE1),
    error              = Color(0xFFDC2626),
    onError            = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "dark",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemIsDark
    }
    
    // Sync state safely using SideEffect after successful composition
    androidx.compose.runtime.SideEffect {
        AppThemeState.updateTheme(themeMode, systemIsDark)
    }

    val colors = if (darkTheme) {
        AppColorScheme
    } else {
        AppLightColorScheme
    }

    // Initialize custom font loading fallbacks synchronously before measurement and layout
    val context = LocalContext.current
    remember(context) {
        FontFallbackManager.initialize(context)
        true
    }

    // 2026 Material 3 Expressive Engine Activation
    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}

/**
 * Centralized background gradient for the application.
 * Dynamic and re-composes automatically on theme changes.
 */
val AppBackgroundGradient: Brush
    @Composable
    get() = rememberAppBackgroundBrush()

/**
 * Composable function to construct and cache AppBackground Brush
 */
@Composable
fun rememberAppBackgroundBrush(): Brush {
    val isDark = AppThemeState.isDarkTheme
    return remember(isDark) {
        val topColor = if (isDark) StaticDeepNight else Color(0xFFFAF9F6)
        val bottomColor = if (isDark) StaticMidnightLayer else Color(0xFFF4F1EA)
        Brush.verticalGradient(listOf(topColor, bottomColor))
    }
}

/**
 * GPU-optimized cached repeating Islamic diamond background modifier.
 * Avoids any per-frame object allocation and utilizes hardware acceleration.
 */
@Composable
fun Modifier.islamicBackground(alpha: Float? = null): Modifier {
    val backgroundBrush = rememberAppBackgroundBrush()
    val isDark = AppThemeState.isDarkTheme
    val patternAlpha = alpha ?: (if (isDark) 0.04f else 0.08f)
    val patternColor = if (isDark) StaticGoldPrimary else Color(0xFFC29D38)

    return this
        .background(backgroundBrush)
        .drawWithCache {
            val sizePx = 60.dp.toPx()
            val cols = (size.width / sizePx).toInt() + 1
            val rows = (size.height / sizePx).toInt() + 1
            val combinedPath = Path()
            
            for (col in 0..cols) {
                for (row in 0..rows) {
                    val x = col * sizePx
                    val y = row * sizePx
                    combinedPath.moveTo(x + sizePx / 2, y)
                    combinedPath.lineTo(x + sizePx, y + sizePx / 2)
                    combinedPath.lineTo(x + sizePx / 2, y + sizePx)
                    combinedPath.lineTo(x, y + sizePx / 2)
                    combinedPath.close()
                }
            }
            
            onDrawBehind {
                drawPath(
                    path = combinedPath,
                    color = patternColor.copy(alpha = patternAlpha),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
}

