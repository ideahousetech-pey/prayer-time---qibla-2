package id.ideahousetech.prayertime_qibla.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DailyScheduleScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    onBackClick: () -> Unit,
    onNavigateToMonthly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationName by locationViewModel.locationName.collectAsState()
    val monthlySchedule by prayerViewModel.monthlySchedule.collectAsState()

    // 1. Tanggal aktif harian terpilih (default: hari ini)
    val todayCalendar = remember { Calendar.getInstance() }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    // Hitung 7 hari timeline yang melingkupi hari ini (H-3 s.d H+3)
    val timelineDays = remember {
        (0..6).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, offset - 3)
            cal
        }
    }

    // Mengambil data jadwal sholat untuk tanggal terpilih
    val activeSchedule = remember(selectedCalendar, monthlySchedule) {
        val targetDay = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        if (monthlySchedule.isNotEmpty()) {
            val idx = (targetDay - 1).coerceIn(0, monthlySchedule.size - 1)
            monthlySchedule[idx]
        } else {
            null
        }
    }

    // Format tanggal gregorian untuk selected date
    val formattedSelectedGregorian = remember(selectedCalendar) {
        val sdf = SimpleDateFormat("dd MMMM, yyyy", Locale("id", "ID"))
        sdf.format(selectedCalendar.time)
    }

    // Format tanggal hijriah untuk selected date
    val formattedSelectedHijri = remember(selectedCalendar) {
        val hijri = HijriDateUtils.convertToHijri(selectedCalendar)
        hijri.formatted
    }

    // SharedPreferences untuk status ON/OFF adzan individual
    val adzanPrefs = remember { context.getSharedPreferences("adzan_individual_prefs", Context.MODE_PRIVATE) }
    
    // State lokal untuk trigger komposisi ulang saat speaker ditekan
    var refreshSpeakerState by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. TOOLBAR KUSTOM PREMIUM ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(CardSurface, CircleShape)
                            .border(1.dp, DividerLine, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = if (locationName.isEmpty()) "Mencari Lokasi..." else locationName,
                            fontSize = 16.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Membagikan jadwal sholat hari ini...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan",
                            tint = GoldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onNavigateToMonthly,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Jadwal Bulanan",
                            tint = GoldPrimary
                        )
                    }
                }
            }

            // --- 2. DIAGRAM LINTASAN MATAHARI (SUN PATH ARC) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GoldGlow.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp)) {
                    val w = size.width
                    val h = size.height

                    // Gambar kurva setengah lingkaran putus-putus
                    val path = Path().apply {
                        moveTo(0f, h)
                        cubicTo(w * 0.25f, h * -0.3f, w * 0.75f, h * -0.3f, w, h)
                    }

                    drawPath(
                        path = path,
                        color = GoldDim.copy(alpha = 0.45f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    )

                    // Node posisi sholat
                    val nodes = listOf(
                        Offset(0f, h), // Subuh
                        Offset(w * 0.15f, h * 0.65f), // Terbit
                        Offset(w * 0.5f, h * 0.13f), // Dzuhur
                        Offset(w * 0.73f, h * 0.35f), // Ashar
                        Offset(w * 0.9f, h * 0.77f), // Maghrib
                        Offset(w, h) // Isya
                    )

                    nodes.forEach { node ->
                        drawCircle(
                            color = GoldPrimary,
                            radius = 6.dp.toPx(),
                            center = node
                        )
                        drawCircle(
                            color = DeepNight,
                            radius = 3.dp.toPx(),
                            center = node
                        )
                    }
                }

                // Ornamen Bulan Sabit
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 24.dp)
                        .size(38.dp)
                        .border(1.dp, DividerLine, CircleShape)
                        .background(CardSurface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 3. HORIZONTAL TIMELINE STRIP HARI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                timelineDays.forEach { cal ->
                    val isSelected = cal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR)
                    val dayLabel = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "M"
                        Calendar.MONDAY -> "S"
                        Calendar.TUESDAY -> "S"
                        Calendar.WEDNESDAY -> "R"
                        Calendar.THURSDAY -> "K"
                        Calendar.FRIDAY -> "J"
                        Calendar.SATURDAY -> "S"
                        else -> "M"
                    }
                    val dateLabel = cal.get(Calendar.DAY_OF_MONTH).toString()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCalendar = cal }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = dayLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) GoldPrimary else TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dateLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNight
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dateLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- 4. TANGGAL GANDA AKTIF SELEKSI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface.copy(alpha = 0.5f))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$formattedSelectedGregorian  /  $formattedSelectedHijri",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 5. DAFTAR WAKTU SHOLAT ---
            if (activeSchedule != null) {
                val terbitTime = remember(activeSchedule.fajr) { 
                    calculateTerbitFromFajr(activeSchedule.fajr) 
                }

                val pList = listOf(
                    PrayerRowItem("Subuh", activeSchedule.fajr, "الفجر", "subuh"),
                    PrayerRowItem("Terbit", terbitTime, "الشروق", "terbit", isTerbit = true),
                    PrayerRowItem("Dzuhur", activeSchedule.dhuhr, "الظهر", "dzuhur"),
                    PrayerRowItem("Ashar", activeSchedule.asr, "العصر", "ashar"),
                    PrayerRowItem("Maghrib", activeSchedule.maghrib, "المغرب", "maghrib"),
                    PrayerRowItem("Isya", activeSchedule.isha, "العشاء", "isya")
                )

                // State trigger dari speaker
                val trigger = refreshSpeakerState

                // Evaluasi status masing-masing row
                val today = remember { Calendar.getInstance() }
                val isToday = selectedCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                              selectedCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                val isPast = selectedCalendar.before(today) && !isToday

                val currentHour = today.get(Calendar.HOUR_OF_DAY)
                val currentMin = today.get(Calendar.MINUTE)
                val currentInMinutes = currentHour * 60 + currentMin

                val prayerMinutes = remember(pList) {
                    pList.map { p ->
                        val parts = p.timeValue.replace('.', ':').split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        h * 60 + m
                    }
                }

                val activeIndex = if (isToday) {
                    var maxIdx = -1
                    for (i in pList.indices) {
                        if (currentInMinutes >= prayerMinutes[i]) {
                            maxIdx = i
                        }
                    }
                    maxIdx
                } else {
                    -1
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pList.forEachIndexed { index, p ->
                        val isAlarmOn = adzanPrefs.getBoolean("enable_adzan_${p.prefKey}", true)

                        val status = when {
                            isPast -> PrayerStatus.LEWAT
                            isToday -> {
                                when {
                                    index < activeIndex -> PrayerStatus.LEWAT
                                    index == activeIndex -> PrayerStatus.AKTIF
                                    else -> PrayerStatus.AKAN_DATANG
                                }
                            }
                            else -> PrayerStatus.AKAN_DATANG
                        }

                        PrayerCardItem(
                            p = p,
                            status = status,
                            isAlarmOn = isAlarmOn,
                            onSpeakerClick = {
                                val currentVal = adzanPrefs.getBoolean("enable_adzan_${p.prefKey}", true)
                                adzanPrefs.edit().putBoolean("enable_adzan_${p.prefKey}", !currentVal).apply()
                                refreshSpeakerState++
                                Toast.makeText(
                                    context,
                                    if (!currentVal) "Suara Adzan ${p.name} Diaktifkan" else "Suara Adzan ${p.name} Dimatikan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            }
        }
    }
}

enum class PrayerStatus {
    LEWAT, AKTIF, AKAN_DATANG
}

@Composable
fun PrayerCardItem(
    p: PrayerRowItem,
    status: PrayerStatus,
    isAlarmOn: Boolean,
    onSpeakerClick: () -> Unit
) {
    val cardBackground = when (status) {
        PrayerStatus.LEWAT -> CardSurface.copy(alpha = 0.5f)
        PrayerStatus.AKTIF -> GoldGlow
        PrayerStatus.AKAN_DATANG -> CardSurface
    }

    val cardBorder = when (status) {
        PrayerStatus.LEWAT -> BorderStroke(0.5.dp, DividerLine.copy(alpha = 0.5f))
        PrayerStatus.AKTIF -> BorderStroke(1.dp, GoldPrimary)
        PrayerStatus.AKAN_DATANG -> BorderStroke(1.dp, DividerLine)
    }

    val contentAlpha = if (status == PrayerStatus.LEWAT) 0.5f else 1.0f

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = cardBorder,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon (Checkmark / Play / Circle)
            when (status) {
                PrayerStatus.LEWAT -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Sudah lewat",
                        tint = TealAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                PrayerStatus.AKTIF -> {
                    Icon(
                        imageVector = Icons.Outlined.PlayCircle,
                        contentDescription = "Saat ini",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                PrayerStatus.AKAN_DATANG -> {
                    Icon(
                        imageVector = Icons.Outlined.Circle,
                        contentDescription = "Akan datang",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Prayer Name
            Text(
                text = p.name,
                fontWeight = if (status == PrayerStatus.AKTIF) FontWeight.Bold else FontWeight.Medium,
                fontSize = 15.sp,
                color = if (status == PrayerStatus.AKTIF) GoldPrimary else TextPrimary,
                modifier = Modifier.weight(1.5f)
            )

            // Hour Display
            Text(
                text = p.timeValue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (status == PrayerStatus.AKTIF) GoldPrimary else TextPrimary,
                modifier = Modifier.weight(1.2f),
                textAlign = TextAlign.Start
            )

            // Arabic text (Fajr, Syuruq etc.)
            Text(
                text = p.arabicName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (status == PrayerStatus.AKTIF) GoldPrimary else TextSecondary,
                modifier = Modifier.weight(1.5f),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Alarm Speaker Clickable
            if (p.isTerbit) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "Matahari Terbit (Dilarang Sholat)",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                IconButton(
                    onClick = onSpeakerClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isAlarmOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Alarm untuk ${p.name}",
                        tint = if (isAlarmOn) {
                            if (status == PrayerStatus.AKTIF) GoldPrimary else TealAccent
                        } else {
                            TextMuted
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

data class PrayerRowItem(
    val name: String,
    val timeValue: String,
    val arabicName: String,
    val prefKey: String,
    val isTerbit: Boolean = false
)

private fun calculateTerbitFromFajr(fajr: String): String {
    return try {
        val parts = fajr.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val totalMinutes = hour * 60 + minute + 75
        val targetHour = (totalMinutes / 60) % 24
        val targetMinute = totalMinutes % 60
        "%02d:%02d".format(targetHour, targetMinute)
    } catch (e: Exception) {
        "05:50"
    }
}
