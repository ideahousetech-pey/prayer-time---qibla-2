package id.ideahousetech.prayertime_qibla.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf

/**
 * ==========================================
 * DESIGN TOKENS: ISLAMIC LUXURY 2026 PALETTE
 * ==========================================
 *
 * An opulent, deeply spiritual design system combining the royal legacy of
 * Islamic architecture with ultra-modern 2026 Glassmorphism and Material 3 Expressiveness.
 *
 * Base Mood: Imperial Obsidian & Frosted Emerald Velvet (Dark Type)
 *            Alabaster Marble & Imperial Gold Saffron (Light Type)
 */

// --- GLOBAL THEME COORDINATOR (Thread-safe, Compile-safe dynamic switchover)
object AppThemeState {
    private val _isDarkTheme = androidx.compose.runtime.mutableStateOf(true)
    var isDarkTheme: Boolean
        get() = _isDarkTheme.value
        set(value) {
            _isDarkTheme.value = value
        }
    
    val currentThemeMode = androidx.compose.runtime.mutableStateOf("dark")

    @Synchronized
    fun updateTheme(mode: String, systemIsDark: Boolean) {
        currentThemeMode.value = mode
        isDarkTheme = when (mode) {
            "light" -> false
            "dark" -> true
            else -> systemIsDark
        }
    }
}

// --- STATIC UNDERLYING TOKENS FOR SOLID CONSTANT REFERENCE
val StaticDeepNight        = Color(0xFF030A07)   // Imperial Obsidian
val StaticMidnightLayer    = Color(0xFF071912)   // Velvet Malachite
val StaticCardSurface      = Color(0xFF0C241B)   // Satin Emerald
val StaticCardElevated     = Color(0xFF14362A)   // Luminous Verdue

val StaticGoldPrimary      = Color(0xFFE5C158)   // Imperial Gold
val StaticGoldLight        = Color(0xFFFDEFAF)   // Champagne Gold
val StaticGoldDim          = Color(0xFF9E7E38)   // Antique Burnished Gold
val StaticGoldGlow         = Color(0x22E5C158)   // Translucent Gold Glaze

val StaticTealAccent       = Color(0xFF2EC4B6)   // Mystic Turquoise
val StaticTealDim          = Color(0xFF0A5C54)   // Deep Jade Patina
val StaticDarkTeal         = Color(0xFF0B4237)   // Royal Forest Teal

val StaticTextPrimary      = Color(0xFFF5FCF8)   // Pristine Pearl Silk
val StaticTextSecondary    = Color(0xFFA5C5B5)   // Polished Mint
val StaticTextMuted        = Color(0xFF537A68)   // Velvet Shadow

val StaticGlassBackdrop    = Color(0x1F0C241B)   // Ultrathin Frosted Acrylic Overlay
val StaticGlassBorder      = Color(0x3DFDEFAF)   // Ultraprecise 2026 Hairline Golden Frame
val StaticDividerLine      = Color(0x1DF5FCF8)   // Ultra-light silk divider
val StaticErrorRed         = Color(0xFFF87171)   // Vibrant Crimson Rose

// --- ACTIVE THEME DYNAMIC GETTERS (AUTO-RECOMPOSED ACROSS EVERY SCREEN SAFELY)
val DeepNight: Color
    get() = if (AppThemeState.isDarkTheme) StaticDeepNight else Color(0xFFFAF9F6)

val MidnightLayer: Color
    get() = if (AppThemeState.isDarkTheme) StaticMidnightLayer else Color(0xFFF4F1EA)

val CardSurface: Color
    get() = if (AppThemeState.isDarkTheme) StaticCardSurface else Color(0xFFFFFFFF)

val CardElevated: Color
    get() = if (AppThemeState.isDarkTheme) StaticCardElevated else Color(0xFFFAF8F5)

val GoldPrimary: Color
    get() = if (AppThemeState.isDarkTheme) StaticGoldPrimary else Color(0xFFC29D38)

val GoldLight: Color
    get() = if (AppThemeState.isDarkTheme) StaticGoldLight else Color(0xFF826315)

val GoldDim: Color
    get() = if (AppThemeState.isDarkTheme) StaticGoldDim else Color(0xFF6E5311)

val GoldGlow: Color
    get() = if (AppThemeState.isDarkTheme) StaticGoldGlow else Color(0x11C29D38)

val TealAccent: Color
    get() = if (AppThemeState.isDarkTheme) StaticTealAccent else Color(0xFF008D80)

val TealDim: Color
    get() = if (AppThemeState.isDarkTheme) StaticTealDim else Color(0xFFD4FAF5)

val DarkTeal: Color
    get() = if (AppThemeState.isDarkTheme) StaticDarkTeal else Color(0xFF0F5E4E)

val TextPrimary: Color
    get() = if (AppThemeState.isDarkTheme) StaticTextPrimary else Color(0xFF0C241B)

val TextSecondary: Color
    get() = if (AppThemeState.isDarkTheme) StaticTextSecondary else Color(0xFF426354)

val TextMuted: Color
    get() = if (AppThemeState.isDarkTheme) StaticTextMuted else Color(0xFF809C90)

val GlassBackdrop: Color
    get() = if (AppThemeState.isDarkTheme) StaticGlassBackdrop else Color(0x2BFAF9F6)

val GlassBorder: Color
    get() = if (AppThemeState.isDarkTheme) StaticGlassBorder else Color(0x4EC29D38)

val GlassGaze: Color
    get() = if (AppThemeState.isDarkTheme) Color(0x12FFFFFF) else Color(0x06FFFFFF)

val SuccessGreen: Color
    get() = if (AppThemeState.isDarkTheme) Color(0xFF34D399) else Color(0xFF059669)

val WarningAmber: Color
    get() = if (AppThemeState.isDarkTheme) Color(0xFFFB923C) else Color(0xFFD97706)

val DividerLine: Color
    get() = if (AppThemeState.isDarkTheme) StaticDividerLine else Color(0xFFEDEAE1)

val ResetRed: Color
    get() = if (AppThemeState.isDarkTheme) Color(0xFF6B1A1A) else Color(0xFF991B1B)

val ErrorRed: Color
    get() = if (AppThemeState.isDarkTheme) StaticErrorRed else Color(0xFFDC2626)


