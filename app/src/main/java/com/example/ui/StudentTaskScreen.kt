package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerSubmissionUiModel
import id.ideahousetech.prayertime_qibla.viewmodel.StudentTaskViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.TaskItemUiModel
import id.ideahousetech.prayertime_qibla.viewmodel.TaskLockState
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Layar Lembar Tugas Ramadhan 30 Hari untuk Siswa.
 * Menampilkan checklist 6 waktu ibadah harian dengan penegakan validasi waktu per ibadah.
 */
@Composable
fun StudentTaskScreen(
    viewModel: StudentTaskViewModel,
    todayPrayerTime: PrayerTime? = null,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(todayPrayerTime) {
        viewModel.loadData(todayPrayerTime)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                        .testTag("student_task_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }

                Text(
                    text = "Lembar Tugas Ramadhan",
                    fontSize = 18.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, WarningAmber.copy(alpha = 0.3f), CircleShape)
                        .testTag("student_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Keluar",
                        tint = WarningAmber
                    )
                }
            }

            // Student Profile & Progress Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(TealAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assalamualaikum,",
                                fontSize = 11.sp,
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldPrimary
                            )
                            Text(
                                text = uiState.student?.name ?: "Siswa Ramadhan",
                                fontSize = 15.sp,
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Kelas ${uiState.studentClass?.label ?: "-"}",
                                fontSize = 12.sp,
                                fontFamily = NunitoFont,
                                color = GoldPrimary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // Badge Progres Selesai
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealAccent.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${uiState.completedCount}/${uiState.totalCount} Ibadah",
                                fontSize = 12.sp,
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                color = TealAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Linear Progress Bar
                    val progressFraction = if (uiState.totalCount > 0) {
                        uiState.completedCount.toFloat() / uiState.totalCount.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoldPrimary,
                        trackColor = MidnightLayer
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.errorMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        fontSize = 12.sp,
                        fontFamily = NunitoFont,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Daftar Tugas 30 Hari
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else if (uiState.taskItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada daftar tugas Ramadhan yang diterbitkan.",
                        fontFamily = NunitoFont,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                ) {
                    items(uiState.taskItems, key = { it.task.taskId }) { item ->
                        TaskItemCard(
                            item = item,
                            onTogglePrayer = { prayerItem ->
                                viewModel.toggleTask(item, prayerItem)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemCard(
    item: TaskItemUiModel,
    onTogglePrayer: (PrayerSubmissionUiModel) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }
    val formattedDate = remember(item.task.taskDate) {
        try {
            dateFormat.format(item.task.taskDate.toDate())
        } catch (_: Exception) {
            "-"
        }
    }

    val completedDayCount = item.prayerItems.count { it.isSelesai }
    val isDayFull = completedDayCount == item.prayerItems.size && item.prayerItems.isNotEmpty()

    val cardBorderColor = when {
        isDayFull -> TealAccent.copy(alpha = 0.5f)
        item.prayerItems.any { it.lockState == TaskLockState.ACTIVE } -> GoldPrimary.copy(alpha = 0.5f)
        else -> CardElevated.copy(alpha = 0.4f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
            .testTag("task_item_${item.task.dayIndex}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Hari & Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isDayFull -> TealAccent.copy(alpha = 0.15f)
                        item.prayerItems.any { it.lockState == TaskLockState.ACTIVE } -> GoldPrimary.copy(alpha = 0.15f)
                        else -> MidnightLayer
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "H-${item.task.dayIndex}",
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isDayFull -> TealAccent
                                item.prayerItems.any { it.lockState == TaskLockState.ACTIVE } -> GoldPrimary
                                else -> TextSecondary
                            }
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.task.title.ifEmpty { "Tugas Hari ke-${item.task.dayIndex}" },
                        fontSize = 14.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        fontFamily = NunitoFont,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDayFull) TealAccent.copy(alpha = 0.15f) else MidnightLayer
                ) {
                    Text(
                        text = "$completedDayCount/6 Ibadah",
                        fontSize = 11.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = if (isDayFull) TealAccent else GoldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = CardElevated.copy(alpha = 0.6f))
            Spacer(Modifier.height(6.dp))

            // 6 Sub-items Waktu Sholat & Tarawih
            item.prayerItems.forEach { prayer ->
                PrayerItemRow(
                    dayIndex = item.task.dayIndex,
                    prayer = prayer,
                    onToggle = { onTogglePrayer(prayer) }
                )
            }
        }
    }
}

@Composable
private fun PrayerItemRow(
    dayIndex: Int,
    prayer: PrayerSubmissionUiModel,
    onToggle: () -> Unit
) {
    val isClickable = prayer.lockState == TaskLockState.ACTIVE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isClickable, onClick = onToggle)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prayer.label,
                fontSize = 13.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                color = if (prayer.isSelesai) TextPrimary else if (prayer.lockState == TaskLockState.ACTIVE) GoldLight else TextSecondary
            )

            when (prayer.lockState) {
                TaskLockState.ACTIVE -> {
                    Text(
                        text = if (prayer.isSelesai) "✓ Selesai hari ini" else "○ Waktu aktif, sentuh untuk mencentang",
                        fontSize = 10.sp,
                        fontFamily = NunitoFont,
                        color = if (prayer.isSelesai) TealAccent else GoldPrimary
                    )
                }
                TaskLockState.EXPIRED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = if (prayer.isSelesai) TealAccent else WarningAmber,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = if (prayer.isSelesai) "Selesai (Terkunci)" else "Sudah lewat waktu",
                            fontSize = 10.sp,
                            fontFamily = NunitoFont,
                            color = if (prayer.isSelesai) TealAccent else WarningAmber
                        )
                    }
                }
                TaskLockState.NOT_STARTED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "Belum waktunya",
                            fontSize = 10.sp,
                            fontFamily = NunitoFont,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = onToggle,
            enabled = isClickable,
            modifier = Modifier
                .size(36.dp)
                .testTag("task_checkbox_${dayIndex}_${prayer.prayerType}")
        ) {
            if (prayer.isSelesai) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selesai",
                    tint = TealAccent,
                    modifier = Modifier.size(24.dp)
                )
            } else if (prayer.lockState == TaskLockState.ACTIVE) {
                Icon(
                    imageVector = Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Belum selesai",
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Terkunci",
                    tint = TextSecondary.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
