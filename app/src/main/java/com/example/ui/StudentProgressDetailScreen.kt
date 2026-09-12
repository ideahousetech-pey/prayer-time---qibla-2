package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.ramadhan.Student
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.TaskItemUiModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen Detail Progres Amaliah Ramadhan Siswa (Read-Only 30 Hari).
 * Digunakan oleh Guru & Koordinator untuk memantau detail pengerjaan siswa.
 */
@Composable
fun StudentProgressDetailScreen(
    student: Student,
    taskItems: List<TaskItemUiModel>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = taskItems.sumOf { it.prayerItems.count { p -> p.isSelesai } }
    val totalCount = taskItems.sumOf { it.prayerItems.size }

    Box(
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                        .testTag("student_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Detail Progres Siswa",
                    fontSize = 18.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Student Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
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
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            fontSize = 16.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Lembar Amaliah Ramadhan (Read-Only)",
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TealAccent.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TealAccent.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "$completedCount/$totalCount Ibadah Tercatat",
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = TealAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    items(taskItems, key = { it.task.taskId }) { item ->
                        ReadOnlyTaskCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyTaskCard(item: TaskItemUiModel) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")) }
    val dateStr = remember(item.task.taskDate) {
        try {
            dateFormat.format(item.task.taskDate.toDate())
        } catch (_: Exception) {
            "-"
        }
    }

    val completedDayCount = item.prayerItems.count { it.isSelesai }
    val isDayFull = completedDayCount == item.prayerItems.size && item.prayerItems.isNotEmpty()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDayFull) TealAccent.copy(alpha = 0.45f) else CardElevated,
                RoundedCornerShape(14.dp)
            )
            .testTag("read_only_task_${item.task.dayIndex}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: H-{dayIndex}, Judul, Tanggal, & Ringkasan Harian
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDayFull) TealAccent.copy(alpha = 0.15f) else MidnightLayer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "H-${item.task.dayIndex}",
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = if (isDayFull) TealAccent else TextSecondary
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.task.title.ifEmpty { "Tugas Hari ke-${item.task.dayIndex}" },
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dateStr,
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
                        text = "$completedDayCount/${item.prayerItems.size} Ibadah",
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

            // 6 Baris Sub-Item Waktu Sholat & Tarawih (Read-Only)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item.prayerItems.forEach { prayer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (prayer.isSelesai) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = if (prayer.isSelesai) "Selesai" else "Belum",
                            tint = if (prayer.isSelesai) TealAccent else TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = prayer.label,
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            fontWeight = if (prayer.isSelesai) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (prayer.isSelesai) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (prayer.isSelesai) "Selesai" else "Belum",
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            fontWeight = if (prayer.isSelesai) FontWeight.Bold else FontWeight.Normal,
                            color = if (prayer.isSelesai) TealAccent else TextSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
