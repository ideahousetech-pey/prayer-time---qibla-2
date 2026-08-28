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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.ideahousetech.prayertime_qibla.model.IslamicHoliday
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.FileSecurityUtils
import id.ideahousetech.prayertime_qibla.utils.FileValidationResult
import id.ideahousetech.prayertime_qibla.utils.TrustedAdzanDomains
import id.ideahousetech.prayertime_qibla.utils.SecureDownloadHelper
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun HolidayDialog(
    holiday: IslamicHoliday,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
            tonalElevation = 12.dp,
            border = BorderStroke(2.dp, GoldPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(GoldGlow, CircleShape)
                            .border(1.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🕌",
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Selamat Memperingati Hari Besar",
                        fontSize = 12.sp,
                        color = TealAccent,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = holiday.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 1. Amalan Utama
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, DividerLine, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "✨ AMALAN UTAMA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = holiday.description,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // 2. Sejarah Peristiwa
                    if (holiday.history.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .border(1.dp, DividerLine, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📖 SEJARAH PERISTIWA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealAccent,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holiday.history,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // 3. Keutamaan & Dalil
                    if (holiday.quranHadith.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = GoldGlow.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "📜 KEUTAMAAN & DALIL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holiday.quranHadith,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DeepNight
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Saya Mengerti, Alhamdulillah",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

data class OnlineAdzan(val displayName: String, val url: String, val desc: String)

@Composable
fun SettingsDialog(
    locationViewModel: LocationViewModel,
    onDismiss: () -> Unit,
    onReminderToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs.get(context) }
    val scope = rememberCoroutineScope()

    val onlineAdzans = remember {
        listOf(
            OnlineAdzan("Adzan Makkah", "https://www.islamcan.com/audio/adhans/adhan1.mp3", "Adzan syahdu nan agung dari Masjidil Haram, Makkah."),
            OnlineAdzan("Adzan Madinah", "https://www.islamcan.com/audio/adhans/adhan10.mp3", "Adzan merdu menenangkan dari Masjid Nabawi, Madinah."),
            OnlineAdzan("Adzan Mesir", "https://www.islamcan.com/audio/adhans/adhan13.mp3", "Adzan bernada indah khas gaya legendaris Mesir."),
            OnlineAdzan("Adzan Al-Aqsa", "https://www.islamcan.com/audio/adhans/adhan14.mp3", "Adzan khidmat menyentuh kalbu dari Masjidil Aqsa."),
            OnlineAdzan("Adzan Makkah Subuh", "https://www.islamcan.com/audio/adhans/adhan2.mp3", "Adzan khusus Subuh (dengan tambahan Ash-Shalaatu Khairum Minan-Naum).")
        )
    }

    var isAlarmEnabled by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true)) }
    var isDailyReminderEnabled by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, true)) }
    var prayerOffset by remember { mutableStateOf(prefs.getInt(PrefsKeys.PRAYER_TIME_OFFSET, 0)) }

    var customAdzanName by remember { mutableStateOf(prefs.getString(PrefsKeys.CUSTOM_ADZAN_NAME, null)) }
    var customAdzanFajrName by remember { mutableStateOf(prefs.getString(PrefsKeys.CUSTOM_ADZAN_FAJR_NAME, null)) }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadingName by remember { mutableStateOf("") }

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
                if (!TrustedAdzanDomains.isUrlTrusted(attemptUrl)) {
                    continue
                }
                
                val result = SecureDownloadHelper.downloadAdzan(context, attemptUrl, targetFile) { progress ->
                    if (progress >= 0) {
                        downloadProgress = progress
                    } else {
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
            
            if (success) {
                withContext(Dispatchers.Main) {
                    val savedName = "${adzan.displayName} (Internet)"
                    if (isSubuh) {
                        prefs.edit().putString(PrefsKeys.CUSTOM_ADZAN_FAJR_NAME, savedName).apply()
                        customAdzanFajrName = savedName
                    } else {
                        prefs.edit().putString(PrefsKeys.CUSTOM_ADZAN_NAME, savedName).apply()
                        customAdzanName = savedName
                    }
                    isDownloading = false
                    Toast.makeText(context, "Selesai mengunduh & menerapkan: $savedName", Toast.LENGTH_LONG).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    isDownloading = false
                    Toast.makeText(context, "Gagal mengunduh adzan: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var player: MediaPlayer? by remember { mutableStateOf(null) }
    var activePreview by remember { mutableStateOf<String?>(null) } // "umum" or "fajr" or null

    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }

    fun playPreview(fileName: String, type: String) {
        try {
            player?.stop()
            player?.release()
            player = null

            val file = File(context.filesDir, fileName)
            var sourceSet = false
            val tempPlayer = MediaPlayer()
            
            // 1. Coba memutar file fisik local di filesDir terlebih dahulu (termasuk kustom atau salinan asset)
            if (file.exists() && file.length() > 50000) {
                try {
                    tempPlayer.setDataSource(file.absolutePath)
                    sourceSet = true
                } catch (e: Exception) {
                    // Lanjut ke tingkat berikutnya
                }
            }
            
            // 2. Jika file fisik belum siap/tidak ada, muat langsung dari assets
            if (!sourceSet) {
                try {
                    context.assets.openFd(fileName).use { afd ->
                        tempPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        sourceSet = true
                    }
                } catch (e: Exception) {
                    // Lanjut ke tingkat berikutnya
                }
            }
            
            // 3. Fallback ke ringtone default Alarm perangkat jika seluruh metode di atas gagal
            if (!sourceSet) {
                try {
                    val defaultUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    tempPlayer.setDataSource(context, defaultUri)
                    sourceSet = true
                } catch (e: Exception) {
                    // Gagal total
                }
            }
            
            if (sourceSet) {
                tempPlayer.prepare()
                tempPlayer.start()
                tempPlayer.setOnCompletionListener {
                    activePreview = null
                }
                player = tempPlayer
                activePreview = type
            } else {
                Toast.makeText(context, "Gagal memutar suara adzan.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memutar suara: ${e.message}", Toast.LENGTH_SHORT).show()
            activePreview = null
        }
    }

    fun stopPreview() {
        player?.stop()
        player?.release()
        player = null
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(2.dp, GoldPrimary, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Pengaturan Aplikasi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section Tema: Mode Tampilan
                var appThemeMode by remember { mutableStateOf(prefs.getString(PrefsKeys.APP_THEME_MODE, "dark") ?: "dark") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Tema & Mode Tampilan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pilih tema gelap, terang, atau ikuti pengaturan sistem Anda",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    
                    // Segmented Button Custom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSurface, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val modes = listOf(
                            "dark" to "Gelap 🌙",
                            "light" to "Terang ☀️",
                            "system" to "Sistem 🔄"
                        )
                        modes.forEach { (modeKey, modeName) ->
                            val isSelected = appThemeMode == modeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(
                                        color = if (isSelected) GoldPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        prefs.edit().putString(PrefsKeys.APP_THEME_MODE, modeKey).apply()
                                        id.ideahousetech.prayertime_qibla.ui.theme.AppThemeState.currentThemeMode.value = modeKey
                                        appThemeMode = modeKey
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) DeepNight else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Aktivasi Alarm Adzan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Suara Alarm Adzan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Putar adzan saat waktu sholat tiba",
                            fontSize = 11.sp,
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
                            checkedThumbColor = DeepNight,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DividerLine
                        )
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Banner peringatan exact alarm
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    // Buka Settings untuk grant exact alarm permission
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = WarningAmber.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Izin Alarm Tepat Waktu Belum Diberikan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber
                                    )
                                    Text(
                                        "Ketuk di sini → Aktifkan izin → Alarm adzan bisa telat tanpa ini",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 2: Aktivasi Kutipan Harian
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kutipan Amalan Harian",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tampilkan hikmah sholat di layar utama",
                            fontSize = 11.sp,
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
                            checkedThumbColor = DeepNight,
                            checkedTrackColor = GoldPrimary,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DividerLine
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Koreksi Waktu Sholat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Koreksi Waktu Sholat (Menit)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sesuaikan jadwal sholat agar cocok dengan masjid setempat",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DividerLine,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("-1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }

                        Text(
                            text = if (prayerOffset == 0) "Sesuai Standar" else if (prayerOffset > 0) "+$prayerOffset Menit (Maju)" else "$prayerOffset Menit (Mundur)",
                            fontSize = 14.sp,
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
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DividerLine,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+1", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 4: Lokasi Manual vs GPS Otomatis (ditambah tombol Refresh GPS)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    val isManualLoc by locationViewModel.isManualLocation.collectAsState()
                    val currentAddress by locationViewModel.locationName.collectAsState()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lokasi & Koordinat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isManualLoc) "Kota Manual: $currentAddress" else "GPS Otomatis Aktif",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!isManualLoc) {
                                Button(
                                    onClick = {
                                        locationViewModel.refreshLocation()
                                        Toast.makeText(context, "Sinkronisasi koordinat GPS...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealAccent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Refresh GPS",
                                        tint = DeepNight,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sinkron",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNight
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isManualLoc) {
                                        locationViewModel.setAutoLocation()
                                        Toast.makeText(context, "Harap sinkron GPS otomatis", Toast.LENGTH_SHORT).show()
                                    } else {
                                        locationViewModel.setManualLocation("Jakarta", -6.2088, 106.8456)
                                        Toast.makeText(context, "Beralih ke Kota Manual Jakarta", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isManualLoc) ResetRed else DarkTeal
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (isManualLoc) "Gunakan GPS" else "Set Manual",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
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
                            placeholder = { Text("Cari kota Indonesia...", color = TextSecondary, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = DividerLine
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .background(DeepNight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 130.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredCities) { city ->
                                    val coords = indonesianCities[city]!!
                                    val isSelected = currentAddress.contains(city, ignoreCase = true)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                locationViewModel.setManualLocation(city, coords.first, coords.second)
                                                Toast.makeText(context, "$city dipilih.", Toast.LENGTH_SHORT).show()
                                            }
                                            .background(
                                                if (isSelected) GoldGlow else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = city,
                                            color = if (isSelected) GoldPrimary else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "${coords.first}, ${coords.second}",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PILIHAN SUARA ADZAN
                Text(
                    text = "PILIHAN SUARA ADZAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                // Row 1: Adzan Umum
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .border(1.dp, DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adzan Umum (Biasa)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = customAdzanName ?: "Nada Bawaan Aplikasi",
                            fontSize = 11.sp,
                            color = if (customAdzanName != null) GoldPrimary else TextSecondary,
                            maxLines = 1
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                if (activePreview == "umum") {
                                    stopPreview()
                                } else {
                                    playPreview("adzan.mp3", "umum")
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(DividerLine, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (activePreview == "umum") Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Mainkan",
                                tint = if (activePreview == "umum") ErrorRed else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { launcherUmum.launch("audio/*") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldGlow, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Unggah",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (customAdzanName != null) {
                            IconButton(
                                onClick = { deleteCustomAudio(isFajr = false) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ResetRed.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: Adzan Subuh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(DividerLine, RoundedCornerShape(12.dp))
                        .border(1.dp, DividerLine, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adzan Khusus Subuh",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = customAdzanFajrName ?: "Nada Bawaan Aplikasi",
                            fontSize = 11.sp,
                            color = if (customAdzanFajrName != null) GoldPrimary else TextSecondary,
                            maxLines = 1
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                if (activePreview == "fajr") {
                                    stopPreview()
                                } else {
                                    playPreview("adzan_fajr.mp3", "fajr")
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(DividerLine, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (activePreview == "fajr") Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Mainkan",
                                tint = if (activePreview == "fajr") ErrorRed else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { launcherFajr.launch("audio/*") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(GoldGlow, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Unggah",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (customAdzanFajrName != null) {
                            IconButton(
                                onClick = { deleteCustomAudio(isFajr = true) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(ResetRed.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // INFO APLIKASI
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DividerLine, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardElevated.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "INFO APLIKASI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Waktu Sholat & Kiblat v1.0.2\nMari Tegakkan Sholat Tepat Waktu.\n© ferry_pey",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DeepNight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Simpan & Kembali",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
