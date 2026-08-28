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
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.StudentTaskViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.TaskItemUiModel
import id.ideahousetech.prayertime_qibla.viewmodel.TaskLockState
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Layar Lembar Tugas Ramadhan 30 Hari untuk Siswa.
 * Menampilkan checklist harian dengan penegakan validasi lock berdasarkan taskDate.
 */
@Composable
fun StudentTaskScreen(
    viewModel: StudentTaskViewModel,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadData()
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
                                text = uiState.student?.name ?: "Siswa Ramadhan",
                                fontSize = 16.sp,
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
                                color = GoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        // Badge Progres Selesai
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealAccent.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${uiState.completedCount}/${uiState.totalCount} Selesai",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
                ) {
                    items(uiState.taskItems, key = { it.task.taskId }) { item ->
                        TaskItemCard(
                            item = item,
                            onToggle = { viewModel.toggleTask(item) }
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
    onToggle: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }
    val formattedDate = remember(item.task.taskDate) {
        try {
            dateFormat.format(item.task.taskDate.toDate())
        } catch (_: Exception) {
            "-"
        }
    }

    val isClickable = item.lockState == TaskLockState.ACTIVE
    val cardBorderColor = when {
        item.isSelesai -> TealAccent.copy(alpha = 0.5f)
        item.lockState == TaskLockState.ACTIVE -> GoldPrimary.copy(alpha = 0.5f)
        else -> CardElevated.copy(alpha = 0.4f)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = isClickable, onClick = onToggle)
            .testTag("task_item_${item.task.dayIndex}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = when {
                    item.isSelesai -> TealAccent.copy(alpha = 0.15f)
                    item.lockState == TaskLockState.ACTIVE -> GoldPrimary.copy(alpha = 0.15f)
                    else -> MidnightLayer
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "H-${item.task.dayIndex}",
                        fontSize = 12.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            item.isSelesai -> TealAccent
                            item.lockState == TaskLockState.ACTIVE -> GoldPrimary
                            else -> TextSecondary
                        }
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

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

                Spacer(Modifier.height(4.dp))

                // Keterangan Status Lock
                when (item.lockState) {
                    TaskLockState.ACTIVE -> {
                        Text(
                            text = if (item.isSelesai) "✓ Selesai dikerjakan hari ini" else "○ Buka checklist untuk menyelesaikan",
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            color = if (item.isSelesai) TealAccent else GoldPrimary
                        )
                    }
                    TaskLockState.EXPIRED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (item.isSelesai) "Selesai (Sudah lewat hari, terkunci)" else "Sudah lewat hari, tidak bisa diubah",
                                fontSize = 11.sp,
                                fontFamily = NunitoFont,
                                color = if (item.isSelesai) TealAccent else WarningAmber
                            )
                        }
                    }
                    TaskLockState.NOT_STARTED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Belum waktunya",
                                fontSize = 11.sp,
                                fontFamily = NunitoFont,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Checkbox Icon
            IconButton(
                onClick = onToggle,
                enabled = isClickable,
                modifier = Modifier.testTag("task_checkbox_${item.task.dayIndex}")
            ) {
                if (item.isSelesai) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selesai",
                        tint = TealAccent,
                        modifier = Modifier.size(28.dp)
                    )
                } else if (item.lockState == TaskLockState.ACTIVE) {
                    Icon(
                        imageVector = Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Belum selesai",
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.RadioButtonUnchecked,
                        contentDescription = "Terkunci",
                        tint = TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
