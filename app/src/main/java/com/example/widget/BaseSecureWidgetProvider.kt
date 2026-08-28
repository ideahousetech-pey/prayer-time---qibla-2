package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import id.ideahousetech.prayertime_qibla.utils.IntentSecurityUtils

/**
 * Base class aman untuk AppWidgetProvider.
 * Melindungi widget dari serangan replay, flooding, spoofing, dan eksploitasi data eksternal.
 */
abstract class BaseSecureWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "BaseSecureWidget"
        private const val MIN_UPDATE_INTERVAL_MS = 2000L // Rate limit 2 detik untuk perlindungan baterai
        
        @Volatile
        private var lastUpdateTime = 0L
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive dipanggil pada ${this::class.java.simpleName} dengan action: ${intent.action}")

        // 1. Verifikasi integritas pengirim intent (Caller Verification)
        if (!IntentSecurityUtils.isIntentFromTrustedSource(context, intent)) {
            Log.e(TAG, "PERINGATAN KEAMANAN: Intent dari sumber tidak dikenal ditolak. Action: ${intent.action}")
            return
        }

        // 2. Pembatasan Frekuensi (Rate Limiting) untuk pencegahan serangan drain baterai
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime < MIN_UPDATE_INTERVAL_MS) {
            Log.w(TAG, "Widget update terlalu cepat (Rate limit hit). Action diabaikan: ${intent.action}")
            return
        }
        lastUpdateTime = now

        // 3. Sanitasi data masukan (Validate & Sanitize Intent Extras)
        val cleanBundle = IntentSecurityUtils.sanitizeIntentExtras(intent)
        val sanitizedIntent = Intent(intent).apply {
            replaceExtras(cleanBundle)
        }

        // 4. Lanjutkan ke pemrosesan standard jika lolos seluruh pengecekan
        super.onReceive(context, sanitizedIntent)
    }
}
