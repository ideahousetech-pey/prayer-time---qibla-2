package id.ideahousetech.prayertime_qibla.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.pendingIntentFlags
import id.ideahousetech.prayertime_qibla.utils.IntentSecurityUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val stopHandler = Handler(Looper.getMainLooper())
    private val isAudioPlaying = java.util.concurrent.atomic.AtomicBoolean(false)

    companion object {
        const val CHANNEL_ID       = "islamic_prayer_alarms"
        const val CHANNEL_NAME     = "Jadwal Waktu Sholat & Adzan"
        const val ACTION_PLAY_ADZAN  = "id.ideahousetech.prayertime_qibla.ACTION_PLAY_ADZAN"
        const val ACTION_STOP_ADZAN  = "id.ideahousetech.prayertime_qibla.ACTION_STOP_ADZAN"
        const val ACTION_PRE_REMINDER = "id.ideahousetech.prayertime_qibla.ACTION_PRE_REMINDER"
        const val EXTRA_PRAYER_NAME  = "prayer_name"
        const val EXTRA_IS_FAJR      = "is_fajr"

        // Batas maksimal durasi adzan yang diputar (5 menit)
        private const val MAX_ADZAN_DURATION_MS = 5 * 60 * 1000L

        @Volatile private var instance: NotificationService? = null

        fun getInstance(context: Context): NotificationService {
            return instance ?: synchronized(this) {
                instance ?: NotificationService(context.applicationContext)
                    .also { instance = it }
            }
        }
    }

    init {
        createNotificationChannel()
        triggerCopyAssetAudioFiles()
    }

    // ── Audio Focus ──────────────────────────────────────────────────────────

    /**
     * Meminta audio focus sebelum memutar adzan.
     * Ini menghentikan atau me-mute musik/media lain yang sedang berjalan.
     */
    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    // Hentikan adzan jika focus diambil paksa oleh app lain
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        stopAdzanAudio()
                    }
                }
                .build()
            audioFocusRequest = focusRequest
            audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /** Melepaskan audio focus setelah adzan selesai */
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        audioFocusRequest = null
    }

    // ── WakeLock ─────────────────────────────────────────────────────────────

    /**
     * Acquire WakeLock agar CPU tidak tidur saat audio diputar.
     * Otomatis release setelah MAX_ADZAN_DURATION_MS + 10 detik buffer.
     */
    private fun acquireWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PrayerTimes:AdzanWakeLock"
            ).apply {
                acquire(MAX_ADZAN_DURATION_MS + 10_000L)
            }
            Log.d("NotificationService", "WakeLock acquired")
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal acquire WakeLock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = null
            Log.d("NotificationService", "WakeLock released")
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal release WakeLock: ${e.message}")
        }
    }

    // ── MediaPlayer ──────────────────────────────────────────────────────────

    private fun releasePlayer() {
        if (!isAudioPlaying.compareAndSet(true, false)) {
            // Sudah dirilis atau tidak sedang memutar adzan
            return
        }

        stopHandler.removeCallbacksAndMessages(null)
        
        // 1. Set mediaPlayer ke null terlebih dahulu untuk menghindari race condition
        val playerToRelease = mediaPlayer
        mediaPlayer = null
        
        try {
            playerToRelease?.apply {
                try {
                    if (isPlaying) {
                        stop()
                    }
                } catch (_: Exception) {}
                reset()
                release()
            }
        } finally {
            // 3. Pastikan WakeLock dan AudioFocus SELALU dirilis di dalam finally block
            releaseAudioFocus()
            releaseWakeLock()
        }
    }

    fun getAudioFileNameForPrayer(prayerName: String): String {
        val prefs = SecurePrefs.get(context)
        val key = when (prayerName.lowercase()) {
            "subuh" -> "adzan_subuh_sound"
            "dzuhur", "dhuhur" -> "adzan_dhuhr_sound"
            "ashar" -> "adzan_asr_sound"
            "maghrib" -> "adzan_maghrib_sound"
            "isya", "isha" -> "adzan_isha_sound"
            else -> "adzan_dhuhr_sound"
        }
        val choice = prefs.getString(key, "makkah") ?: "makkah"
        return if (choice == "madinah") "adzan_fajr.mp3" else "adzan.mp3"
    }

    /**
     * Memutar audio adzan dengan sistem berlapis:
     * 1. File MP3 valid di filesDir (tersalin dari assets atau custom)
     * 2. Langsung dari assets (jika ada file valid)
     * 3. Ringtone alarm sistem (fallback terakhir)
     *
     * Menggunakan prepareAsync() agar tidak memblok main thread.
     */
    fun playAdzanAudio(isFajr: Boolean, prayerName: String? = null) {
        try {
            releasePlayer()

            val prefs = SecurePrefs.get(context)
            if (!prefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true)) {
                Log.d("NotificationService", "Alarm adzan dinonaktifkan")
                return
            }

            // Minta audio focus dulu — jika ditolak, adzan tetap diputar (alarm harus berbunyi)
            val focusGranted = requestAudioFocus()
            Log.d("NotificationService", "Audio focus: ${if (focusGranted) "granted" else "denied, lanjut tetap putar"}")

            // Acquire WakeLock agar CPU tidak tidur
            acquireWakeLock()

            val audioFileName = if (prayerName != null) {
                getAudioFileNameForPrayer(prayerName)
            } else {
                if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
            }

            val player = MediaPlayer()
            
            // Set flag bermain menjadi true dan assign ke global instance
            isAudioPlaying.set(true)
            mediaPlayer = player

            // Atur volume dari preferensi (default 80%)
            val volumeInt = prefs.getInt("adzan_volume", 80)
            val volumeFloat = volumeInt / 100f
            player.setVolume(volumeFloat, volumeFloat)

            // Atur stream audio ke STREAM_ALARM untuk bypass silent mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                player.setAudioStreamType(AudioManager.STREAM_ALARM)
            }

            // Tentukan sumber audio
            var sourceSet = false

            // 1. Coba file di filesDir (harus > 100KB agar valid)
            val localFile = File(context.filesDir, audioFileName)
            if (localFile.exists() && localFile.length() > 100_000) {
                try {
                    player.setDataSource(localFile.absolutePath)
                    sourceSet = true
                    Log.d("NotificationService", "Sumber: file lokal ($audioFileName, ${localFile.length()} bytes)")
                } catch (e: Exception) {
                    player.reset()
                    Log.e("NotificationService", "Gagal dari file lokal: ${e.message}")
                }
            }

            // 2. Coba dari assets langsung (harus > 100KB)
            if (!sourceSet) {
                try {
                    val afd = context.assets.openFd(audioFileName)
                    if (afd.length > 100_000) {
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        sourceSet = true
                        Log.d("NotificationService", "Sumber: assets ($audioFileName, ${afd.length} bytes)")
                    }
                    afd.close()
                } catch (e: Exception) {
                    try { player.reset() } catch (_: Exception) {}
                    Log.e("NotificationService", "Gagal dari assets: ${e.message}")
                }
            }

            // 3. Fallback ke ringtone alarm sistem
            if (!sourceSet) {
                try {
                    val alarmUri = android.media.RingtoneManager.getDefaultUri(
                        android.media.RingtoneManager.TYPE_ALARM
                    )
                    player.setDataSource(context, alarmUri)
                    sourceSet = true
                    Log.d("NotificationService", "Sumber: system alarm ringtone (fallback)")
                } catch (e: Exception) {
                    try { player.reset() } catch (_: Exception) {}
                    Log.e("NotificationService", "Gagal dari ringtone sistem: ${e.message}")
                }
            }

            if (!sourceSet) {
                Log.e("NotificationService", "Semua sumber audio gagal, adzan tidak bisa diputar")
                releasePlayer()
                return
            }

            // Gunakan prepareAsync() — tidak memblok main thread
            player.isLooping = false

            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    Log.d("NotificationService", "Adzan mulai diputar")

                    // Auto-stop setelah MAX_ADZAN_DURATION_MS
                    stopHandler.postDelayed({
                        Log.d("NotificationService", "Auto-stop adzan setelah ${MAX_ADZAN_DURATION_MS / 1000}s")
                        stopAdzanAudio()
                    }, MAX_ADZAN_DURATION_MS)
                } catch (e: Exception) {
                    Log.e("NotificationService", "Gagal start player setelah prepare: ${e.message}")
                    releasePlayer()
                }
            }

            player.setOnCompletionListener {
                Log.d("NotificationService", "Adzan selesai diputar")
                releasePlayer()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("NotificationService", "MediaPlayer error: what=$what extra=$extra")
                releasePlayer()
                true
            }

            player.prepareAsync()  // NON-BLOCKING — tidak blok main thread

        } catch (e: Exception) {
            Log.e("NotificationService", "Error playAdzanAudio: ${e.message}")
            releasePlayer()
        }
    }

    fun stopAdzanAudio() {
        try {
            releasePlayer()
            Log.d("NotificationService", "Adzan dihentikan")
        } catch (e: Exception) {
            Log.e("NotificationService", "Error stopAdzanAudio: ${e.message}")
        }
    }

    // ── Notification Channel ─────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm adzan tepat waktu sholat"
                enableLights(true)
                enableVibration(true)
                // Penting: set sound null di channel karena MediaPlayer yang handle audio
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // ── StateFlows untuk observasi dari UI ──────────────────────────────────────
    private val _copyProgress = MutableStateFlow<CopyProgress>(CopyProgress.Idle)
    val copyProgress: StateFlow<CopyProgress> = _copyProgress.asStateFlow()

    private val _previewState = MutableStateFlow<PreviewState>(PreviewState.Ready)
    val previewState: StateFlow<PreviewState> = _previewState.asStateFlow()

    private var previewPlayer: MediaPlayer? = null
    private val previewHandler = Handler(Looper.getMainLooper())
    private val isPreviewPlaying = java.util.concurrent.atomic.AtomicBoolean(false)

    // ── Asset Copy ───────────────────────────────────────────────────────────

    /**
     * Memicu proses salin file audio secara asinkron pada background thread (Dispatchers.IO).
     * Mencegah potensi ANR pada Main thread.
     */
    fun triggerCopyAssetAudioFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            copyAssetAudioFilesIfNeeded()
        }
    }

    /**
     * Menyalin file audio dari assets ke filesDir dengan pelaporan progress real-time.
     * Hanya menyalin jika file assets valid (> 100KB) dan belum identik di lokal.
     */
    private suspend fun copyAssetAudioFilesIfNeeded() {
        val filesToCopy = listOf("adzan.mp3", "adzan_fajr.mp3")
        _copyProgress.value = CopyProgress.Copying(0f)

        try {
            var totalBytesCopied = 0L
            var totalExpectedBytes = 0L

            // 1. Hitung total ukuran perkiraan bytes yang harus disalin
            for (fileName in filesToCopy) {
                try {
                    context.assets.openFd(fileName).use { afd ->
                        totalExpectedBytes += afd.length
                    }
                } catch (e: Exception) {
                    Log.e("NotificationService", "Gagal membaca asset fd: ${e.message}")
                }
            }

            if (totalExpectedBytes == 0L) {
                _copyProgress.value = CopyProgress.Success
                return
            }

            var currentCopied = 0L
            for (fileName in filesToCopy) {
                val destFile = File(context.filesDir, fileName)
                var assetSize = 0L

                try {
                    context.assets.openFd(fileName).use { afd ->
                        assetSize = afd.length
                    }
                } catch (e: Exception) {
                    Log.e("NotificationService", "Gagal membaca fd untuk $fileName: ${e.message}")
                }

                // Cek jika file lokal sudah valid dan ukurannya identik, kita lewati copying namun tetap hitung dalam total progress
                if (destFile.exists() && destFile.length() == assetSize && assetSize > 100_000) {
                    currentCopied += assetSize
                    val progress = (currentCopied.toFloat() / totalExpectedBytes).coerceIn(0f, 1f)
                    _copyProgress.value = CopyProgress.Copying(progress)
                    Log.d("NotificationService", "$fileName sudah terinstal dan identik")
                    continue
                }

                if (assetSize > 100_000) {
                    try {
                        context.assets.open(fileName).use { input ->
                            FileOutputStream(destFile).use { output ->
                                val buffer = ByteArray(16 * 1024) // 16KB buffer
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                    currentCopied += bytesRead
                                    val progress = (currentCopied.toFloat() / totalExpectedBytes).coerceIn(0f, 1f)
                                    _copyProgress.value = CopyProgress.Copying(progress)
                                    yield() // Cooperative cancellation & context yield
                                }
                            }
                        }
                        Log.d("NotificationService", "$fileName berhasil disalin (${destFile.length()} bytes)")
                    } catch (e: Exception) {
                        Log.e("NotificationService", "Gagal menyalin $fileName: ${e.message}")
                        if (destFile.exists()) destFile.delete() // Hapus jika rusak/interupsi
                        throw e
                    }
                } else {
                    Log.w("NotificationService", "$fileName di assets terlalu kecil (${assetSize} bytes)")
                    if (destFile.exists()) destFile.delete()
                }
            }
            _copyProgress.value = CopyProgress.Success
        } catch (e: Exception) {
            _copyProgress.value = CopyProgress.Error(e.message ?: "Gagal menyalin file audio")
        }
    }

    // ── Audio File Validation ────────────────────────────────────────────────

    /**
     * Memvalidasi keabsahan file audio dari assets dan filesDir.
     * Mengecek status ketersediaan, kelayakan ukuran, serta kemampuan decode MediaPlayer.
     */
    fun validateAudioFile(fileName: String): AudioFileStatus {
        // 1. Cek keberadaan dan ukuran di assets
        var assetExists = false
        var assetSize = 0L
        try {
            context.assets.openFd(fileName).use { afd ->
                assetExists = true
                assetSize = afd.length
            }
        } catch (e: Exception) {
            // Assets tidak ditemukan
        }

        if (!assetExists) {
            return AudioFileStatus.Missing
        }

        if (assetSize <= 100_000) {
            return AudioFileStatus.TooSmall
        }

        // 2. Cek keberadaan di filesDir lokal
        val localFile = File(context.filesDir, fileName)
        if (!localFile.exists()) {
            return AudioFileStatus.Missing
        }

        if (localFile.length() < 100_000) {
            return AudioFileStatus.TooSmall
        }

        // 3. Tes kemampuan decode MediaPlayer (Dry Run)
        val player = MediaPlayer()
        return try {
            player.setDataSource(localFile.absolutePath)
            player.setVolume(0f, 0f) // Diam/Silent
            player.prepare()
            AudioFileStatus.Valid
        } catch (e: Exception) {
            Log.e("NotificationService", "Gagal mendekode berkas audio $fileName: ${e.message}")
            AudioFileStatus.Corrupted
        } finally {
            player.reset()
            player.release()
        }
    }

    // ── Preview/Test Adzan ───────────────────────────────────────────────────

    /**
     * Memutar sampel adzan selama durasi tertentu (default 10 detik).
     * Jika saat ini sedang memutar, memanggil fungsi ini akan menghentikannya secara instan.
     */
    fun previewAdzan(isFajr: Boolean, durationSeconds: Int = 10, prayerName: String? = null) {
        if (isPreviewPlaying.get()) {
            stopPreviewAdzan()
            return
        }

        try {
            stopPreviewAdzan() // Pastikan preview sebelumnya mati bersih
            _previewState.value = PreviewState.Loading

            val audioFileName = if (prayerName != null) {
                getAudioFileNameForPrayer(prayerName)
            } else {
                if (isFajr) "adzan_fajr.mp3" else "adzan.mp3"
            }
            val player = MediaPlayer()
            previewPlayer = player
            isPreviewPlaying.set(true)

            // Set volume dari preferensi (default 80%)
            val prefs = SecurePrefs.get(context)
            val volumeInt = prefs.getInt("adzan_volume", 80)
            val volumeFloat = volumeInt / 100f
            player.setVolume(volumeFloat, volumeFloat)

            // Gunakan USAGE_MEDIA untuk pratinjau agar aman sesuai intensitas volume media sistem
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }

            var sourceSet = false
            val localFile = File(context.filesDir, audioFileName)
            if (localFile.exists() && localFile.length() > 100_000) {
                try {
                    player.setDataSource(localFile.absolutePath)
                    sourceSet = true
                    Log.d("NotificationService", "Pratinjau dari lokal: ${localFile.absolutePath}")
                } catch (e: Exception) {
                    player.reset()
                }
            }

            if (!sourceSet) {
                try {
                    val afd = context.assets.openFd(audioFileName)
                    if (afd.length > 100_000) {
                        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        sourceSet = true
                        Log.d("NotificationService", "Pratinjau langsung dari assets: $audioFileName")
                    }
                    afd.close()
                } catch (e: Exception) {
                    player.reset()
                }
            }

            if (!sourceSet) {
                _previewState.value = PreviewState.Ready
                isPreviewPlaying.set(false)
                Log.e("NotificationService", "Gagal memposisikan sumber audio pratinjau")
                return
            }

            player.prepareAsync()
            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    _previewState.value = PreviewState.Playing
                    Log.d("NotificationService", "Pratinjau adzan dimulai")

                    previewHandler.postDelayed({
                        Log.d("NotificationService", "Durasi pratinjau $durationSeconds detik selesai")
                        stopPreviewAdzan()
                    }, durationSeconds * 1000L)
                } catch (e: Exception) {
                    Log.e("NotificationService", "Gagal memulai pemutaran pratinjau: ${e.message}")
                    stopPreviewAdzan()
                }
            }

            player.setOnCompletionListener {
                stopPreviewAdzan()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e("NotificationService", "MediaPlayer Pratinjau Error: $what / $extra")
                stopPreviewAdzan()
                true
            }

        } catch (e: Exception) {
            Log.e("NotificationService", "Kesalahan internal pratinjau: ${e.message}")
            stopPreviewAdzan()
        }
    }

    /** Menghentikan pratinjau adzan secara bersih */
    fun stopPreviewAdzan() {
        if (!isPreviewPlaying.compareAndSet(true, false)) {
            _previewState.value = PreviewState.Ready
            return
        }

        previewHandler.removeCallbacksAndMessages(null)
        val player = previewPlayer
        previewPlayer = null
        try {
            player?.apply {
                if (isPlaying) {
                    stop()
                }
                reset()
                release()
            }
        } catch (_: Exception) {
        } finally {
            _previewState.value = PreviewState.Ready
            Log.d("NotificationService", "Pratinjau adzan dihentikan bersih")
        }
    }

    // ── Alarm Scheduling ─────────────────────────────────────────────────────

    fun scheduleDailyAlarms(times: PrayerTime) {
        cancelAllScheduledAlarms()

        val prayerMap = mapOf(
            "Subuh"   to Pair(times.fajr,    true),
            "Dzuhur"  to Pair(times.dhuhr,   false),
            "Ashar"   to Pair(times.asr,     false),
            "Maghrib" to Pair(times.maghrib, false),
            "Isya"    to Pair(times.isha,    false)
        )

        var alarmIndex = 1
        for ((name, data) in prayerMap) {
            val (timeStr, isFajr) = data
            try {
                val parts  = timeStr.split(":")
                val hour   = parts[0].toInt()
                val minute = parts[1].toInt()

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= System.currentTimeMillis()) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                // Jadwalkan pre-reminder sesuai preferensi (enable_pre_reminder, pre_reminder_minutes)
                val prefs = SecurePrefs.get(context)
                val enablePreReminder = prefs.getBoolean("enable_pre_reminder", false)
                val preReminderMin = prefs.getInt("pre_reminder_minutes", 15)

                if (enablePreReminder) {
                    val preCalendar = (calendar.clone() as Calendar).apply {
                        add(Calendar.MINUTE, -preReminderMin)
                    }
                    if (preCalendar.timeInMillis > System.currentTimeMillis()) {
                        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                            action = ACTION_PRE_REMINDER
                            putExtra(EXTRA_PRAYER_NAME, name)
                        }
                        // Sign the intent with the target execution timestamp
                        IntentSecurityUtils.signScheduledIntent(alarmIntent, preCalendar.timeInMillis)
                        
                        scheduleExactAlarm(
                            requestCode = alarmIndex + 100,
                            triggerAtMs = preCalendar.timeInMillis,
                            intent = alarmIntent
                        )
                        Log.d("NotificationService", "Pre-reminder $name dijadwalkan")
                    }
                }

                // Jadwalkan alarm utama adzan
                val mainAlarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                    action = ACTION_PLAY_ADZAN
                    putExtra(EXTRA_PRAYER_NAME, name)
                    putExtra(EXTRA_IS_FAJR, isFajr)
                }
                // Sign the intent with the target execution timestamp
                IntentSecurityUtils.signScheduledIntent(mainAlarmIntent, calendar.timeInMillis)

                scheduleExactAlarm(
                    requestCode = alarmIndex,
                    triggerAtMs = calendar.timeInMillis,
                    intent = mainAlarmIntent
                )
                Log.d("NotificationService", "Alarm $name dijadwalkan pukul $timeStr")
                alarmIndex++

            } catch (e: Exception) {
                Log.e("NotificationService", "Gagal jadwalkan $name: ${e.message}")
            }
        }
    }

    private fun scheduleExactAlarm(requestCode: Int, triggerAtMs: Long, intent: Intent) {
        val pendingIntent = IntentSecurityUtils.createSecurePendingIntent(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            IntentSecurityUtils.PendingIntentType.BROADCAST
        )

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                    )
                } else {
                    // Exact alarm tidak diizinkan — gunakan inexact sebagai fallback
                    // (bisa telat 5-15 menit, tapi lebih baik daripada tidak sama sekali)
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                    )
                    Log.w("NotificationService",
                        "PERINGATAN: Exact alarm tidak diizinkan. " +
                        "Alarm mungkin tidak tepat waktu. " +
                        "Minta user buka Pengaturan > Aplikasi > Izin Khusus > Alarm & Pengingat")
                }
            }
            else -> alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
            )
        }
    }

    private fun cancelAllScheduledAlarms() {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_NO_CREATE

        for (i in 1..5) {
            listOf(i, i + 100).forEach { code ->
                val action = if (code <= 5) ACTION_PLAY_ADZAN else ACTION_PRE_REMINDER
                val pi = PendingIntent.getBroadcast(
                    context, code,
                    Intent(context, AlarmReceiver::class.java).apply { this.action = action },
                    flags
                )
                pi?.let { alarmManager.cancel(it); it.cancel() }
            }
        }
    }
}

