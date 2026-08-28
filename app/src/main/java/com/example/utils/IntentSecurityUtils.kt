package id.ideahousetech.prayertime_qibla.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.util.UUID

/**
 * Utilitas keamanan siber untuk melindungi Intent dan PendingIntent
 * dari manipulasi, replay attacks, dan spoofing oleh aplikasi luar.
 */
object IntentSecurityUtils {

    private const val TAG = "IntentSecurityUtils"
    private val sessionSecret = UUID.randomUUID().toString()

    enum class PendingIntentType {
        ACTIVITY, BROADCAST, SERVICE, FOREGROUND_SERVICE
    }

    /**
     * Menandatangani Intent dengan signature berbasis HMAC-SHA256 tiruan
     * menggunakan rahasia session dinamis dan stempel waktu (timestamp).
     */
    fun signIntent(intent: Intent): Intent {
        val action = intent.action ?: ""
        val timestamp = System.currentTimeMillis()
        intent.putExtra("_security_timestamp", timestamp)
        intent.putExtra("_security_signature", generateSignature(action, timestamp))
        return intent
    }

    /**
     * Menandatangani Intent masa depan (scheduled) dengan target waktu eksekusi presisi.
     */
    fun signScheduledIntent(intent: Intent, scheduledTimeMs: Long): Intent {
        val action = intent.action ?: ""
        intent.putExtra("_security_scheduled_time", scheduledTimeMs)
        intent.putExtra("_security_signature", generateSignature(action, scheduledTimeMs))
        return intent
    }

    /**
     * Memeriksa apakah Intent berasal dari sumber tepercaya (aplikasi sendiri atau system).
     */
    fun isIntentFromTrustedSource(context: Context, intent: Intent): Boolean {
        val callingUid = android.os.Binder.getCallingUid()
        val myUid = android.os.Process.myUid()

        // 1. Jika caller adalah UID aplikasi kita sendiri atau System Server (1000) atau Root (0), selalu percayai
        if (callingUid == myUid || callingUid == 1000 || callingUid == 0) {
            return true
        }

        // 2. Jika dipanggil dari luar, verifikasi signature kriptografis dan replay protection
        val action = intent.action ?: return false
        val signature = intent.getStringExtra("_security_signature") ?: return false
        val scheduledTime = intent.getLongExtra("_security_scheduled_time", 0L)
        val timestamp = intent.getLongExtra("_security_timestamp", 0L)

        val now = System.currentTimeMillis()

        if (scheduledTime != 0L) {
            // Verifikasi masa berlaku alarm terjadwal (toleransi 5 menit sejak waktu target)
            if (Math.abs(now - scheduledTime) > 300000) {
                Log.e(TAG, "Replay attack/penundaan terdeteksi pada alarm terjadwal. Diff: ${Math.abs(now - scheduledTime)} ms")
                return false
            }
            val expectedSignature = generateSignature(action, scheduledTime)
            if (signature != expectedSignature) {
                Log.e(TAG, "Signature alarm terjadwal tidak cocok!")
                return false
            }
        } else if (timestamp != 0L) {
            // Verifikasi intent instan (toleransi 60 detik)
            if (Math.abs(now - timestamp) > 60000) {
                Log.e(TAG, "Replay attack terdeteksi atau toleransi waktu habis. Diff: ${Math.abs(now - timestamp)} ms")
                return false
            }
            val expectedSignature = generateSignature(action, timestamp)
            if (signature != expectedSignature) {
                Log.e(TAG, "Signature intent instan tidak cocok!")
                return false
            }
        } else {
            // Tidak ada token keamanan sama sekali
            Log.e(TAG, "Percobaan exploit: Intent tidak memiliki token tanda tangan keamanan.")
            return false
        }

        return true
    }

    /**
     * Menghapus extras yang tidak aman atau kompleks untuk menghindari exploit deserialisasi.
     * Hanya mengizinkan tipe data primitif dan String.
     */
    fun sanitizeIntentExtras(intent: Intent): Bundle {
        val originalBundle = intent.extras ?: return Bundle()
        val cleanBundle = Bundle()
        for (key in originalBundle.keySet()) {
            when (val value = originalBundle.get(key)) {
                is String -> cleanBundle.putString(key, value)
                is Int -> cleanBundle.putInt(key, value)
                is Long -> cleanBundle.putLong(key, value)
                is Boolean -> cleanBundle.putBoolean(key, value)
                is Float -> cleanBundle.putFloat(key, value)
                is Double -> cleanBundle.putDouble(key, value)
                // Abaikan parcelable/serializable eksternal yang mencurigakan
            }
        }
        return cleanBundle
    }

    /**
     * Membuat PendingIntent aman ber-stempel kriptografis dan bertipe FLAG_IMMUTABLE.
     */
    fun createSecurePendingIntent(
        context: Context,
        requestCode: Int,
        intent: Intent,
        flags: Int,
        type: PendingIntentType
    ): PendingIntent {
        // Buat intent eksplisit secara paksa ke package aplikasi sendiri jika tidak ditentukan
        if (intent.component == null && intent.`package` == null) {
            intent.setPackage(context.packageName)
        }

        // Tandatangani intent dengan token keamanan jika belum ditandatangani secara terjadwal
        if (!intent.hasExtra("_security_signature")) {
            signIntent(intent)
        }

        // Pastikan FLAG_IMMUTABLE terpasang di Android M+ demi kepatuhan keamanan
        val secureFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (flags and PendingIntent.FLAG_MUTABLE.inv()) or PendingIntent.FLAG_IMMUTABLE
        } else {
            flags
        }

        return when (type) {
            PendingIntentType.ACTIVITY -> {
                PendingIntent.getActivity(context, requestCode, intent, secureFlags)
            }
            PendingIntentType.BROADCAST -> {
                PendingIntent.getBroadcast(context, requestCode, intent, secureFlags)
            }
            PendingIntentType.SERVICE -> {
                PendingIntent.getService(context, requestCode, intent, secureFlags)
            }
            PendingIntentType.FOREGROUND_SERVICE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(context, requestCode, intent, secureFlags)
                } else {
                    PendingIntent.getService(context, requestCode, intent, secureFlags)
                }
            }
        }
    }

    /**
     * Overload createSecurePendingIntent dengan deteksi otomatis bertipe standard.
     */
    fun createSecurePendingIntent(
        context: Context,
        requestCode: Int,
        intent: Intent,
        flags: Int
    ): PendingIntent {
        val componentName = intent.component?.className ?: ""
        val type = when {
            componentName.contains("Service") -> PendingIntentType.SERVICE
            componentName.contains("Receiver") || componentName.contains("Widget") -> PendingIntentType.BROADCAST
            else -> PendingIntentType.ACTIVITY
        }
        return createSecurePendingIntent(context, requestCode, intent, flags, type)
    }

    private fun generateSignature(action: String, timestamp: Long): String {
        val data = "$action:$timestamp:$sessionSecret"
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }
}
