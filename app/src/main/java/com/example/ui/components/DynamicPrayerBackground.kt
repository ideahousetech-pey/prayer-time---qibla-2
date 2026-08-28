package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import id.ideahousetech.prayertime_qibla.ui.theme.GoldPrimary
import kotlin.math.cos
import kotlin.math.sin

/**
 * =========================================================================
 * DYNAMIC PRAYER BACKGROUND SYSTEM (Islamic Luxury 2026 Core Engine)
 * =========================================================================
 * Dynamically adapts the background canvas theme corresponding directly to 
 * the 5 Daily Prayer times (Subuh, Dzuhur, Ashar, Maghrib, Isya).
 * Uses smooth, infinite animators, floating particles, solar flares,
 * and high-fidelity gradient interpolations in Jetpack Compose.
 */
@Composable
fun DynamicPrayerBackground(
    prayerName: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val cleanPrayer = prayerName.uppercase().trim()

    // 1. DEFINING LUXURY CORE COLOR SCHEMES FOR THE 5 PRAYERS
    val (targetTop, targetMid, targetBot) = when (cleanPrayer) {
        "SUBUH" -> Triple(
            Color(0xFF030D1B), // Fajr twilight deep navy
            Color(0xFF2E1C4E), // Misty morning lavender sky
            Color(0xFFE4A467)  // Soft aurora sunrise amber
        )
        "DZUHUR" -> Triple(
            Color(0xFF013A63), // Royal zenith sapphire
            Color(0xFF2C7DA0), // Turquoise high noon horizon
            Color(0xFFFFEAAB)  // Saffron gold crowning radiance
        )
        "ASHAR" -> Triple(
            Color(0xFF2C1307), // Bronze dusk mahogany
            Color(0xFF9E420C), // Golden hour ochre saffron
            Color(0xFFE4AD5B)  // Gilded brass horizon highlight
        )
        "MAGHRIB" -> Triple(
            Color(0xFF140722), // Crimson obsidian dusk
            Color(0xFF4C0834), // Royal Persian wine dark
            Color(0xFFFF4040)  // Setting sun burning crimson gold
        )
        "ISYA" -> Triple(
            Color(0xFF02040F), // Extreme star diamond abyss
            Color(0xFF0C1446), // Royal indigo
            Color(0xFF022B1B)  // Velvet Malachite Green
        )
        else -> Triple(
            Color(0xFF030A07), // Default app backdrop top
            Color(0xFF071912), // Default app backdrop mid
            Color(0xFF000503)  // Default app backdrop bot
        )
    }

    // 2. INTERPOLATING GRADIENT COLOR CORES VIA ANIMATED TRANSITION SCHEMES
    val topAnimate by animateColorAsState(
        targetValue = targetTop,
        animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
        label = "bgTop"
    )
    val midAnimate by animateColorAsState(
        targetValue = targetMid,
        animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
        label = "bgMid"
    )
    val botAnimate by animateColorAsState(
        targetValue = targetBot,
        animationSpec = tween(durationMillis = 1800, easing = LinearOutSlowInEasing),
        label = "bgBot"
    )

    // 3. INFINITE BEAT ENGINE & ROTATOR CHANNELS
    val infiniteTransition = rememberInfiniteTransition(label = "dynamicPrayerBackground")
    
    // Slow rotational degree for celestial sun shafts / stars
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotator"
    )

    // Gentle vertical float wave offset
    val floatingWaves by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveOffset"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Base Dynamic Gradient Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topAnimate, midAnimate, botAnimate)
                )
            )
        }

        // Custom Overlay Interactive Artworks per Prayer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when (cleanPrayer) {
                "SUBUH" -> {
                    // Fajr: Dew-glow mist drifting upwards
                    val pulseRadius = 140.dp.toPx() + floatingWaves * 1.5f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x2CE4A467), Color.Transparent),
                            center = Offset(w / 2f, h * 0.9f)
                        ),
                        radius = pulseRadius,
                        center = Offset(w / 2f, h * 0.9f)
                    )

                    // Floating fine dew drop coordinates
                    val offsets = listOf(
                        Offset(w * 0.15f, h * 0.4f - floatingWaves),
                        Offset(w * 0.35f, h * 0.6f - floatingWaves * 0.8f),
                        Offset(w * 0.55f, h * 0.3f - floatingWaves * 1.2f),
                        Offset(w * 0.8f, h * 0.5f - floatingWaves * 0.5f)
                    )
                    offsets.forEach { pt ->
                        drawCircle(
                            color = Color(0x66FFE3B0),
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    }
                }
                "DZUHUR" -> {
                    // Zuhur: Blazing High-Noon sun shafts spinning at the top
                    val sunCenter = Offset(w / 2f, h * 0.15f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x3BFFFFEA), Color.Transparent),
                            center = sunCenter
                        ),
                        radius = 200.dp.toPx() * pulseGlow,
                        center = sunCenter
                    )

                    // Draw golden solar core
                    drawCircle(
                        color = Color(0x7DFFDF88),
                        radius = 42.dp.toPx(),
                        center = sunCenter
                    )

                    // Draw rotating sunbeams
                    rotate(degrees = rotationAngle, pivot = sunCenter) {
                        for (i in 0 until 8) {
                            val rad = (i * 45 * (kotlin.math.PI / 180.0)).toFloat()
                            val length = 120.dp.toPx()
                            val endX = sunCenter.x + length * cos(rad)
                            val endY = sunCenter.y + length * sin(rad)
                            
                            drawLine(
                                color = Color(0x1AFFE5B4),
                                start = sunCenter,
                                end = Offset(endX, endY),
                                strokeWidth = 8.dp.toPx()
                            )
                        }
                    }
                }
                "ASHAR" -> {
                    // Ashar: Horizon copper waves flowing
                    val wavePath1 = Path().apply {
                        val startY = h * 0.85f + floatingWaves
                        moveTo(0f, startY)
                        cubicTo(
                            w / 3f, startY - 30.dp.toPx(),
                            w * 2f / 3f, startY + 50.dp.toPx(),
                            w, startY - 10.dp.toPx()
                        )
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    val wavePath2 = Path().apply {
                        val startY = h * 0.88f - floatingWaves * 0.7f
                        moveTo(0f, startY)
                        cubicTo(
                            w / 4f, startY + 40.dp.toPx(),
                            w * 3f / 4f, startY - 40.dp.toPx(),
                            w, startY + 20.dp.toPx()
                        )
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }

                    drawPath(
                        path = wavePath1,
                        color = Color(0x1ADB752D)
                    )
                    drawPath(
                        path = wavePath2,
                        color = Color(0x2CE4AD5B)
                    )
                }
                "MAGHRIB" -> {
                    // Maghrib: Fading disk of local sunset
                    val duskCenter = Offset(w / 2f, h * 0.75f + floatingWaves * 0.4f)
                    
                    // Giant solar embers reflecting on the sky
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x3BFF5252), Color.Transparent),
                            center = duskCenter
                        ),
                        radius = 280.dp.toPx(),
                        center = duskCenter
                    )

                    // Setting red-orange disk
                    drawCircle(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x9EFE8F35), Color.Transparent),
                            startY = duskCenter.y - 120f,
                            endY = duskCenter.y + 120f
                        ),
                        radius = 80.dp.toPx(),
                        center = duskCenter
                    )
                }
                "ISYA" -> {
                    // Isya: Sparkling 8-point stars blinking in night obsidian
                    val stars = listOf(
                        Offset(w * 0.12f, h * 0.18f) to 1.0f,
                        Offset(w * 0.28f, h * 0.32f) to 0.4f,
                        Offset(w * 0.45f, h * 0.08f) to 0.7f,
                        Offset(w * 0.62f, h * 0.25f) to 0.5f,
                        Offset(w * 0.85f, h * 0.15f) to 0.9f,
                        Offset(w * 0.78f, h * 0.42f) to 0.3f,
                        Offset(w * 0.38f, h * 0.55f) to 0.6f
                    )

                    stars.forEach { (pos, intensity) ->
                        val localAlpha = (pulseGlow * intensity).coerceIn(0.1f, 1.0f)
                        val sizeMultiplier = 6.dp.toPx() * intensity

                        // Drawing 8-point tiny shining stars
                        for (j in 0 until 4) {
                            val angle = j * 45f
                            rotate(degrees = angle, pivot = pos) {
                                drawLine(
                                    color = Color.White.copy(alpha = localAlpha),
                                    start = Offset(pos.x - sizeMultiplier, pos.y),
                                    end = Offset(pos.x + sizeMultiplier, pos.y),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. EMBEDDED DUST ACCENTS LAYER (Subtle Luxury Sparkles)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 160.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                val factor = if (x % 2 == 0) 1f else -1f
                val py = (size.height * 0.3f) + (floatingWaves * 2f * factor) + (x * 40f)
                val px = x * step + 40f
                if (px < size.width && py < size.height) {
                    drawCircle(
                        color = GoldPrimary.copy(alpha = 0.04f * pulseGlow),
                        radius = 2.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }

        // Render main content
        content()
    }
}