// ── AlarmReceiver ─────────────────────────────────────────────────────────────

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private val lastActionTimes = java.util.concurrent.ConcurrentHashMap<String, Long>()
        private const val RATE_LIMIT_WINDOW_MS = 2000L // 2 detik per action
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive dipanggil dengan action: ${intent.action}")

        // 1. Verifikasi asal pemanggil & Tanda tangan Kriptografis
        if (!IntentSecurityUtils.isIntentFromTrustedSource(context, intent)) {
            Log.e(TAG, "ALERT KEAMANAN: BroadcastReceiver mendeteksi intent palsu/tidak sah!")
            return
        }

        val action = intent.action ?: return

        // 2. Pembatasan Frekuensi (Rate Limiting) per aksi
        val now = System.currentTimeMillis()
        val lastTime = lastActionTimes[action] ?: 0L
        if (now - lastTime < RATE_LIMIT_WINDOW_MS) {
            Log.w(TAG, "Rate limit hit untuk aksi: $action. Diabaikan.")
            return
        }
        lastActionTimes[action] = now

        // 3. Sanitasi Extras (Pencegahan Eksploitasi Deserialisasi)
        val cleanBundle = IntentSecurityUtils.sanitizeIntentExtras(intent)
        val sanitizedIntent = Intent(intent).apply {
            replaceExtras(cleanBundle)
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (sanitizedIntent.action) {
                    NotificationService.ACTION_PLAY_ADZAN -> {
                        val prayerName = sanitizedIntent.getStringExtra(NotificationService.EXTRA_PRAYER_NAME) ?: "Sholat"
                        val isFajr    = sanitizedIntent.getBooleanExtra(NotificationService.EXTRA_IS_FAJR, false)
                        Log.d(TAG, "Alarm terverifikasi sukses: $prayerName (fajr=$isFajr)")

                        val prefs = SecurePrefs.get(context)
                        val adzanVolume = prefs.getInt("adzan_volume", 80)
                        Log.d(TAG, "Alarm volume preference: $adzanVolume%")

                        // Tampilkan notifikasi heads-up secara aman
                        showHeadsUpNotification(context, prayerName)

                        // Putar adzan
                        NotificationService.getInstance(context).playAdzanAudio(isFajr, prayerName)
                    }
                    NotificationService.ACTION_PRE_REMINDER -> {
                        val prayerName = sanitizedIntent.getStringExtra(NotificationService.EXTRA_PRAYER_NAME) ?: "Sholat"
                        showPreReminderNotification(context, prayerName)
                    }
                    NotificationService.ACTION_STOP_ADZAN -> {
                        NotificationService.getInstance(context).stopAdzanAudio()
                        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                            .cancel(1001)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in async onReceive: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showHeadsUpNotification(context: Context, prayerName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val rawLaunchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            ?: Intent()
        val openPi = IntentSecurityUtils.createSecurePendingIntent(
            context,
            0,
            rawLaunchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            IntentSecurityUtils.PendingIntentType.ACTIVITY
        )

        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = NotificationService.ACTION_STOP_ADZAN
        }
        val stopPi = IntentSecurityUtils.createSecurePendingIntent(
            context,
            99,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            IntentSecurityUtils.PendingIntentType.BROADCAST
        )

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🕌 Waktu Sholat $prayerName")
            .setContentText("Mari tegakkan sholat tepat waktu.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openPi)
            .addAction(android.R.drawable.ic_media_pause, "Hentikan Adzan", stopPi)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(openPi, true)  // Tampil di lockscreen
            .build()

        nm.notify(1001, notification)
    }

    private fun showPreReminderNotification(context: Context, prayerName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openPi = PendingIntent.getActivity(
            context, 120,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val prefs = SecurePrefs.get(context)
        val preReminderMin = prefs.getInt("pre_reminder_minutes", 15)

        val notification = NotificationCompat.Builder(context, NotificationService.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $prayerName dalam $preReminderMin menit")
            .setContentText("Segera persiapkan diri untuk sholat $prayerName.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openPi)
            .setAutoCancel(true)
            .build()

        nm.notify(1002, notification)
    }
}

sealed class AdzanSource {
    object Default : AdzanSource()       // dari assets
    data class Custom(val uri: Uri) : AdzanSource() // file user
}

sealed class AudioFileStatus {
    object Valid : AudioFileStatus()
    object Missing : AudioFileStatus()
    object Corrupted : AudioFileStatus()
    object TooSmall : AudioFileStatus()
}

sealed class CopyProgress {
    object Idle : CopyProgress()
    data class Copying(val progress: Float) : CopyProgress()
    object Success : CopyProgress()
    data class Error(val message: String) : CopyProgress()
}

sealed class PreviewState {
    object Ready : PreviewState()
    object Loading : PreviewState()
    object Playing : PreviewState()
}

