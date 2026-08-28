package id.ideahousetech.prayertime_qibla.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.ui.StatusPickerDialog
import id.ideahousetech.prayertime_qibla.ui.IslamicGlassCard
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModel
import id.ideahousetech.prayertime_qibla.model.PrayerStatus
import id.ideahousetech.prayertime_qibla.model.PrayerName
import kotlinx.coroutines.flow.map

/**
 * Kartu shortcut pencatatan cepat (Takhrij Tracker Harian) langsung dari HomeScreen.
 * Menghadirkan korelasi luring, visual mewah, pencatatan micro-interaction memuaskan,
 * serta akses cepat menuju dashboard komprehensif.
 */
@Composable
fun PrayerTrackerQuickCard(
    trackerViewModel: PrayerTrackerViewModel,
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayDate = remember { trackerViewModel.getTodayDateString() }
    
    // Tarik list all tracker untuk dicarikan data penegakan hari ini
    val allTrackers by trackerViewModel.allTrackers.collectAsState()
    val todayTracker = remember(allTrackers, todayDate) {
        allTrackers.find { it.date == todayDate }
    }
    
    val streakCount by trackerViewModel.streakCount.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedPrayerName by remember { mutableStateOf("") }
    var currentStatus by remember { mutableStateOf("") }

    IslamicGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header panel: title, shortcut dashboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAddCheck,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "PELACAK SHOLAT HARIAN",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                }
                
                // Shortcut detail button to open comprehensive view
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToScreen(AppScreen.TRACKER) }
                ) {
                    Text(
                        text = "Dashboard",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Progress status text & live active streaks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val list = listOf(
                    todayTracker?.subuhStatus ?: "None",
                    todayTracker?.dhuhrStatus ?: "None",
                    todayTracker?.asrStatus ?: "None",
                    todayTracker?.maghribStatus ?: "None",
                    todayTracker?.isyaStatus ?: "None"
                )
                val completedCount = list.count { it != "None" }
                
                Text(
                    text = "Hari Ini: $completedCount dari 5 Sholat dicatat",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )

                if (streakCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = Color(0xFFEF6C00),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$streakCount HARI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF6C00)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Interactive horizontal capsules for the 5 prayers today
            val quickPrayers = listOf(
                PrayerName.SUBUH.indonesianName to (todayTracker?.subuhStatus ?: PrayerStatus.NONE),
                PrayerName.DZUHUR.indonesianName to (todayTracker?.dhuhrStatus ?: PrayerStatus.NONE),
                PrayerName.ASHAR.indonesianName to (todayTracker?.asrStatus ?: PrayerStatus.NONE),
                PrayerName.MAGHRIB.indonesianName to (todayTracker?.maghribStatus ?: PrayerStatus.NONE),
                PrayerName.ISYA.indonesianName to (todayTracker?.isyaStatus ?: PrayerStatus.NONE)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrayers.forEach { (name, status) ->
                    val isDone = status != PrayerStatus.NONE
                    val itemBgColor = when (status) {
                        PrayerStatus.JAMAAH -> TealAccent.copy(alpha = 0.15f)
                        PrayerStatus.MUNFARID -> GoldPrimary.copy(alpha = 0.12f)
                        PrayerStatus.MASBUQ -> Color(0xFFFFB74D).copy(alpha = 0.12f)
                        PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.08f)
                        else -> MidnightLayer.copy(alpha = 0.5f)
                    }
                    val itemBorderColor = when (status) {
                        PrayerStatus.JAMAAH -> TealAccent.copy(alpha = 0.5f)
                        PrayerStatus.MUNFARID -> GoldPrimary.copy(alpha = 0.5f)
                        PrayerStatus.MASBUQ -> Color(0xFFFFB74D).copy(alpha = 0.5f)
                        PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.2f)
                        else -> GoldPrimary.copy(alpha = 0.12f)
                    }
                    val textTint = when (status) {
                        PrayerStatus.JAMAAH -> TealAccent
                        PrayerStatus.MUNFARID -> GoldPrimary
                        PrayerStatus.MASBUQ -> Color(0xFFFFB74D)
                        PrayerStatus.HALANGAN -> Color.White.copy(alpha = 0.6f)
                        else -> TextSecondary
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(CornerMedium))
                            .background(itemBgColor)
                            .border(1.dp, itemBorderColor, RoundedCornerShape(CornerMedium))
                            .clickable {
                                selectedPrayerName = name
                                currentStatus = status.displayName
                                showDialog = true
                            }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = name,
                            tint = textTint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDone) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        StatusPickerDialog(
            prayerName = selectedPrayerName,
            currentStatus = currentStatus,
            onDismiss = { showDialog = false },
            onStatusSelected = { newStatus ->
                trackerViewModel.updatePrayerStatus(todayDate, selectedPrayerName, newStatus)
                showDialog = false
                Toast.makeText(context, "Status $selectedPrayerName diperbarui!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
