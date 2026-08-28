package id.ideahousetech.prayertime_qibla.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.ideahousetech.prayertime_qibla.data.PrayerTracker
import id.ideahousetech.prayertime_qibla.model.AchievementBadge
import id.ideahousetech.prayertime_qibla.model.BadgeCategory
import id.ideahousetech.prayertime_qibla.model.WeeklySpiritualSummary
import id.ideahousetech.prayertime_qibla.model.MonthlySpiritualSummary
import id.ideahousetech.prayertime_qibla.model.PrayerStatus
import id.ideahousetech.prayertime_qibla.model.PrayerName
import id.ideahousetech.prayertime_qibla.ui.IslamicGlassCard
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Layar Pelacak Sholat (Prayer Tracker) bernuansa Islamic Luxury M3.
 * Dikembangkan oleh Gamification Expert & Senior Product Designer.
 * Membagi sirkulasi interaktif menjadi 3 Tab:
 * 1. Jadwal Harian: Checklist interaktif beserta Kalender Heatmap Keaktifan luring.
 * 2. Pencapaian: Badge spiritual (Riyadh Milestones) berbasis hadits silsilah shalat & refleksi luhur.
 * 3. Ringkasan: Rekapitulasi Mingguan & Bulanan didukung visualisasi native kustom, evaluasi, serta Tausiyah Khusus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTrackerScreen(
    trackerViewModel: PrayerTrackerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by trackerViewModel.selectedDate.collectAsState()
    val selectedTracker by trackerViewModel.selectedDateTracker.collectAsState()
    val allTrackers by trackerViewModel.allTrackers.collectAsState()
    val streakCount by trackerViewModel.streakCount.collectAsState()
    val bestStreakCount by trackerViewModel.bestStreakCount.collectAsState()
    val jamaahCount by trackerViewModel.jamaahCount.collectAsState()
    val totalDoneCount by trackerViewModel.totalDoneCount.collectAsState()
    val monthTrackers by trackerViewModel.calendarMonthTrackers.collectAsState()

    // Gamification states
    val badges by trackerViewModel.badges.collectAsState()
    val weeklySummary by trackerViewModel.weeklySummary.collectAsState()
    val monthlySummary by trackerViewModel.monthlySummary.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Jadwal Harian, 1 = Pencapaian, 2 = Ringkasan
    var selectedBadgeDetail by remember { mutableStateOf<AchievementBadge?>(null) }
    var selectedSummaryPeriod by remember { mutableStateOf(0) } // 0 = Mingguan, 1 = Bulanan

    var showStatusPickerDialog by remember { mutableStateOf(false) }
    var activePrayerToEdit by remember { mutableStateOf<String?>(null) }
    var activeCurrentStatus by remember { mutableStateOf("") }

    val displayMonthFormatter = remember { SimpleDateFormat("MMMM yyyy", Locale("id", "ID")) }
    val currentMonthLabel = displayMonthFormatter.format(Date())

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. HEADER DENGAN TOMBOL BACK
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MidnightLayer.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "PELACAK SHOLAT",
                        fontSize = 19.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Istiqomahkan sholat 5 waktu dengan ketenangan ruhani",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2. STATS BOARD (Islamic Luxury Design) - Selalu Ditampilkan di Atas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active/Best Streak Card
                StatLuxuryCard(
                    modifier = Modifier.weight(1f),
                    title = "Streak Aktif",
                    value = "$streakCount Hari",
                    summary = "Sakti: $bestStreakCount Hari",
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = Color(0xFFFFA726)
                )

                // Jamaah Metric
                StatLuxuryCard(
                    modifier = Modifier.weight(1f),
                    title = "Berjamaah",
                    value = "$jamaahCount x",
                    summary = "Mulia 27 derajat",
                    icon = Icons.Default.Mosque,
                    iconColor = TealAccent
                )

                // Total Salat Done
                StatLuxuryCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Sholat",
                    value = "$totalDoneCount x",
                    summary = "Telah ditegakkan",
                    icon = Icons.Default.Verified,
                    iconColor = GoldPrimary
                )
            }

            // 3. TAB SELECTION SELECTOR WITH LUXURY GLASS STYLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
                    .clip(RoundedCornerShape(CornerMedium))
                    .background(MidnightLayer.copy(alpha = 0.6f))
                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(CornerMedium))
                    .padding(4.dp)
            ) {
                val tabs = listOf("Jadwal Harian", "Pencapaian Badges", "Ringkasan")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    Brush.verticalGradient(listOf(GoldPrimary, GoldDim))
                                } else {
                                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MidnightLayer else Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 4. ANIMATED CONTENT TRANSITION BASED ON TAB SELECTION
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TrackerTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> { // TAB 1: Jadwal Harian
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Kalender heatmap
                            IslamicGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Peta Konsistensi - $currentMonthLabel".uppercase(),
                                            fontSize = 11.sp,
                                            fontFamily = CinzelFont,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary,
                                            letterSpacing = 1.2.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                                            Text(text = "Penuh", fontSize = 9.sp, color = TextSecondary)
                                            Spacer(Modifier.width(4.dp))
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF6C00)))
                                            Text(text = "Bolong", fontSize = 9.sp, color = TextSecondary)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    CalendarHeatmapContainer(
                                        monthTrackers = monthTrackers,
                                        selectedDate = selectedDate,
                                        onDateSelected = { dateStr ->
                                            trackerViewModel.selectDate(dateStr)
                                        }
                                    )
                                }
                            }

                            // Tanggal terpilih & micro-checklist
                            val parsedDateLabel = remember(selectedDate) {
                                try {
                                    val dateObj = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate)
                                    SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(dateObj!!)
                                } catch (e: Exception) {
                                    selectedDate
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jadwal Tanggal: $parsedDateLabel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (selectedDate == trackerViewModel.getTodayDateString()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldPrimary.copy(alpha = 0.1f))
                                            .border(0.5.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Hari Ini",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary
                                        )
                                    }
                                }
                            }

                            val prayers = listOf(
                                PrayerTrackItem(PrayerName.SUBUH.indonesianName, selectedTracker?.subuhStatus ?: PrayerStatus.NONE, Icons.Outlined.WbTwilight),
                                PrayerTrackItem(PrayerName.DZUHUR.indonesianName, selectedTracker?.dhuhrStatus ?: PrayerStatus.NONE, Icons.Outlined.WbSunny),
                                PrayerTrackItem(PrayerName.ASHAR.indonesianName, selectedTracker?.asrStatus ?: PrayerStatus.NONE, Icons.Outlined.Cloud),
                                PrayerTrackItem(PrayerName.MAGHRIB.indonesianName, selectedTracker?.maghribStatus ?: PrayerStatus.NONE, Icons.Outlined.WbCloudy),
                                PrayerTrackItem(PrayerName.ISYA.indonesianName, selectedTracker?.isyaStatus ?: PrayerStatus.NONE, Icons.Outlined.NightsStay)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(bottom = 40.dp)
                            ) {
                                prayers.forEach { item ->
                                    PrayerTrackingRow(
                                        item = item,
                                        onClick = {
                                            activePrayerToEdit = item.name
                                            activeCurrentStatus = item.status.displayName
                                            showStatusPickerDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> { // TAB 2: Pencapaian Spiritual Badges
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "RIYADUSH Shalihin - Milestone Spiritual",
                                fontSize = 11.sp,
                                fontFamily = CinzelFont,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Pencapaian dipicu luring murni dari hadits-hadits shahih pilar sholat fardhu.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Tampilkan grid Badge
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 40.dp)
                            ) {
                                badges.forEach { badge ->
                                    BadgeDisplayRow(
                                        badge = badge,
                                        onClick = { selectedBadgeDetail = badge }
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // TAB 3: Ringkasan Laporan Mingguan & Bulanan
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Sub-selector periode: Pekan Terakhir vs Bulan Terakhir
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { selectedSummaryPeriod = 0 },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedSummaryPeriod == 0) GoldPrimary.copy(alpha = 0.2f) else MidnightLayer.copy(alpha = 0.4f),
                                        contentColor = if (selectedSummaryPeriod == 0) GoldPrimary else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(CornerMedium),
                                    border = BorderStroke(1.dp, if (selectedSummaryPeriod == 0) GoldPrimary else GoldPrimary.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Laporan Mingguan", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = { selectedSummaryPeriod = 1 },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedSummaryPeriod == 1) GoldPrimary.copy(alpha = 0.2f) else MidnightLayer.copy(alpha = 0.4f),
                                        contentColor = if (selectedSummaryPeriod == 1) GoldPrimary else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(CornerMedium),
                                    border = BorderStroke(1.dp, if (selectedSummaryPeriod == 1) GoldPrimary else GoldPrimary.copy(alpha = 0.1f))
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Laporan Bulanan", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            if (selectedSummaryPeriod == 0) {
                                // Tampilkan Rekap Mingguan
                                WeeklySummaryTabContent(summary = weeklySummary)
                            } else {
                                // Tampilkan Rekap Bulanan
                                MonthlySummaryTabContent(summary = monthlySummary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog Pemilih Status Sholat (M3 BottomSheet-style Dialog)
    if (showStatusPickerDialog && activePrayerToEdit != null) {
        StatusPickerDialog(
            prayerName = activePrayerToEdit!!,
            currentStatus = activeCurrentStatus,
            onDismiss = {
                showStatusPickerDialog = false
                activePrayerToEdit = null
            },
            onStatusSelected = { newStatus ->
                trackerViewModel.updatePrayerStatus(selectedDate, activePrayerToEdit!!, newStatus)
                showStatusPickerDialog = false
                activePrayerToEdit = null
                Toast.makeText(context, "Status ${activePrayerToEdit} diperbarui!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Dialog Detail Badge (Spiritual Milestones Drawer-Style Modal dialog)
    if (selectedBadgeDetail != null) {
        BadgeDetailDialog(
            badge = selectedBadgeDetail!!,
            onDismiss = { selectedBadgeDetail = null }
        )
    }
}

/**
 * Baris Kustom Badge Milestones
 */
@Composable
fun BadgeDisplayRow(
    badge: AchievementBadge,
    onClick: () -> Unit
) {
    val alphaBg = if (badge.isUnlocked) 0.35f else 0.15f
    val borderCol = if (badge.isUnlocked) GoldPrimary else GoldPrimary.copy(alpha = 0.12f)
    val titleCol = if (badge.isUnlocked) Color.White else Color.White.copy(alpha = 0.45f)
    val descCol = if (badge.isUnlocked) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerMedium))
            .background(MidnightLayer.copy(alpha = 0.5f))
            .border(0.8.dp, borderCol, RoundedCornerShape(CornerMedium))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emblem Icon Bulat bermotif emas
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (badge.isUnlocked) GoldPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                        .border(1.2.dp, if (badge.isUnlocked) GoldPrimary else Color.White.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val badgeIcon = when (badge.id) {
                        "fajr_warrior" -> Icons.Default.WbTwilight
                        "jamaah_champion" -> Icons.Default.Mosque
                        "seven_day_istiqomah" -> Icons.Default.LocalFireDepartment
                        "perfect_light" -> Icons.Default.Shield
                        "midday_guardian" -> Icons.Default.WbSunny
                        else -> Icons.Default.NightsStay
                    }
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) GoldPrimary else Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = badge.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleCol
                        )
                        if (badge.isUnlocked) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Terbuka",
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = badge.requirementText,
                        fontSize = 11.sp,
                        color = descCol,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    // Progress indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(badge.progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (badge.isUnlocked) GoldPrimary else TextSecondary)
                            )
                        }

                        Text(
                            text = badge.progressText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) GoldPrimary else TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Klik info button
            Icon(
                imageVector = Icons.Default.ArrowRight,
                contentDescription = "Detail",
                tint = if (badge.isUnlocked) GoldPrimary else Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Dialog Detail Milestones Ibadah yang mewah
 */
@Composable
fun BadgeDetailDialog(
    badge: AchievementBadge,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerLarge))
                .background(MidnightLayer)
                .border(1.5.dp, GoldPrimary, RoundedCornerShape(CornerLarge))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emblem Icon bulat besar kustom
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.1f))
                        .border(1.5.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val badgeIcon = when (badge.id) {
                        "fajr_warrior" -> Icons.Default.WbTwilight
                        "jamaah_champion" -> Icons.Default.Mosque
                        "seven_day_istiqomah" -> Icons.Default.LocalFireDepartment
                        "perfect_light" -> Icons.Default.Shield
                        "midday_guardian" -> Icons.Default.WbSunny
                        else -> Icons.Default.NightsStay
                    }
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = badge.title.uppercase(),
                    fontSize = 16.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (badge.isUnlocked) GoldPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, if (badge.isUnlocked) GoldPrimary else TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (badge.isUnlocked) "AKTIF / MAULID" else "PROSES AKUMULASI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) GoldPrimary else TextSecondary
                    )
                }

                Text(
                    text = badge.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Divider(color = GoldPrimary.copy(alpha = 0.15f), thickness = 1.dp)

                // Kutipan Hadits Emas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(CornerMedium))
                        .background(CardElevated.copy(alpha = 0.5f))
                        .border(0.5.dp, GoldPrimary.copy(alpha = 0.08f), RoundedCornerShape(CornerMedium))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = badge.hadithSource ?: "Referensi Sunnah",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Fadhilah: \"${badge.spiritualBenefit}\"",
                        fontSize = 11.sp,
                        color = Color.White,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(CornerMedium)
                ) {
                    Text(
                        text = "TUTUP DIAGNOSIS",
                        color = MidnightLayer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * TAMPILAN TAB LAPORAN MINGGUAN
 */
@Composable
fun WeeklySummaryTabContent(
    summary: WeeklySpiritualSummary
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Visual circular rekap
        IslamicGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Ring Left
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { summary.completionPercentage },
                        modifier = Modifier.fillMaxSize(),
                        color = GoldPrimary,
                        strokeWidth = 8.dp,
                        trackColor = Color.White.copy(alpha = 0.08f),
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(summary.completionPercentage * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Tegak",
                            fontSize = 9.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Detail Metrics Right
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PERIODE: ${summary.startDateLabel} - ${summary.endDateLabel}".uppercase(),
                        fontSize = 10.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryMiniText("Berjamaah", "${summary.totalJamaah}x", TealAccent)
                        SummaryMiniText("Sendiri", "${summary.totalMunfarid}x", GoldPrimary)
                        SummaryMiniText("Terlambat", "${summary.totalMasbuq}x", Color(0xFFFFB74D))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (summary.totalHalangan > 0) {
                        Text(
                            text = "Terbebas ${summary.totalHalangan} sholat syar'i (Udzur / Halangan)",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Tausiyah khusus (Therapeutic Spiritual Advice card)
        IslamicGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "TAUSIYAH & EVALUASI SPIRITUAL PEKANAN",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = summary.adviceTherapy,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Rujukan: ${summary.adviceSource}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
            }
        }

        // Distribusi Salat Terstruktur (Horizontal Progress Bars)
        IslamicGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "REKAP TENAGA TIAP WAKTU SALAT",
                    fontSize = 11.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val displayPrayers = listOf("Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya")

                displayPrayers.forEach { prayerName ->
                    val count = summary.prayerDistribution[prayerName] ?: 0
                    val barRatio = count / 7.0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayerName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(62.dp)
                        )

                        // Progress bar block with custom styling
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MidnightLayer)
                                .border(0.5.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                GoldPrimary.copy(alpha = 0.4f),
                                                GoldPrimary
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "$count / 7 Hari",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (count >= 5) GoldPrimary else TextSecondary,
                            modifier = Modifier.width(62.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

/**
 * TAMPILAN TAB LAPORAN BULANAN
 */
@Composable
fun MonthlySummaryTabContent(
    summary: MonthlySpiritualSummary
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        IslamicGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = summary.monthLabel.uppercase(),
                            fontSize = 13.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Rekapitulasi Sujud Sebulan Ini",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .border(0.5.dp, GoldPrimary, RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Gelar: ${summary.primaryAchievedMilestone}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Linear bar rekap target bulanan
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Persentase Ketepatan Salat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${(summary.completionPercentage * 100).toInt()}% terlaksana",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(summary.completionPercentage)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(TealAccent, GoldPrimary)))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barisan metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonthlyStatTile(
                        modifier = Modifier.weight(1f),
                        label = "Hari Mencatat",
                        value = "${summary.activeDaysCount} / 30 Hari",
                        icon = Icons.Default.CalendarMonth
                    )

                    MonthlyStatTile(
                        modifier = Modifier.weight(1f),
                        label = "Pencapaian Terbuka",
                        value = "${summary.unlockedBadgesCount} / 6 Badges",
                        icon = Icons.Default.Verified
                    )

                    MonthlyStatTile(
                        modifier = Modifier.weight(1f),
                        label = "Max Streak",
                        value = "${summary.bestStreakThisMonth} Hari",
                        icon = Icons.Default.LocalFireDepartment
                    )
                }
            }
        }

        // Tinjauan Bulanan Terapeutik
        IslamicGlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "DIAGNOSIS SPIRITUAL BULANAN",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = summary.generalAssessment,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MonthlyStatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CornerMedium))
            .background(MidnightLayer.copy(alpha = 0.3f))
            .border(0.5.dp, GoldPrimary.copy(alpha = 0.08f), RoundedCornerShape(CornerMedium))
            .padding(10.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label, fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun SummaryMiniText(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(text = label, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Black)
    }
}

/**
 * Matriks Kalender Heatmap visual dinamis
 */
@Composable
fun CalendarHeatmapContainer(
    monthTrackers: List<PrayerTracker>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    val trackerMap = remember(monthTrackers) {
        monthTrackers.associateBy { it.date }
    }

    // Mengalkulasi hari-hari sepanjang bulan aktif berjalan
    val daysInMonthList = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val currentMonth = cal.get(Calendar.MONTH)
        
        while (cal.get(Calendar.MONTH) == currentMonth) {
            list.add(sdfKey.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    // Mengambil nama hari pendek untuk baris teratas (Ahad, Sen, Sel, Rab, Kam, Jum, Sab)
    val dayLabels = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Label Nama Hari
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid Angka Penanggalan
        val firstDayCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        val startDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Ahad=0)

        val totalGridSlots = startDayOfWeek + daysInMonthList.size
        val rowsCount = (totalGridSlots + 6) / 7

        repeat(rowsCount) { r ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { c ->
                    val index = r * 7 + c
                    if (index >= startDayOfWeek && index < totalGridSlots) {
                        val dateString = daysInMonthList[index - startDayOfWeek]
                        val tracker = trackerMap[dateString]
                        
                        val isSelected = dateString == selectedDate
                        val dayNum = dateString.split("-").last().toInt().toString()

                        // Hitung pemenuhan
                        val isFull = tracker?.isFullyCompleted() ?: false
                        val countDone = tracker?.let {
                            listOf(it.subuhStatus, it.dhuhrStatus, it.asrStatus, it.maghribStatus, it.isyaStatus)
                                .count { s -> s != PrayerStatus.NONE }
                        } ?: 0

                        val boxBg = when {
                            isSelected -> GoldPrimary
                            isFull -> Color(0xFF2E7D32).copy(alpha = 0.7f)
                            countDone > 0 -> Color(0xFFEF6C00).copy(alpha = 0.5f)
                            else -> MidnightLayer.copy(alpha = 0.6f)
                        }

                        val borderStroke = when {
                            isSelected -> 1.5.dp
                            else -> 0.5.dp
                        }

                        val borderColor = when {
                            isSelected -> Color.White
                            isFull -> Color(0xFF1B5E20)
                            countDone > 0 -> Color(0xFFE65100)
                            else -> GoldPrimary.copy(alpha = 0.15f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(boxBg)
                                .border(borderStroke, borderColor, RoundedCornerShape(8.dp))
                                .clickable { onDateSelected(dateString) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || isFull) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) MidnightLayer else Color.White
                            )
                        }
                    } else {
                        // Empty Spacer slot
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Kartu stats yang didekorasi elegan Islamic Luxury
 */
@Composable
fun StatLuxuryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    summary: String,
    icon: ImageVector,
    iconColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CornerMedium))
            .background(MidnightLayer.copy(alpha = 0.4f))
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(listOf(GoldPrimary.copy(alpha = 0.2f), Color.Transparent)),
                shape = RoundedCornerShape(CornerMedium)
            )
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = summary,
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Dialog Pemilih Status Sholat yang Premium dan Bersih
 */
@Composable
fun StatusPickerDialog(
    prayerName: String,
    currentStatus: String,
    onDismiss: () -> Unit,
    onStatusSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerLarge))
                .background(MidnightLayer)
                .border(1.5.dp, GoldPrimary, RoundedCornerShape(CornerLarge))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Dialog
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CATAT KEMAJUAN SHOLAT",
                    fontSize = 15.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Perbarui aktivitas sholat ${prayerName} Anda",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // List of options (M3 tactile choices)
                val options = listOf(
                    Triple(PrayerStatus.JAMAAH.displayName, "Sholat Berjamaah", "Pahala 27x lipat lebih utama"),
                    Triple(PrayerStatus.MUNFARID.displayName, "Sholat Sendiri (Munfarid)", "Menjalankan kewajiban sholat"),
                    Triple(PrayerStatus.MASBUQ.displayName, "Terlambat / Menyusul (Masbuq)", "Tetap laksanakan meski tertunda"),
                    Triple(PrayerStatus.HALANGAN.displayName, "Halangan / Udzur Syar'i", "Udzur syar'i (wanita haid / sakit)"),
                    Triple(PrayerStatus.NONE.displayName, "Belum Sholat / Hapus", "Reset data catatan sholat")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { opt ->
                        val isSelected = opt.first == currentStatus
                        val borderCol = if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.1f)
                        val bgCol = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else CardElevated.copy(alpha = 0.5f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(CornerMedium))
                                .background(bgCol)
                                .border(1.dp, borderCol, RoundedCornerShape(CornerMedium))
                                .clickable { onStatusSelected(opt.first) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onStatusSelected(opt.first) },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary, unselectedColor = TextSecondary)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = opt.second,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GoldPrimary else Color.White
                                )
                                Text(
                                    text = opt.third,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LEWATI",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Item individual penampung data visualisasi row sholat
 */
data class PrayerTrackItem(
    val name: String,
    val status: PrayerStatus,
    val icon: ImageVector
)

/**
 * Baris individual pelacak sholat dengan glassmorphic dan visual indah
 */
@Composable
fun PrayerTrackingRow(
    item: PrayerTrackItem,
    onClick: () -> Unit
) {
    val bgGradient = when (item.status) {
        PrayerStatus.JAMAAH -> Brush.horizontalGradient(listOf(TealAccent.copy(alpha = 0.12f), Color.Transparent))
        PrayerStatus.MUNFARID -> Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.08f), Color.Transparent))
        PrayerStatus.MASBUQ -> Brush.horizontalGradient(listOf(Color(0xFFFFB74D).copy(alpha = 0.08f), Color.Transparent))
        PrayerStatus.HALANGAN -> Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent))
        else -> Brush.horizontalGradient(listOf(MidnightLayer.copy(alpha = 0.2f), Color.Transparent))
    }

    val borderStrokeColor = when (item.status) {
        PrayerStatus.JAMAAH -> TealAccent.copy(alpha = 0.4f)
        PrayerStatus.MUNFARID -> GoldPrimary.copy(alpha = 0.4f)
        PrayerStatus.MASBUQ -> Color(0xFFFFB74D).copy(alpha = 0.4f)
        PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.2f)
        else -> GoldPrimary.copy(alpha = 0.1f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerMedium))
            .background(MidnightLayer.copy(alpha = 0.4f))
            .background(bgGradient)
            .border(0.8.dp, borderStrokeColor, RoundedCornerShape(CornerMedium))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CardElevated)
                        .border(1.dp, borderStrokeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = when (item.status) {
                            PrayerStatus.JAMAAH -> TealAccent
                            PrayerStatus.MUNFARID -> GoldPrimary
                            PrayerStatus.MASBUQ -> Color(0xFFFFB74D)
                            PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.6f)
                            else -> TextSecondary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    val statusText = when (item.status) {
                        PrayerStatus.JAMAAH -> "Berjamaah"
                        PrayerStatus.MUNFARID -> "Sendiri (Munfarid)"
                        PrayerStatus.MASBUQ -> "Terlambat / Masbuq"
                        PrayerStatus.HALANGAN -> "Halangan / Udzur"
                        else -> "Belum Dicatat"
                    }
                    
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = if (item.status == PrayerStatus.NONE) TextSecondary else Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Status Indicator Icon / Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        when (item.status) {
                            PrayerStatus.JAMAAH -> TealAccent.copy(alpha = 0.2f)
                            PrayerStatus.MUNFARID -> GoldPrimary.copy(alpha = 0.2f)
                            PrayerStatus.MASBUQ -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                            PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.1f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val statusIcon = when (item.status) {
                        PrayerStatus.JAMAAH -> Icons.Default.Mosque
                        PrayerStatus.MUNFARID -> Icons.Default.Person
                        PrayerStatus.MASBUQ -> Icons.Default.AccessTime
                        PrayerStatus.HALANGAN -> Icons.Default.Healing
                        else -> Icons.Default.RadioButtonUnchecked
                    }
                    
                    val statusTint = when (item.status) {
                        PrayerStatus.JAMAAH -> TealAccent
                        PrayerStatus.MUNFARID -> GoldPrimary
                        PrayerStatus.MASBUQ -> Color(0xFFFFB74D)
                        PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.7f)
                        else -> TextSecondary
                    }

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(13.dp)
                    )

                    Text(
                        text = when (item.status) {
                            PrayerStatus.NONE -> "Klik Catat"
                            else -> "Ubah"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusTint
                    )
                }
            }
        }
    }
}
