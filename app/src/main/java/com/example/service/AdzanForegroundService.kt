package id.ideahousetech.prayertime_qibla.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import id.ideahousetech.prayertime_qibla.MainActivity
import id.ideahousetech.prayertime_qibla.R
import id.ideahousetech.prayertime_qibla.utils.IntentSecurityUtils

/**
 * Foreground Service untuk memastikan alarm adzan tidak di-kill
 * oleh sistem Android, terutama di MIUI (Xiaomi), ColorOS (OPPO/Realme),
 * dan One UI (Samsung) yang agresif membunuh background process.
 * Sudah di-hardening penuh dari celah keamanan.
 */
class AdzanForegroundService : Service() {

    companion object {
        private const val TAG = "AdzanForegroundService"
        const val CHANNEL_ID_FG = "adzan_foreground_channel"
        const val NOTIFICATION_ID_FG = 1002
        const val ACTION_START = "id.ideahousetech.prayertime_qibla.START_FOREGROUND"
        const val ACTION_STOP  = "id.ideahousetech.prayertime_qibla.STOP_FOREGROUND"
        
        private const val RATE_LIMIT_WINDOW_MS = 2000L // Batasi request maksimal 1x per 2 detik
        @Volatile
        private var lastActionTime = 0L
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Tangani null intent (ketika OS me-restart service karena START_STICKY)
        if (intent == null) {
            Log.d(TAG, "Service dijalankan ulang otomatis oleh Android OS.")
            startForegroundWithNotification()
            return START_STICKY
        }

        // 2. Verifikasi Caller & Replay Protection (Integritas Pengirim)
        if (!IntentSecurityUtils.isIntentFromTrustedSource(this, intent)) {
            Log.e(TAG, "ALERT KEAMANAN: Upaya memanggil service dari caller yang tidak dikenal diblokir!")
            return START_NOT_STICKY
        }

        // 3. Whitelisting Action (Hanya izinkan aksi terdaftar)
        val action = intent.action ?: ACTION_START
        if (action != ACTION_START && action != ACTION_STOP) {
            Log.e(TAG, "ALERT KEAMANAN: Aksi tidak terdaftar ditolak: $action")
            return START_NOT_STICKY
        }

        // 4. Rate Limiting (Cegah DOS / Flooding CPU drain)
        val now = System.currentTimeMillis()
        if (now - lastActionTime < RATE_LIMIT_WINDOW_MS) {
            Log.w(TAG, "Rate limit terlampaui untuk aksi: $action. Diabaikan.")
            return START_STICKY
        }
        lastActionTime = now

        Log.d(TAG, "Aksi terverifikasi aman dan dieksekusi: $action")
        when (action) {
            ACTION_START -> startForegroundWithNotification()
            ACTION_STOP  -> stopSelf()
        }
        return START_STICKY   // Restart otomatis jika di-kill
    }

    private fun startForegroundWithNotification() {
        createForegroundChannel()
        
        // Gunakan secure PendingIntent untuk notifikasi foreground service
        val mainIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = IntentSecurityUtils.createSecurePendingIntent(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            IntentSecurityUtils.PendingIntentType.ACTIVITY
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_FG)
            .setContentTitle("Waktu Sholat & Adzan")
            .setContentText("Layanan aktif mendengarkan jadwal sholat...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID_FG,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID_FG, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memanggil startForeground secara aman: ${e.message}")
        }
    }

    private fun createForegroundChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_FG, "Adzan Aktif",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Menjaga alarm adzan tetap aktif" }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
