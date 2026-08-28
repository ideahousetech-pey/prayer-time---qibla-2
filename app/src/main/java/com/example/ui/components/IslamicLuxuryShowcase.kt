package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*

/**
 * ==========================================
 * EXPERT COMPOSE GLASSMORPHIC CARD COMPONENT
 * ==========================================
 * Represents a signature component style of the 2026 Islamic Luxury system:
 * Frosted backdrop, translucent surface variance, and high-contrast gold hairline.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerMedium,
    elevation: Dp = IslamicLuxuryElevation.ambient,
    useGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = if (useGlow) GoldPrimary else Color.Black,
                spotColor = if (useGlow) GoldPrimary else Color.Black
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GlassBackdrop,
                        GlassBackdrop.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorder,
                        GlassBorder.copy(alpha = 0.1f),
                        GlassBorder.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Specular glow overlay at the top left in 2026 layout structures
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GlassGaze, Color.Transparent),
                        radius = 200f
                    )
                )
        )
        Column(
            modifier = Modifier.padding(IslamicLuxurySpacing.normal)
        ) {
            content()
        }
    }
}

/**
 * DATA MODELS FOR SYSTEM SPECIFICATIONS VISUALIZERS
 */
data class ColorSwatch(val name: String, val color: Color, val hex: String)
data class MetricSpec(val name: String, val value: String, val desc: String)

/**
 * ====================================================
 * ISLAMIC LUXURY 2026 - DEMONSTRATION & DESIGN SYSTEM
 * ====================================================
 */
@Composable
fun IslamicLuxuryShowcase(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Swatches & Shapes, 1: Typography & Specs, 2: Sample UI Components
    val tabs = listOf("Palette & Geometric", "Typografi & Spacing", "Komponen Mewah")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepNight)
            .padding(IslamicLuxurySpacing.normal)
    ) {
        // --- Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ISLAMIC LUXURY 2026",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Spesifikasi Token, Komponen, dan Pola Desain",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CardElevated)
            ) {
                Text("✕", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(IslamicLuxurySpacing.normal))

        // --- Custom M3 Expressive Tab Row Selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerSmall))
                .background(MidnightLayer)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = index == activeTab
                val textCol = if (selected) DeepNight else TextSecondary
                val bgBrush = if (selected) {
                    Brush.horizontalGradient(listOf(GoldPrimary, GoldLight))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(CornerSmall - 2.dp))
                        .background(bgBrush)
                        .clickable { activeTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = textCol,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(IslamicLuxurySpacing.normal))

        // --- Content Frame with Slide-fade transition animation
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) + 
                 scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            label = "showcase_tab_transition"
        ) { targetState ->
            when (targetState) {
                0 -> PaletteAndShapesTab()
                1 -> TypoAndSpacingTab()
                2 -> ComponentLuxuryTab()
            }
        }
    }
}

/**
 * TAB 1: SWATCHES & PRIMARY SHAPES SHOWCASE
 */
@Composable
fun PaletteAndShapesTab() {
    val swatches = listOf(
        ColorSwatch("Deep Obsidian", DeepNight, "#030A07"),
        ColorSwatch("Velvet Medium", MidnightLayer, "#071912"),
        ColorSwatch("Satin Emerald", CardSurface, "#0C241B"),
        ColorSwatch("Luminous Jade", CardElevated, "#14362A"),
        ColorSwatch("Imperial Gold", GoldPrimary, "#E5C158"),
        ColorSwatch("Champagne H.", GoldLight, "#FDEFAF"),
        ColorSwatch("Burnished Gold", GoldDim, "#9E7E38"),
        ColorSwatch("Sydhu Teal", TealAccent, "#2EC4B6")
    )

    Column(verticalArrangement = Arrangement.spacedBy(IslamicLuxurySpacing.normal)) {
        Text(
            text = "I. Sistem Palet Warna Kekhalifahan (The Emerald Gold Palette)",
            style = MaterialTheme.typography.titleMedium,
            color = GoldLight
        )

        // Color Swatches Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                swatches.take(4).forEach { swatch ->
                    ColorSwatchRow(swatch)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                swatches.drop(4).forEach { swatch ->
                    ColorSwatchRow(swatch)
                }
            }
        }

        Divider(color = DividerLine, thickness = 1.dp)

        Text(
            text = "II. Geometri & Kelengkungan Sudut (Corner Radii)",
            style = MaterialTheme.typography.titleMedium,
            color = GoldLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(IslamicLuxurySpacing.tiny)
        ) {
            ShapeDisplayCard("XS (6dp)", RoundedCornerShape(CornerExtraSmall), Modifier.weight(1f))
            ShapeDisplayCard("S (12dp)", RoundedCornerShape(CornerSmall), Modifier.weight(1f))
            ShapeDisplayCard("M (20dp)", RoundedCornerShape(CornerMedium), Modifier.weight(1f))
            ShapeDisplayCard("Dome (40dp)", RoundedCornerShape(CornerExtraLarge), Modifier.weight(1f))
        }
    }
}

