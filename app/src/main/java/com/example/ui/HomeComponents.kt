package id.ideahousetech.prayertime_qibla.ui

import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.calculatePrayerProgress

/**
 * ==========================================
 * EXPERT COMPOSE LUXURY GLASSMORPHIC CARD
 * ==========================================
 * Employs deep velvet-malachite glass overlay with glowing gold hairline borders.
 */
@Composable
fun IslamicGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerMedium,
    elevation: Dp = IslamicLuxuryElevation.normal,
    useGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = if (useGlow) GoldPrimary.copy(alpha = 0.3f) else Color.Black,
                spotColor = if (useGlow) GoldPrimary.copy(alpha = 0.5f) else Color.Black
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CardSurface.copy(alpha = 0.95f),
                        MidnightLayer.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBorder.copy(alpha = 0.4f),
                        GlassBorder.copy(alpha = 0.1f),
                        GoldDim.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Specular ambient glare
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GlassGaze.copy(alpha = 0.08f), Color.Transparent),
                        radius = 280f
                    )
                )
        )
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            content()
        }
    }
}

/**
 * ==========================================
 * REDESIGNED HEADER: COMPACT CELESTIAL BAR
 * ==========================================
 * Extremely low visual noise, styled dynamically, under 72dp maximum height.
 */
@Composable
fun HomeHeader(
    gregorianDate : String,
    hijriDate     : String,
    locationName  : String,
    isLoading     : Boolean,
    showRamadhanFeature : Boolean = false,
    onRamadhanClick : () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Live GPS indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (locationName.isNotEmpty()) TealAccent else WarningAmber)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                     text = locationName.ifEmpty { "Mencari GPS..." },
                     fontSize = 12.sp,
                     fontFamily = NunitoFont,
                     fontWeight = FontWeight.Bold,
                     color = TextPrimary,
                     maxLines = 1,
                     overflow = TextOverflow.Ellipsis
                )
                if (isLoading) {
                    Spacer(Modifier.width(6.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        color = TealAccent,
                        strokeWidth = 1.2.dp
                    )
                }
            }
            
            Spacer(Modifier.height(2.dp))
            
            // Sacred Dates line
            Text(
                text = "$hijriDate  •  $gregorianDate",
                fontSize = 10.sp,
                fontFamily = NunitoFont,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (showRamadhanFeature) {
            IconButton(
                onClick = onRamadhanClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .testTag("ramadhan_feature_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "Tugas Ramadhan",
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * ==========================================
 * LIGHTWEIGHT CELESTIAL PROGRESS RING
 * ==========================================
 * Rebuilt using static performance geometry to avoid power drain.
 */
@Composable
fun AstrolabeProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2.0f - 4.dp.toPx()
            val strokeW = 3.dp.toPx()
            if (radius > 0f) {
                // Background Track
                drawCircle(
                    color = MidnightLayer.copy(alpha = 0.5f),
                    radius = radius,
                    style = Stroke(width = strokeW)
                )
                // Active arc sweep
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to TealAccent,
                        0.5f to GoldPrimary,
                        1.0f to GoldLight
                    ),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * ==========================================
 * COMPACT HERO DIAL CARD
 * ==========================================
 * Consolidates Next Prayer, Countdown, and Progress Ring, under 180dp maximum height.
 */
@Composable
fun NextPrayerHeroCard(
    nextPrayerName : String,
    countdown      : String,
    todaySchedule  : PrayerTime?
) {
    val nextTime = when (nextPrayerName.uppercase()) {
        "SUBUH"   -> todaySchedule?.fajr    ?: "--:--"
        "DZUHUR"  -> todaySchedule?.dhuhr   ?: "--:--"
        "ASHAR"   -> todaySchedule?.asr     ?: "--:--"
        "MAGHRIB" -> todaySchedule?.maghrib ?: "--:--"
        "ISYA"    -> todaySchedule?.isha    ?: "--:--"
        else      -> "--:--"
    }

    val arabicName = when (nextPrayerName.uppercase()) {
        "SUBUH"   -> "الفجر"
        "DZUHUR"  -> "الظهر"
        "ASHAR"   -> "العصر"
        "MAGHRIB" -> "المغرب"
        "ISYA"    -> "العشاء"
        else      -> ""
    }

    val progressVal = calculatePrayerProgress(todaySchedule, nextPrayerName)
    val progressPct = (progressVal * 100).toInt()

    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 20.dp),
        cornerRadius = CornerMedium,
        useGlow = false
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dial column
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .weight(0.9f),
                contentAlignment = Alignment.Center
            ) {
                // Lightweight dial sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2.0f - 6.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    if (radius > 0f) {
                        drawCircle(
                            color = MidnightLayer.copy(alpha = 0.5f),
                            radius = radius,
                            style = Stroke(width = strokeW)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                0.0f to TealAccent,
                                0.5f to GoldPrimary,
                                1.0f to GoldLight
                            ),
                            startAngle = -90f,
                            sweepAngle = progressVal * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                        // Marker point
                        val angleRad = Math.toRadians((progressVal * 360f - 90f).toDouble())
                        val endX = (size.width / 2 + radius * Math.cos(angleRad)).toFloat()
                        val endY = (size.height / 2 + radius * Math.sin(angleRad)).toFloat()
                        drawCircle(
                            color = GoldLight,
                            radius = 3.dp.toPx(),
                            center = Offset(endX, endY)
                        )
                    }
                }

                // Inner Calligraphy
                Text(
                    text = arabicName,
                    fontSize = 18.sp,
                    fontFamily = AmiriQuranFont,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.width(16.dp))

            // Text info column
            Column(
                modifier = Modifier.weight(1.3f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MENUJU $nextPrayerName",
                        fontSize = 10.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        letterSpacing = 1.sp
                    )
                    
                    Text(
                        text = "$progressPct%",
                        fontSize = 10.sp,
                        fontFamily = CinzelFont,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MidnightLayer.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = nextTime,
                    fontSize = 32.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(8.dp))

                // Countdown Pill Capsule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CornerMedium))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(0.5.dp, GoldPrimary.copy(alpha = 0.25f), RoundedCornerShape(CornerMedium))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = TealAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = countdown,
                        fontFamily = CinzelFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}

/**
 * ==========================================
 * COMPACT DAILY SCHEDULE LIST (5 PRAYER TIMES)
 * ==========================================
 * Renders all 5 prayers on one single line inside a glass card.
 */
@Composable
fun TodayPrayerTimesRow(
    todaySchedule  : PrayerTime?,
    nextPrayerName : String
) {
    val isRamadhan = remember { HijriDateUtils.isCurrentlyRamadhan() }
    val prayers = buildList {
        if (isRamadhan) {
            add("Imsak" to (todaySchedule?.imsak?.ifEmpty { null } ?: "--:--"))
        }
        add("Subuh" to (todaySchedule?.fajr    ?: "--:--"))
        add("Dzuhur" to (todaySchedule?.dhuhr   ?: "--:--"))
        add("Ashar" to (todaySchedule?.asr     ?: "--:--"))
        add("Maghrib" to (todaySchedule?.maghrib ?: "--:--"))
        add("Isya" to (todaySchedule?.isha    ?: "--:--"))
    }

    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        cornerRadius = CornerMedium
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "JADWAL SHOLAT HARI INI",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.width(8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLine)
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                prayers.forEach { (name, time) ->
                    val isNext = name.equals(nextPrayerName, ignoreCase = true)
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isNext) CardElevated else Color.Transparent
                            )
                            .border(
                                width = if (isNext) 1.dp else 0.dp,
                                color = if (isNext) GoldPrimary.copy(alpha = 0.35f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text          = name,
                            fontFamily    = NunitoFont,
                            fontSize      = 10.sp,
                            fontWeight    = if (isNext) FontWeight.Bold else FontWeight.Medium,
                            color         = if (isNext) GoldPrimary else TextSecondary,
                            textAlign     = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Text(
                            text       = time,
                            fontSize   = 13.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color      = if (isNext) GoldLight else TextPrimary,
                            textAlign     = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * ==========================================
 * SPRINT ADDITION: QUICK ACTIONS ROW (MAX 4)
 * ==========================================
 * Flat, sleek horizontal button bar offering single-row navigation.
 */
@Composable
fun QuickActionsRow(
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("Al-Qur'an", Icons.Outlined.AutoStories, AppScreen.QURAN),
        Triple("Kiblat", Icons.Outlined.Explore, AppScreen.KIBLAT),
        Triple("Tasbih", Icons.Outlined.ChangeCircle, AppScreen.TASBIH),
        Triple("Pelacak", Icons.Outlined.CheckCircle, AppScreen.TRACKER)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(CornerMedium))
                    .background(MidnightLayer.copy(alpha = 0.4f))
                    .border(
                        width = 0.8.dp,
                        brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.15f), Color.Transparent)),
                        shape = RoundedCornerShape(CornerMedium)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(),
                        onClick = { onNavigateToScreen(item.third) }
                    )
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.second,
                    contentDescription = item.first,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.first,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * ==========================================
 * COMPATIBLE HELPER WRAPPERS (COMPILE-SAFE)
 * ==========================================
 */
@Composable
fun ReminderNoteCard() {
    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepNight)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📖",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HIKMAH UTAMA HARI INI",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        letterSpacing = 1.5.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Amalan Sholat Tepat Waktu",
                    fontSize = 14.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "\"Sesungguhnya sholat itu bagi orang-orang yang beriman adalah kewajiban yang ditentukan waktunya.\"\n— QS. An-Nisa': 103",
                    fontSize = 12.sp,
                    fontFamily = NunitoFont,
                    lineHeight = 18.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GridMenuSection(
    onNavigateToScreen: (AppScreen) -> Unit
) {
    // Left empty or fallback to maintain back-compatibility if referenced.
    // Screen leverages QuickActionsRow as the active clean replacement.
}
