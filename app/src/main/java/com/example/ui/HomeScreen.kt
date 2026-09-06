package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.BuildConfig
import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.ui.components.DailyInsightSection
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel

/**
 * Screen Utama Aplikasi Waktu Sholat & Kiblat (Sederhana & Berkinerja Tinggi).
 * Desain disederhanakan secara radikal untuk mengurangi scrolling, visual noise, mementingkan
 * readability, serta mematikan animasi kanvas berulang yang memakan daya baterai berlebih.
 */
@Composable
fun HomeScreen(
    prayerViewModel: PrayerViewModel,
    locationViewModel: LocationViewModel,
    trackerViewModel: id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModel,
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier,
    onRamadhanClick: (() -> Unit)? = null
) {
    val todayGregorian by prayerViewModel.todayGregorian.collectAsState()
    val todayHijri by prayerViewModel.todayHijri.collectAsState()
    val locationName by locationViewModel.locationName.collectAsState()
    val todaySchedule by prayerViewModel.todaySchedule.collectAsState()
    val countdown by prayerViewModel.countdownString.collectAsState()
    val nextPrayerName by prayerViewModel.nextPrayerName.collectAsState()
    val currentHolidayPopUp by prayerViewModel.currentHolidayPopUp.collectAsState()

    val showRamadhanFeature = BuildConfig.FORCE_SHOW_RAMADHAN_FEATURE || HijriDateUtils.isRamadhanFeatureWindowActive()

    val userLocation by locationViewModel.userLocation.collectAsState()
    val isLoadingLoc by locationViewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val adzanPrefs = remember { SecurePrefs.get(context) }
    var enableDailyReminder by remember { mutableStateOf(adzanPrefs.getBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, true)) }

    LaunchedEffect(Unit) {
        enableDailyReminder = adzanPrefs.getBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, true)
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    id.ideahousetech.prayertime_qibla.ui.components.DynamicPrayerBackground(
        prayerName = nextPrayerName,
        modifier = modifier.fillMaxSize()
    ) {
        // PERFORMANCY AUDIT SUCCESS: Kanvas looping Girih bintang 8-titik yang memakan daya baterai
        // telah dihilangkan sepenuhnya, diganti dng latar gradasi Emerald murni berkinerja tinggi.

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 14 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // 1. Header Ringkas (Maksimal 72dp)
                HomeHeader(
                    gregorianDate       = todayGregorian,
                    hijriDate           = todayHijri,
                    locationName        = locationName,
                    isLoading           = isLoadingLoc,
                    showRamadhanFeature = showRamadhanFeature,
                    onRamadhanClick     = onRamadhanClick ?: { onNavigateToScreen(AppScreen.RAMADHAN_ENTRY) }
                )

                // 2. Hero Card Waktu Sholat Berikutnya (Maksimal 180dp)
                NextPrayerHeroCard(
                    nextPrayerName = nextPrayerName,
                    countdown      = countdown,
                    todaySchedule  = todaySchedule
                )

                // 3. Jadwal Sholat 5 Waktu Hari Ini (Linear Ringkas)
                TodayPrayerTimesRow(
                    todaySchedule  = todaySchedule,
                    nextPrayerName = nextPrayerName
                )

                // 4. Insight Renungan Harian (Ekspandabel, Minim Ruang/Scroll)
                DailyInsightSection()

                if (enableDailyReminder) {
                    ReminderNoteCard()
                }

                Spacer(Modifier.height(10.dp))
            }
        }

        // Dialog PopUp Hari Kebesaran Islam
        currentHolidayPopUp?.let { holiday ->
            HolidayDialog(
                holiday = holiday,
                onDismiss = { prayerViewModel.dismissHolidayPopUp() }
            )
        }
    }
}