@Composable
fun ColorSwatchRow(swatch: ColorSwatch) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerSmall))
            .background(CardSurface)
            .border(0.5.dp, DividerLine, RoundedCornerShape(CornerSmall))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(swatch.color)
                .border(1.dp, GoldLight.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(swatch.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(swatch.hex, style = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 10.sp), color = TextSecondary)
        }
    }
}

@Composable
fun ShapeDisplayCard(label: String, shape: RoundedCornerShape, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardSurface, shape)
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), shape)
            .padding(12.dp)
            .height(52.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            color = GoldLight, 
            textAlign = TextAlign.Center
        )
    }
}

/**
 * TAB 2: TYPOGRAPHY SHOWCASE & SPACING SPECIFICATION
 */
@Composable
fun TypoAndSpacingTab() {
    val spacingSpecs = listOf(
        MetricSpec("Spacing Tiny", "8 dp", "Grid standar elemen kecil"),
        MetricSpec("Spacing Normal", "16 dp", "Padding standar screen/cards"),
        MetricSpec("Spacing Medium", "24 dp", "Batas vertikal antar seksi"),
        MetricSpec("Elevation Ambient", "3 dp", "Ketinggian standar Glassmorphic"),
        MetricSpec("Elevation Stellar", "16 dp", "Fokus dialog mengambang")
    )

    Column(verticalArrangement = Arrangement.spacedBy(IslamicLuxurySpacing.normal)) {
        Text(
            text = "III. Hirarki Tipografi Berpasangan (Symphony Display Style)",
            style = MaterialTheme.typography.titleMedium,
            color = GoldLight
        )

        // Type Examples
        GlassmorphicCard {
            Text(
                text = "Cinzel Heading",
                style = MaterialTheme.typography.displayMedium,
                color = GoldPrimary
            )
            Text(
                text = "Elegansi kaligrafi klasik dengan proporsi monumental kekaisaran.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Divider(color = DividerLine, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nunito Sans-Serif Body Text",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = "Struktur huruf bulat natural yang nyaman untuk membaca teks panjang dalam jangka waktu lama secara terus-menerus.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Divider(color = DividerLine, thickness = 1.dp)

        Text(
            text = "IV. Skala Spasi & Bidang Kedalaman (M3)",
            style = MaterialTheme.typography.titleMedium,
            color = GoldLight
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            spacingSpecs.forEach { spec ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(spec.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(spec.desc, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                    Text(
                        text = spec.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * TAB 3: ROYAL COMPONENTS STYLING DEMO
 */
@Composable
fun ComponentLuxuryTab() {
    var rippleCount by remember { mutableStateOf(0) }
    var scaleButton by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(IslamicLuxurySpacing.normal)) {
        Text(
            text = "V. Implementasi Komponen Berkarakter (The Premium UI)",
            style = MaterialTheme.typography.titleMedium,
            color = GoldLight
        )

        // 1. Double Glassmorphic Card
        GlassmorphicCard(
            useGlow = true,
            elevation = IslamicLuxuryElevation.stellar
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Royal Icon",
                    tint = GoldPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(bottom = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MAHKOTA SPIRITUAL",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Efek berpendar emas lembut (Gold Ambient Glow)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Komponen ini memanfaatkan pencahayaan radial transparan di sudut atas untuk memberikan pantulan kristal pada permukaan gelap.",
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
        }

        // 2. Translucent Gold Interactive Button with Spring Physics
        val buttonScale by animateFloatAsState(
            targetValue = if (scaleButton) 0.95f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "interactive_physics_button"
        )

        Button(
            onClick = {
                rippleCount++
                scaleButton = !scaleButton
            },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(CornerMedium))
                .clip(RoundedCornerShape(CornerMedium))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GoldPrimary, GoldLight)
                    )
                )
                .clickable {
                    rippleCount++
                },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Text(
                text = "Sentuh Tombol Mewah ($rippleCount)",
                style = MaterialTheme.typography.titleMedium,
                color = DeepNight,
                fontWeight = FontWeight.Bold
            )
        }

        // 3. Info Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerSmall))
                .background(TealDim.copy(alpha = 0.3f))
                .border(0.5.dp, TealAccent.copy(alpha = 0.4f), RoundedCornerShape(CornerSmall))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Jade Info",
                tint = TealAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sistem 2026 secara otomatis meremajakan kerangka kerja Material 3 demi visual berkelas tinggi tanpa penurunan performa rendering sedikit pun.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
