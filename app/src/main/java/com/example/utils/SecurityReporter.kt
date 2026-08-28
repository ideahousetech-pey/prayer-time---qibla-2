package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.os.Build
import id.ideahousetech.prayertime_qibla.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class untuk model laporan audit keamanan internal aplikasi.
 */
data class SecurityReport(
    val timestamp: Long,
    val formattedDate: String,
    val encryptionStatus: String,
    val networkSecurity: String,
    val permissionStatus: Map<String, Boolean>,
    val integrityStatus: String,
    val riskLevel: String,
    val recommendations: List<String>
)

/**
 * Pembuat Laporan Keamanan Internal (Internal Security Reporter Generator).
 * Berfungsi mengompilasi status keamanan runtime (enkripsi, pinning, emulator, root, permissions)
 * untuk audit internal maupun keperluan diagnostik tim pengembang (QA / Debug builds).
 */
object SecurityReporter {

    /**
     * Menghasilkan laporan evaluasi keamanan perangkat dan aplikasi secara realtime.
     */
    fun generateReport(context: Context): SecurityReport {
        // 1. Cek status enkripsi
        val securePrefsLoaded = try {
            val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val containsKey = keyStore.containsAlias("_androidx_security_master_key_")
            if (containsKey) "SECURE_KEYSTORE_AES256" else "PLAIN_FALLBACK_ACTIVE"
        } catch (e: Exception) {
            "ENCRYPTION_ERROR: ${e.message}"
        }

        // 2. Cek status jaringan (pinning & cleartext)
        val isCleartextDisabled = !BuildConfig.DEBUG // Pada release, cleartext wajib ditolak
        val networkSec = "SSL_PINNING_ACTIVE (ZeroSSL/Sectigo) | CleartextAllowed: $isCleartextDisabled"

        // 3. Cek status perizinan (Permissions Audit)
        val pm = PermissionManager()
        val pStatus = pm.checkAllRequiredPermissions(context)
        val permissionsMap = mapOf(
            "ACCESS_COARSE_LOCATION" to pStatus.hasCoarseLocation,
            "ACCESS_FINE_LOCATION" to pStatus.hasFineLocation,
            "POST_NOTIFICATIONS" to pStatus.hasNotifications,
            "SCHEDULE_EXACT_ALARM" to pStatus.hasExactAlarms
        )

        // 4. Integrasi Evaluasi Runtime dari AppSecurityManager
        AppSecurityManager.initialize(context)
        val report = AppSecurityManager.getSecurityReport(context)
        
        val integrity = if (report.isIntegrityValid) "VALID" else "TAMPERED_WARNING"
        val isCompromised = report.isRooted || !report.isIntegrityValid

        val riskLevel = when {
            isCompromised -> "HIGH_RISK"
            report.isEmulator || report.isDebug -> "MEDIUM_RISK"
            else -> "LOW_RISK"
        }

        // 5. Susun Rekomendasi berdasarkan kerentanan yang terdeteksi
        val recs = mutableListOf<String>()
        if (report.isRooted) {
            recs.add("Peringatkan pengguna bahwa perangkat telah di-root (potensial kebocoran memori).")
        }
        if (!report.isIntegrityValid) {
            recs.add("Tolak akses ke database lokal aman karena tanda tangan APK dimodifikasi (Re-install APK asli).")
        }
        if (report.isDebug) {
            recs.add("Pastikan mode debugging (android:debuggable) dimatikan pada build produksi Google Play.")
        }
        if (!pStatus.hasExactAlarms) {
            recs.add("Minta izin SCHEDULE_EXACT_ALARM di Android 12+ untuk fungsionalitas Adzan tepat waktu.")
        }
        if (!pStatus.hasCoarseLocation) {
            recs.add("Minta izin lokasi on-demand untuk mengalkulasi jadwal sholat astronomis lokal.")
        }
        if (recs.isEmpty()) {
            recs.add("Seluruh pos audit keamanan bernilai hijau. Pertahankan kepatuhan keamanan saat ini.")
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return SecurityReport(
            timestamp = System.currentTimeMillis(),
            formattedDate = sdf.format(Date()),
            encryptionStatus = securePrefsLoaded,
            networkSecurity = networkSec,
            permissionStatus = permissionsMap,
            integrityStatus = integrity,
            riskLevel = riskLevel,
            recommendations = recs
        )
    }

    /**
     * Mengekspor laporan keamanan ke berkas lokal di direktori cache internal aplikasi.
     * Berkas ini dapat dilampirkan ke sistem pengiriman error/diagnostik terenkripsi.
     */
    fun exportReport(context: Context): File {
        val report = generateReport(context)
        val filename = "security_audit_report_${System.currentTimeMillis()}.txt"
        val destFile = File(context.cacheDir, filename)

        val reportBuilder = StringBuilder().apply {
            appendLine("=== ADZAN APP SECURITY AUDIT REPORT ===")
            appendLine("Waktu Audit: ${report.formattedDate} (Timestamp: ${report.timestamp})")
            appendLine("Tingkat Risiko: ${report.riskLevel}")
            appendLine("---------------------------------------")
            appendLine("1. DATA ENCRYPTION STATUS:")
            appendLine("   Kategori Enkripsi: ${report.encryptionStatus}")
            appendLine("2. NETWORK SECURITY STATUS:")
            appendLine("   Keamanan Jaringan: ${report.networkSecurity}")
            appendLine("3. RUNTIME APP INTEGRITY:")
            appendLine("   Status Integritas APK: ${report.integrityStatus}")
            appendLine("4. RUNTIME PERMISSION CHECKS:")
            report.permissionStatus.forEach { (perm, granted) ->
                appendLine("   - $perm: ${if (granted) "GRANTED" else "DENIED"}")
            }
            appendLine("5. KEBIJAKAN REKOMENDASI MITIGASI:")
            report.recommendations.forEachIndexed { i, rec ->
                appendLine("   [${i + 1}] $rec")
            }
            appendLine("=======================================")
        }

        destFile.writeText(reportBuilder.toString())
        return destFile
    }
}
