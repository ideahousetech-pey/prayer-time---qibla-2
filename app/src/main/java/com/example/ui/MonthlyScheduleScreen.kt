package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.ui.components.PremiumTopBar
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel
import java.util.Calendar

@Composable
fun MonthlyScheduleScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthlySchedule by prayerViewModel.monthlySchedule.collectAsState()
    val locationName by locationViewModel.locationName.collectAsState()

    val currentDayOfMonth = rememberCurrentDayOfMonth()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumTopBar(
                title = "PERIODIK BULANAN",
                onBack = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section label
                Text(
                    text = "JADWAL SHOLAT 30 HARI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldDim,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Lokasi Info GPS aktif
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Posisi",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (locationName.isEmpty()) "Mencari Lokasi..." else locationName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Table Headers Row
                TableHeadersRow()

                Spacer(modifier = Modifier.height(8.dp))

                // Table Data Rows
                if (monthlySchedule.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(monthlySchedule) { index, item ->
                            val isToday = (index + 1) == currentDayOfMonth
                            TableRowItemCard(item = item, dayIndex = index + 1, isToday = isToday)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mempersiapkan data tabel jadwal bulanan...",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeadersRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface, RoundedCornerShape(12.dp))
            .border(
                BorderStroke(1.dp, DividerLine),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tgl Masehi",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            modifier = Modifier.weight(1.8f)
        )
        Text(
            text = "Subuh",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Dzuhur",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Ashar",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Magrib",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Isya",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TableRowItemCard(
    item: PrayerTime,
    dayIndex: Int,
    isToday: Boolean
) {
    val containerBg = if (isToday) GoldPrimary else CardSurface
    val cardBorder = if (isToday) BorderStroke(1.dp, GoldLight) else BorderStroke(1.dp, DividerLine)
    val textColor = if (isToday) DeepNight else TextPrimary
    val dateTextColor = if (isToday) DeepNight else TextSecondary

    val formattedDateStr = simplifyDateString(item.dateGregorian)

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = cardBorder,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattedDateStr,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = dateTextColor,
                modifier = Modifier.weight(1.8f)
            )

            Text(
                text = item.fajr,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.dhuhr,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.asr,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.maghrib,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.isha,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun simplifyDateString(raw: String): String {
    return try {
        val parts = raw.split(",")
        val dayName = parts[0].substring(0, 3).trim() // Rab
        val dateBody = parts[1].replace(" 2026", "").trim() // 13 Mei
        "$dateBody ($dayName)"
    } catch (e: Exception) {
        raw
    }
}

@Composable
fun rememberCurrentDayOfMonth(): Int {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.DAY_OF_MONTH)
}
