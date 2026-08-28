package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * ==========================================
 * DESIGN TOKENS: SHAPES, CORNERS & METRICS
 * ==========================================
 *
 * Implements architectural geometries and elegant arabesque curves.
 */

// --- Corner Radii Tokens
val CornerExtraSmall = 6.dp
val CornerSmall      = 12.dp
val CornerMedium     = 20.dp   // Main Standard Card Corner (Luxuriously softened)
val CornerLarge      = 28.dp   // Sheets, Drawers, and Dialog structures
val CornerExtraLarge = 40.dp   // Dynamic Islamic Dome Top curved alignments

// --- Reusable Shapes
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(CornerExtraSmall),
    small      = RoundedCornerShape(CornerSmall),
    medium     = RoundedCornerShape(CornerMedium),
    large      = RoundedCornerShape(CornerLarge),
    extraLarge = RoundedCornerShape(CornerExtraLarge)
)

/**
 * ==========================================
 * DESIGN TOKENS: SPACING GRID (Material 3 Density)
 * ==========================================
 */
object IslamicLuxurySpacing {
    val nano   = 2.dp
    val micro  = 4.dp
    val tiny   = 8.dp     // Standard 8dp grid step
    val small  = 12.dp
    val normal = 16.dp    // Default padding for cards & screens
    val medium = 24.dp    // Section separations
    val large  = 32.dp    // Header spacing
    val giant  = 48.dp    // Splash/Hero spacer
}

/**
 * ==========================================
 * DESIGN TOKENS: ELEVATION LAYERS (Material 3 Expressive)
 * ==========================================
 */
object IslamicLuxuryElevation {
    val none    = 0.dp
    val flat    = 1.dp    // Flat but visible hairline borders
    val ambient = 3.dp    // Gentle background card separation
    val normal  = 8.dp    // Interactive standard card shadow
    val stellar = 16.dp   // Majestic float / Dialog elevation
    val aura    = 24.dp   // Extreme highlighted focal points
}
