package id.ideahousetech.prayertime_qibla.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.KeyStoreDiagnostics
import id.ideahousetech.prayertime_qibla.utils.PrefsMigration
import id.ideahousetech.prayertime_qibla.utils.FileSecurityUtils
import id.ideahousetech.prayertime_qibla.utils.FileValidationResult
import id.ideahousetech.prayertime_qibla.utils.TrustedAdzanDomains
import id.ideahousetech.prayertime_qibla.utils.SecureDownloadHelper
import id.ideahousetech.prayertime_qibla.utils.AppSecurityManager
import id.ideahousetech.prayertime_qibla.ui.components.SecurityStatusBadge
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel
import id.ideahousetech.prayertime_qibla.service.NotificationService
import id.ideahousetech.prayertime_qibla.service.PreviewState
import id.ideahousetech.prayertime_qibla.service.CopyProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * SettingsScreen menggantikan ProfileScreen pada Tab 5 ("Pengaturan").
 * Menyediakan antarmuka konfigurasi visual bertema Islami M3 yang kaya dan stabil.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    locationViewModel: LocationViewModel,
    prayerViewModel: PrayerViewModel,
    onReminderToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adzanSettingsViewModel: id.ideahousetech.prayertime_qibla.viewmodel.AdzanSettingsViewModel = 
        androidx.lifecycle.viewmodel.compose.viewModel(factory = id.ideahousetech.prayertime_qibla.viewmodel.AdzanSettingsViewModelFactory(context))

    val prefs = remember { SecurePrefs.get(context) }
    val scope = rememberCoroutineScope()

    val onlineAdzans = remember {
        listOf(
            OnlineAdzan("Adzan Makkah", "https://www.islamcan.com/audio/adhans/adhan1.mp3", "Adzan syahdu dari Masjidil Haram, Makkah."),
            OnlineAdzan("Adzan Madinah", "https://www.islamcan.com/audio/adhans/adhan10.mp3", "Adzan merdu dari Masjid Nabawi, Madinah."),
            OnlineAdzan("Adzan Mesir", "https://www.islamcan.com/audio/adhans/adhan13.mp3", "Adzan bernada indah khas gaya legendaris Mesir."),
            OnlineAdzan("Adzan Al-Aqsa", "https://www.islamcan.com/audio/adhans/adhan14.mp3", "Adzan khidmat menyentuh kalbu dari Masjidil Aqsa."),
            OnlineAdzan("Adzan Makkah Subuh", "https://www.islamcan.com/audio/adhans/adhan2.mp3", "Adzan khusus Subuh (dengan tambahan Ash-Shalaatu Khairum).")
        )
    }

    var isAlarmEnabled by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true)) }
    var isDailyReminderEnabled by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, true)) }
    var isStreakStrict by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.STREAK_STRICT_MODE, false)) }
    var prayerOffset by remember { mutableStateOf(prefs.getInt(PrefsKeys.PRAYER_TIME_OFFSET, 0)) }
    var appThemeMode by remember { mutableStateOf(prefs.getString(PrefsKeys.APP_THEME_MODE, "dark") ?: "dark") }

    var customAdzanName by remember { mutableStateOf(prefs.getString(PrefsKeys.CUSTOM_ADZAN_NAME, null)) }
    var customAdzanFajrName by remember { mutableStateOf(prefs.getString(PrefsKeys.CUSTOM_ADZAN_FAJR_NAME, null)) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadingName by remember { mutableStateOf("") }

    val userLocation by locationViewModel.userLocation.collectAsState()

    val notificationService = remember { NotificationService.getInstance(context) }
    val copyProgressState by notificationService.copyProgress.collectAsState()
    val previewState by notificationService.previewState.collectAsState()

    var isEncryptionActive by remember { mutableStateOf(SecurePrefs.isEncryptionActive(context)) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var diagnosticsResult by remember { mutableStateOf<KeyStoreDiagnostics.DiagnosticResult?>(null) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    fun triggerPrayerReload() {
        userLocation?.let {
            prayerViewModel.loadPrayerTimesForLocation(it.latitude, it.longitude)
        } ?: run {
            prayerViewModel.loadPrayerTimesForLocation(-6.2088, 106.8456)
        }
    }

    fun downloadAdzanOnline(adzan: OnlineAdzan, isSubuh: Boolean) {
        if (isDownloading) return
        isDownloading = true
        downloadProgress = 0f
        downloadingName = adzan.displayName + (if (isSubuh) " (Subuh)" else " (Umum)")
        scope.launch(Dispatchers.IO) {
            val targetFileName = if (isSubuh) "adzan_fajr.mp3" else "adzan.mp3"
            val targetFile = File(context.filesDir, targetFileName)
            
            var success = false
            var errorMsg = ""
            
            val urlsToTry = mutableListOf<String>()
            urlsToTry.add(adzan.url)
            
            val fallbackUrl = when (adzan.displayName) {
                "Adzan Makkah" -> "https://raw.githubusercontent.com/sidandv/My-Azan/master/Azan.mp3"
                "Adzan Madinah" -> "https://www.islamcan.com/audio/adhans/adhan10.mp3"
                "Adzan Makkah Subuh" -> "https://www.islamcan.com/audio/adhans/adhan2.mp3"
                else -> if (isSubuh) "https://www.islamcan.com/audio/adhans/adhan2.mp3" else "https://raw.githubusercontent.com/sidandv/My-Azan/master/Azan.mp3"
            }
            urlsToTry.add(fallbackUrl)
            urlsToTry.add("https://raw.githubusercontent.com/sidandv/My-Azan/master/Azan.mp3")
            
            for (attemptUrl in urlsToTry) {
                // Skor domain whitelist
                if (!TrustedAdzanDomains.isUrlTrusted(attemptUrl)) {
                    continue
                }
                
                val result = SecureDownloadHelper.downloadAdzan(context, attemptUrl, targetFile) { progress ->
                    if (progress >= 0) {
                        downloadProgress = progress
                    } else {
                        // Incremental update untuk file tanpa content length
                        downloadProgress = (downloadProgress + 0.05f).coerceAtMost(0.95f)
                    }
                }
                
                if (result.isSuccess) {
                    success = true
                    break
                } else {
                    errorMsg = result.exceptionOrNull()?.localizedMessage ?: "Unduhan gagal."
                }
            }
            
            withContext(Dispatchers.Main) {
                isDownloading = false
                if (success) {
                    val savedName = "${adzan.displayName} (Internet)"
                    if (isSubuh) {
                        prefs.edit().putString(PrefsKeys.CUSTOM_ADZAN_FAJR_NAME, savedName).apply()
                        customAdzanFajrName = savedName
                    } else {
                        prefs.edit().putString(PrefsKeys.CUSTOM_ADZAN_NAME, savedName).apply()
                        customAdzanName = savedName
                    }
                    Toast.makeText(context, "Selesai mengunduh: $savedName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal mengunduh: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var activePreview by remember { mutableStateOf<String?>(null) } // "umum" or "fajr" or null

    LaunchedEffect(previewState) {
        if (previewState is PreviewState.Ready) {
            activePreview = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            notificationService.stopPreviewAdzan()
        }
    }

    fun playPreview(fileName: String, type: String) {
        val isFajr = (type == "fajr")
        activePreview = type
        notificationService.previewAdzan(isFajr = isFajr, durationSeconds = 10)
    }

    fun stopPreview() {
        notificationService.stopPreviewAdzan()
        activePreview = null
    }

    fun saveAudioFile(uri: Uri, isFajr: Boolean) {
        // 1. Jalankan validasi keamanan file yang ketat
        when (val validationResult = FileSecurityUtils.validateAudioFile(context, uri)) {
            is FileValidationResult.Invalid -> {
                Toast.makeText(context, "File Ditolak: ${validationResult.reason}", Toast.LENGTH_LONG).show()
                return
            }
            FileValidationResult.Valid -> { /* Lolos validasi */ }
        }

        try {
            val contentResolver = context.contentResolver
            var origName = if (isFajr) "adzan_fajr_kustom.mp3" else "adzan_kustom.mp3"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        val retrievedName = cursor.getString(nameIdx)
                        if (retrievedName != null) {
                            origName = retrievedName
                        }
                    }
                }
            }

            // Sanitasi nama file secara menyeluruh demi keamanan Path Traversal
            val sanitizedName = FileSecurityUtils.sanitizeFileName(origName)

            val targetFileName = if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
            val targetFile = File(context.filesDir, targetFileName)
            val tempFile = File(context.cacheDir, "temp_upload_save_${System.currentTimeMillis()}.mp3")

            // Salin ke temp file terlebih dahulu sebelum dipindahkan ke area produksi
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.exists()) {
                if (targetFile.exists()) targetFile.delete()
                if (!tempFile.renameTo(targetFile)) {
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }
            } else {
                throw Exception("Gagal membuat salinan temporer berkas.")
            }

            if (isFajr) {
                prefs.edit().putString(PrefsKeys.CUSTOM_ADZAN_FAJR_NAME, sanitizedName).apply()
                customAdzanFajrName = sanitizedName
            } else {
                prefs.edit().putString(PrefsKeys.CUSTOM_ADZAN_NAME, sanitizedName).apply()
                customAdzanName = sanitizedName
            }

            Toast.makeText(context, "Berhasil diunggah: $sanitizedName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memproses file audio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun deleteCustomAudio(isFajr: Boolean) {
        val targetFileName = if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
        val targetFile = File(context.filesDir, targetFileName)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        if (isFajr) {
            prefs.edit().remove(PrefsKeys.CUSTOM_ADZAN_FAJR_NAME).apply()
            customAdzanFajrName = null
        } else {
            prefs.edit().remove(PrefsKeys.CUSTOM_ADZAN_NAME).apply()
            customAdzanName = null
        }
        stopPreview()
        Toast.makeText(context, "Suara adzan dikembalikan ke bawaan", Toast.LENGTH_SHORT).show()
    }

    val launcherUmum = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveAudioFile(it, isFajr = false) }
    }

    val launcherFajr = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveAudioFile(it, isFajr = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. SETTINGS HEADER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(GoldGlow)
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "PENGATURAN RUHANI",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Kalibrasi jadwal sholat, adzan, lokasi & visual",
                        fontFamily = NunitoFont,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // DOWNLOAD PROGRESS BAR
            if (isDownloading) {
                item {
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mengunduh: $downloadingName",
                                    fontSize = 12.sp,
                                    fontFamily = CinzelFont,
                                    color = GoldLight,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontFamily = NunitoFont,
                                    color = TealAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = GoldPrimary,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            // 2. VISUAL THEME SELECTOR
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionHeader(title = "TEMA & MODE TAMPILAN", icon = Icons.Outlined.LightMode)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Ubah skema warna tampilan antarmuka.",
                                fontSize = 10.sp,
                                fontFamily = NunitoFont,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val modes = listOf(
                                    "dark" to "Gelap 🌙",
                                    "light" to "Terang ☀️",
                                    "system" to "Sistem 🔄"
                                )
                                modes.forEach { (modeKey, modeName) ->
                                    val isSelected = appThemeMode == modeKey
                                    val systemIsDark = androidx.compose.foundation.isSystemInDarkTheme()
                                    Button(
                                        onClick = {
                                            prefs.edit().putString(PrefsKeys.APP_THEME_MODE, modeKey).apply()
                                            id.ideahousetech.prayertime_qibla.ui.theme.AppThemeState.updateTheme(modeKey, systemIsDark)
                                            appThemeMode = modeKey
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) GoldPrimary else DividerLine,
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) GoldPrimary else CardElevated,
                                            contentColor = if (isSelected) (if (AppThemeState.isDarkTheme) StaticDeepNight else Color.White) else TextSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = modeName,
                                            fontSize = 11.sp,
                                            fontFamily = NunitoFont,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. ALARM & REMINDER TOGGLES
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionHeader(title = "SUARA ALARM & REMINDER", icon = Icons.Outlined.NotificationsActive)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Alarm switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Putar Suara Alarm Adzan",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Suara berkumandang otomatis saat masuk shalat",
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = isAlarmEnabled,
                                    onCheckedChange = { checked ->
                                        prefs.edit().putBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, checked).apply()
                                        isAlarmEnabled = checked
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldPrimary,
                                        checkedTrackColor = TealDim,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = CardElevated,
                                        uncheckedBorderColor = DividerLine
                                    )
                                )
                            }

                            // Exact alarm badge
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                if (!alarmManager.canScheduleExactAlarms()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(WarningAmber.copy(alpha = 0.12f))
                                            .border(0.5.dp, WarningAmber.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                val intent = Intent(
                                                    android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                                context.startActivity(intent)
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = WarningAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Ketuk untuk mengaktifkan izin Alarm Tepat Waktu.",
                                                fontSize = 9.sp,
                                                fontFamily = NunitoFont,
                                                color = WarningAmber,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                            // Daily citation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kutipan Amalan & Hikmah",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tampilkan ayat kalbu di layar beranda jemaah",
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = isDailyReminderEnabled,
                                    onCheckedChange = { checked ->
                                        prefs.edit().putBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, checked).apply()
                                        isDailyReminderEnabled = checked
                                        onReminderToggle(checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldPrimary,
                                        checkedTrackColor = TealDim,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = CardElevated,
                                        uncheckedBorderColor = DividerLine
                                    )
                                )
                            }

                            HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                            // Streak Mode (Strict vs Lenient)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mode Streak Ketat (Strict)",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (isStreakStrict) {
                                            "Hari tanpa catatan akan memutuskan streak ketaatan Anda."
                                        } else {
                                            "Hari tanpa catatan dilewati (tidak memutuskan streak Anda)."
                                        },
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = isStreakStrict,
                                    onCheckedChange = { checked ->
                                        prefs.edit().putBoolean(PrefsKeys.STREAK_STRICT_MODE, checked).apply()
                                        isStreakStrict = checked
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldPrimary,
                                        checkedTrackColor = TealDim,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = CardElevated,
                                        uncheckedBorderColor = DividerLine
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // PENGATURAN SUARA ADZAN SECTION
            item {
                AdzanSettingsSection(viewModel = adzanSettingsViewModel)
            }

            // 4. MANUAL LOCATION CALIBRATIONS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionHeader(title = "LOKASI & KOORDINAT JADWAL", icon = Icons.Outlined.Place)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        val isManualLoc by locationViewModel.isManualLocation.collectAsState()
                        val currentAddress by locationViewModel.locationName.collectAsState()

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isManualLoc) "Kota Manual: $currentAddress" else "GPS Otomatis Aktif",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                    Text(
                                        text = if (isManualLoc) "Mengunci jadwal sholat kota tertentu" else "Mendapatkan satelit GPS secara realtime",
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!isManualLoc) {
                                        IconButton(
                                            onClick = {
                                                locationViewModel.refreshLocation()
                                                triggerPrayerReload()
                                                Toast.makeText(context, "Mencari koordinat satelit...", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .size(30.dp)
                                                .background(TealDim.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Sync GPS",
                                                tint = TealAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (isManualLoc) {
                                                locationViewModel.setAutoLocation()
                                                triggerPrayerReload()
                                                Toast.makeText(context, "Satelit GPS otomatis aktif.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                locationViewModel.setManualLocation("Jakarta", -6.2088, 106.8456)
                                                triggerPrayerReload()
                                                Toast.makeText(context, "Kota manual diatur ke Jakarta.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isManualLoc) TealAccent else CardElevated
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = if (isManualLoc) "GPS Aktif" else "Set Manual",
                                            fontSize = 9.sp,
                                            fontFamily = NunitoFont,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isManualLoc) DeepNight else GoldPrimary
                                        )
                                    }
                                }
                            }

                            if (isManualLoc) {
                                val indonesianCities = mapOf(
                                    "Jakarta" to Pair(-6.2088, 106.8456),
                                    "Surabaya" to Pair(-7.2575, 112.7521),
                                    "Bandung" to Pair(-6.9175, 107.6191),
                                    "Medan" to Pair(3.5952, 98.6722),
                                    "Bekasi" to Pair(-6.2349, 106.9896),
                                    "Depok" to Pair(-6.4025, 106.7942),
                                    "Tangerang" to Pair(-6.1702, 106.6400),
                                    "Semarang" to Pair(-6.9932, 110.4203),
                                    "Palembang" to Pair(-2.9761, 104.7754),
                                    "Makassar" to Pair(-5.1477, 119.4327),
                                    "Yogyakarta" to Pair(-7.7971, 110.3688),
                                    "Bogor" to Pair(-6.5971, 106.8060),
                                    "Batam" to Pair(1.0457, 104.0305),
                                    "Pekanbaru" to Pair(0.5071, 101.4478),
                                    "Banjarmasin" to Pair(-3.3194, 114.5908),
                                    "Pontianak" to Pair(-0.0263, 109.3425),
                                    "Samarinda" to Pair(-0.5021, 117.1536),
                                    "Manado" to Pair(1.4748, 124.8421),
                                    "Denpasar" to Pair(-8.6705, 115.2126),
                                    "Aceh" to Pair(5.5483, 95.3238)
                                )

                                var searchQuery by remember { mutableStateOf("") }
                                val filteredCities = remember(searchQuery) {
                                    indonesianCities.keys.filter { it.contains(searchQuery, ignoreCase = true) }
                                }

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Cari kota Indonesia...", color = TextSecondary, fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = DividerLine,
                                        focusedContainerColor = CardElevated,
                                        unfocusedContainerColor = CardElevated,
                                        disabledContainerColor = CardElevated
                                    )
                                )

                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 120.dp)
                                        .background(CardElevated.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                        .padding(4.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 110.dp)
                                    ) {
                                        items(filteredCities) { city ->
                                            val coords = indonesianCities[city]!!
                                            val isSelected = currentAddress.contains(city, ignoreCase = true)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        locationViewModel.setManualLocation(city, coords.first, coords.second)
                                                        triggerPrayerReload()
                                                        Toast.makeText(context, "Selesai set manual kota: $city", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .background(
                                                        if (isSelected) GoldGlow.copy(alpha = 0.2f) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = city,
                                                    color = if (isSelected) GoldPrimary else TextPrimary,
                                                    fontSize = 11.sp,
                                                    fontFamily = CinzelFont,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = "${coords.first}, ${coords.second}",
                                                    color = TextSecondary,
                                                    fontFamily = NunitoFont,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. MANUAL MINUTE OFFSET CALIBRATIONS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionHeader(title = "KOREKSI WAKTU MANUAL", icon = Icons.Outlined.EditCalendar)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "Lakukan kalibrasi manual jadwal sholat (menit) jika terdapat deviasi dengan otoritas setempat.",
                                fontSize = 10.sp,
                                fontFamily = NunitoFont,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (prayerOffset > -15) {
                                            prayerOffset -= 1
                                            prefs.edit().putInt(PrefsKeys.PRAYER_TIME_OFFSET, prayerOffset).apply()
                                            triggerPrayerReload()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DividerLine,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("-1m", fontWeight = FontWeight.Black, fontSize = 11.sp, fontFamily = NunitoFont)
                                }

                                Text(
                                    text = if (prayerOffset == 0) "Standar Otoritas" else if (prayerOffset > 0) "+$prayerOffset Menit" else "$prayerOffset Menit",
                                    fontSize = 12.sp,
                                    fontFamily = CinzelFont,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        if (prayerOffset < 15) {
                                            prayerOffset += 1
                                            prefs.edit().putInt(PrefsKeys.PRAYER_TIME_OFFSET, prayerOffset).apply()
                                            triggerPrayerReload()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DividerLine,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("+1m", fontWeight = FontWeight.Black, fontSize = 11.sp, fontFamily = NunitoFont)
                                }
                            }
                        }
                    }
                }
            }

            // 7. FILE MANAGEMENT (CUSTOM LOADERS)
            item {
                val currentStatusText = when (previewState) {
                    is PreviewState.Loading -> "Memuat..."
                    is PreviewState.Playing -> "Sedang Diputar"
                    is PreviewState.Ready -> "Siap"
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionHeader(title = "MANAJEMEN BERKAS ADZAN", icon = Icons.Outlined.QueueMusic)
                    Spacer(Modifier.height(8.dp))

                    if (copyProgressState is CopyProgress.Copying) {
                        val progressVal = (copyProgressState as CopyProgress.Copying).progress
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldGlow.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { progressVal },
                                    modifier = Modifier.size(24.dp),
                                    color = GoldPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Menyiapkan berkas suara adzan bawaan offline... (${(progressVal * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontFamily = NunitoFont,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1: Adzan Umum
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Adzan Umum",
                                        fontSize = 11.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (activePreview == "umum") {
                                            "[$currentStatusText] " + (customAdzanName ?: "Nada Bawaan Aplikasi (Makkah)")
                                        } else {
                                            customAdzanName ?: "Nada Bawaan Aplikasi (Makkah)"
                                        },
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = if (activePreview == "umum") GoldPrimary else (if (customAdzanName != null) GoldPrimary else TextSecondary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (activePreview == "umum") stopPreview() else playPreview("adzan.mp3", "umum")
                                        },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(DividerLine, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (activePreview == "umum") Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Mainkan",
                                            tint = if (activePreview == "umum") ErrorRed else TextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { launcherUmum.launch("audio/*") },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(GoldGlow, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = "Unggah",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    if (customAdzanName != null) {
                                        IconButton(
                                            onClick = { deleteCustomAudio(isFajr = false) },
                                            modifier = Modifier
                                                .size(30.dp)
                                                .background(ResetRed.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = ErrorRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                            // Row 2: Adzan Subuh
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Adzan Subuh",
                                        fontSize = 11.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (activePreview == "fajr") {
                                            "[$currentStatusText] " + (customAdzanFajrName ?: "Nada Bawaan Aplikasi (Madinah)")
                                        } else {
                                            customAdzanFajrName ?: "Nada Bawaan Aplikasi (Madinah)"
                                        },
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = if (activePreview == "fajr") GoldPrimary else (if (customAdzanFajrName != null) GoldPrimary else TextSecondary),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (activePreview == "fajr") stopPreview() else playPreview("adzan_fajr.mp3", "fajr")
                                        },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(DividerLine, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (activePreview == "fajr") Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Mainkan",
                                            tint = if (activePreview == "fajr") ErrorRed else TextPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { launcherFajr.launch("audio/*") },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(GoldGlow, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = "Unggah",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    if (customAdzanFajrName != null) {
                                        IconButton(
                                            onClick = { deleteCustomAudio(isFajr = true) },
                                            modifier = Modifier
                                                .size(30.dp)
                                                .background(ResetRed.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = ErrorRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DIAGNOSTIK & KEAMANAN DATA
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSectionHeader(title = "DIAGNOSTIK & KEAMANAN DATA", icon = Icons.Outlined.Security)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. Status Proteksi Runtime (Root, Emulator, Integrity)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(GoldGlow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Integritas & Lingkungan",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                    Text(
                                        text = if (AppSecurityManager.isDeviceRooted) {
                                            "Akses Root Aktif (Gentle Warning)"
                                        } else if (!AppSecurityManager.isAppIntegrityValid) {
                                            "APK Dimodifikasi / Tampered!"
                                        } else {
                                            "Aman dari Modifikasi & Debugging"
                                        },
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                                SecurityStatusBadge(level = AppSecurityManager.securityLevel)
                            }

                            HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                            // 2. Enkripsi KeyStore
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isEncryptionActive) TealDim.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isEncryptionActive) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (isEncryptionActive) TealAccent else WarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isEncryptionActive) "Enkripsi KeyStore Aktif" else "Enkripsi Tidak Tersedia",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEncryptionActive) TealAccent else WarningAmber
                                    )
                                    Text(
                                        text = if (isEncryptionActive) {
                                            "Data sensitif dilindungi hardware AES-256 GCM."
                                        } else {
                                            "Mode Fallback Aktif. Koordinat diamankan di memori."
                                        },
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                            }

                            HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        diagnosticsResult = KeyStoreDiagnostics.performDiagnostics()
                                        showDiagnosticsDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CardElevated),
                                    border = BorderStroke(1.dp, DividerLine),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                                        Text("Tes Diagnosa", fontSize = 10.sp, color = TextPrimary, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { showResetConfirmation = true },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CardElevated),
                                    border = BorderStroke(1.dp, DividerLine),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(12.dp))
                                        Text("Reset & Pulihkan", fontSize = 10.sp, color = TextPrimary, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ABOUT FOOTER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Waktu Sholat & Qiblah 2026",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoldDim
                    )
                    Text(
                        text = "Mari Tegakkan Sholat Tepat Waktu.",
                        fontFamily = NunitoFont,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "© IdeaHouse Tech • ferry_pey",
                        fontFamily = NunitoFont,
                        fontSize = 8.sp,
                        color = TextMuted
                    )
                }
            }
        }

        if (showDiagnosticsDialog && diagnosticsResult != null) {
            AlertDialog(
                onDismissRequest = { showDiagnosticsDialog = false },
                icon = {
                    Icon(
                        imageVector = if (diagnosticsResult!!.isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (diagnosticsResult!!.isHealthy) TealAccent else WarningAmber,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Hasil Diagnosa KeyStore",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Status: ${if (diagnosticsResult!!.isHealthy) "SANGAT SEHAT (SEMENTARA/HARDWARE)" else "TERGANGGU (FALLBACK)"}",
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = if (diagnosticsResult!!.isHealthy) TealAccent else WarningAmber,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Hardware Backed: ${if (diagnosticsResult!!.isHardwareBacked) "Ya 🛡️" else "Tidak ⚠️"}",
                            fontFamily = NunitoFont,
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = diagnosticsResult!!.errorMessage,
                            fontFamily = NunitoFont,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        HorizontalDivider(color = DividerLine, thickness = 0.5.dp)
                        Text(
                            text = "Saran / Solusi:",
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldDim
                        )
                        Text(
                            text = diagnosticsResult!!.suggestion,
                            fontFamily = NunitoFont,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDiagnosticsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Tutup", color = DeepNight)
                    }
                },
                containerColor = CardElevated,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showResetConfirmation) {
            AlertDialog(
                onDismissRequest = { showResetConfirmation = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Konfirmasi Reset Penyimpanan",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin mereset KeyStore dan inisiasi ulang preferensi aman?\n\nTindakan ini akan memulihkan data non-sensitif (seperti tema dan onboarding), namun koordinat GPS manual Anda perlu dimasukkan ulang.",
                        fontFamily = NunitoFont,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirmation = false
                            PrefsMigration.handleCorruptedKeystore(context)
                            isEncryptionActive = SecurePrefs.isEncryptionActive(context)
                            Toast.makeText(context, "Sistem penyimpanan telah di-reset & di-inisiasi ulang.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Ya, Reset", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmation = false }) {
                        Text("Batal", color = TextSecondary)
                    }
                },
                containerColor = CardElevated,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontFamily = CinzelFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = GoldPrimary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(color = DividerLine, modifier = Modifier.weight(1f))
    }
}
