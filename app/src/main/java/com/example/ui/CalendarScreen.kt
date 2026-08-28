package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.IslamicHoliday
import id.ideahousetech.prayertime_qibla.ui.components.PremiumTopBar
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import id.ideahousetech.prayertime_qibla.utils.HijriDayGridItem
import java.util.Calendar

@Composable
fun CalendarScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendar = remember { Calendar.getInstance() }
    val currentHijri = remember { HijriDateUtils.convertToHijri(currentCalendar) }

    var activeMonth by remember { mutableStateOf(currentHijri.month) }
    var activeYear by remember { mutableStateOf(currentHijri.year) }
    var selectedHoliday by remember { mutableStateOf<IslamicHoliday?>(null) }

    val monthsList = listOf(
        "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir", 
        "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban", 
        "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
    )

    val gridItems = remember(activeMonth, activeYear) {
        HijriDateUtils.getHijriMonthGrid(activeMonth, activeYear)
    }

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
                title = "KALENDER ISLAMI",
                onBack = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Section Title (Gold, Uppercase, Spaced)
                Text(
                    text = "PENANGGALAN HIJRIAH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldDim,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 2. Navigasi Bulan / Tahun Panel
                val masehiMonthRange = remember(activeMonth, activeYear) {
                    var rangeDisplay = "Mei - Juni 2026"
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        try {
                            val hijrahDateFirst = java.time.chrono.HijrahDate.of(activeYear, activeMonth, 1)
                            val localDateFirst = java.time.LocalDate.ofEpochDay(hijrahDateFirst.toEpochDay())
                            val startDay = localDateFirst.dayOfMonth
                            val startMonth = localDateFirst.monthValue
                            val startYear = localDateFirst.year

                            val length = hijrahDateFirst.lengthOfMonth()
                            val localDateLast = localDateFirst.plusDays(length.toLong() - 1)
                            val endDay = localDateLast.dayOfMonth
                            val endMonth = localDateLast.monthValue
                            val endYear = localDateLast.year

                            val monthsIndo = listOf(
                                "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                            )

                            rangeDisplay = if (startMonth == endMonth) {
                                "$startDay - $endDay ${monthsIndo[startMonth - 1]} $startYear"
                            } else {
                                if (startYear == endYear) {
                                    "$startDay ${monthsIndo[startMonth - 1]} - $endDay ${monthsIndo[endMonth - 1]} $startYear"
                                } else {
                                    "$startDay ${monthsIndo[startMonth - 1]} $startYear - $endDay ${monthsIndo[endMonth - 1]} $endYear"
                                }
                            }
                        } catch (t: Throwable) {
                            android.util.Log.e("CalendarScreen", "Gagal menghitung rentang bulan masehi: ${t.message}")
                        }
                    }
                    rangeDisplay
                }

                CalendarNavigationHeader(
                    monthName = monthsList[activeMonth - 1],
                    yearStr = masehiMonthRange,
                    onPreviousClick = {
                        if (activeMonth == 1) {
                            activeMonth = 12
                            activeYear -= 1
                        } else {
                            activeMonth -= 1
                        }
                    },
                    onNextClick = {
                        if (activeMonth == 12) {
                            activeMonth = 1
                            activeYear += 1
                        } else {
                            activeMonth += 1
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Weekday Labels
                WeekdayLabelsRow()

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Kalender Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gridItems) { item ->
                        CalendarGridCell(
                            item = item,
                            isToday = item.hDay == currentHijri.day && item.hMonth == currentHijri.month && item.hYear == currentHijri.year,
                            onClick = {
                                if (item.holidayName != null) {
                                    val fullHoliday = HijriDateUtils.checkHoliday(item.hDay, item.hMonth)
                                    selectedHoliday = fullHoliday ?: IslamicHoliday(
                                        hijriDate = "%02d-%02d".format(item.hDay, item.hMonth),
                                        name = item.holidayName,
                                        description = item.holidayDescription ?: ""
                                    )
                                }
                            }
                        )
                    }
                }

                // 4b. List Hari Besar Bulan Ini di bawah kalender tanggal
                val monthlyHolidays = remember(gridItems) {
                    gridItems.filter { !it.isPadding && it.holidayName != null }
                }

                if (monthlyHolidays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.dp, DividerLine),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "HARI BESAR BULAN INI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                            )
                            monthlyHolidays.forEach { hItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            val fullHoliday = HijriDateUtils.checkHoliday(hItem.hDay, hItem.hMonth)
                                            selectedHoliday = fullHoliday ?: IslamicHoliday(
                                                hijriDate = "%02d-%02d".format(hItem.hDay, hItem.hMonth),
                                                name = hItem.holidayName ?: "",
                                                description = hItem.holidayDescription ?: ""
                                            )
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(GoldGlow, CircleShape)
                                            .border(1.dp, GoldPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🕌", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hItem.holidayName ?: "",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${hItem.hDay} ${monthsList[activeMonth - 1]} (${hItem.mDayLabel})",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog popup detil
        if (selectedHoliday != null) {
            HolidayDialog(
                holiday = selectedHoliday!!,
                onDismiss = { selectedHoliday = null }
            )
        }
    }
}

@Composable
fun CalendarNavigationHeader(
    monthName: String,
    yearStr: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, DividerLine),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Bulan Sebelumnya",
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = monthName,
                    fontSize = 18.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = yearStr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Bulan Berikutnya",
                    tint = GoldPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun WeekdayLabelsRow() {
    val days = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface, RoundedCornerShape(8.dp))
            .border(
                BorderStroke(1.dp, DividerLine),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { dayName ->
            Text(
                text = dayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (dayName == "Min") WarningAmber else TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

@Composable
fun CalendarGridCell(
    item: HijriDayGridItem,
    isToday: Boolean,
    onClick: () -> Unit
) {
    if (item.isPadding) {
        Box(modifier = Modifier.aspectRatio(1f))
        return
    }

    val isHoliday = item.holidayName != null

    val bgModifier = when {
        isToday -> Modifier.background(GoldPrimary, RoundedCornerShape(12.dp))
        isHoliday -> Modifier.background(GoldGlow, RoundedCornerShape(12.dp))
        else -> Modifier.background(CardSurface, RoundedCornerShape(12.dp))
    }

    val borderModifier = when {
        isToday -> Modifier.border(1.dp, GoldLight, RoundedCornerShape(12.dp))
        isHoliday -> Modifier.border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
        else -> Modifier.border(1.dp, DividerLine, RoundedCornerShape(12.dp))
    }

    val mainTextColor = when {
        isToday -> DeepNight
        isHoliday -> GoldLight
        else -> TextPrimary
    }

    val subTextColor = when {
        isToday -> DeepNight.copy(alpha = 0.75f)
        isHoliday -> TextSecondary
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .then(bgModifier)
            .then(borderModifier)
            .clickable(enabled = isHoliday) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = item.hDay.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = mainTextColor
            )
            Text(
                text = item.mDayLabel,
                fontSize = 10.sp,
                color = subTextColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 1.dp)
            )
            if (isHoliday) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(WarningAmber, CircleShape)
                )
            }
        }
    }
}
