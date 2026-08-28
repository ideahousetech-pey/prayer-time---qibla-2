package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.util.Log

/**
 * Manajer keamanan terpusat (Central Security Manager).
 * Bertanggung jawab mengevaluasi status keamanan perangkat, integritas aplikasi, dan
 * mengkalkulasi tingkat kepatuhan keamanan runtime secara proporsional untuk aplikasi ibadah.
 */
object AppSecurityManager {

    private const val TAG = "AppSecurityManager"

    // Status keamanan runtime
    var isDeviceRooted: Boolean = false
        private set
        
    var isDebugging: Boolean = false
        private set
        
    var isRunningOnEmulator: Boolean = false
        private set
        
    var isAppIntegrityValid: Boolean = true
        private set
        
    var securityLevel: SecurityLevel = SecurityLevel.HIGH
        private set

    /**
     * Tingkat Keamanan Aplikasi berdasarkan evaluasi ancaman runtime.
     */
    enum class SecurityLevel { 
        HIGH,        // Aman sepenuhnya
        MEDIUM,      // Berjalan di Emulator / Debugger Aktif
        LOW,         // Perangkat di-root (Gentle Warning, tetap boleh pakai)
        COMPROMISED  // Integritas APK dirusak / Tampered (Sangat Berbahaya)
    }

    /**
     * Laporan lengkap hasil pemindaian runtime keamanan.
     */
    data class SecurityReport(
        val isRooted: Boolean,
        val isDebug: Boolean,
        val isEmulator: Boolean,
        val isIntegrityValid: Boolean,
        val appSignature: String,
        val securityLevel: SecurityLevel,
        val checkTime: Long
    )

    /**
     * Inisialisasi status keamanan saat aplikasi diluncurkan.
     * Dipanggil sekali di MainActivity.onCreate().
     */
    fun initialize(context: Context) {
        try {
            isDeviceRooted = RootDetector.isDeviceRooted(context)
            isDebugging = SecurityEnvironmentChecker.isDebuggerConnected(context)
            isRunningOnEmulator = SecurityEnvironmentChecker.isRunningOnEmulator()
            isAppIntegrityValid = AppIntegrityChecker.isSignatureValid(context)

            // Hitung SecurityLevel secara hierarkis
            securityLevel = when {
                !isAppIntegrityValid -> SecurityLevel.COMPROMISED
                isDeviceRooted -> SecurityLevel.LOW
                isDebugging || isRunningOnEmulator -> SecurityLevel.MEDIUM
                else -> SecurityLevel.HIGH
            }

            Log.i(TAG, "Security initialized successfully. Level: $securityLevel")
            Log.d(TAG, "Rooted: $isDeviceRooted, Debug: $isDebugging, Emulator: $isRunningOnEmulator, Integrity: $isAppIntegrityValid")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menginisialisasi security manager: ${e.message}", e)
            // Default fallback ke kondisi aman namun skeptis
            securityLevel = SecurityLevel.MEDIUM
        }
    }

    /**
     * Mengembalikan laporan detail keamanan saat ini.
     */
    fun getSecurityReport(context: Context): SecurityReport {
        return SecurityReport(
            isRooted = isDeviceRooted,
            isDebug = isDebugging,
            isEmulator = isRunningOnEmulator,
            isIntegrityValid = isAppIntegrityValid,
            appSignature = AppIntegrityChecker.getAppSignature(context),
            securityLevel = securityLevel,
            checkTime = System.currentTimeMillis()
        )
    }

    /**
     * Menentukan apakah data sensitif (seperti custom audio upload, credentials API) dapat diakses.
     * Demi keamanan, akses dibatasi jika integritas APK dirusak (Compromised), 
     * tetapi tetap diperbolehkan jika hanya rooted (Gentle approach).
     */
    fun canAccessSensitiveData(): Boolean {
        return securityLevel != SecurityLevel.COMPROMISED
    }

    /**
     * Menentukan apakah dialog peringatan keamanan harus ditampilkan ke pengguna.
     * Peringatan ditampilkan jika perangkat terdeteksi Rooted atau APK dirusak (Compromised).
     */
    fun shouldShowSecurityWarning(): Boolean {
        return securityLevel == SecurityLevel.LOW || securityLevel == SecurityLevel.COMPROMISED
    }

    /**
     * Utilitas Proteksi Memori: Menghapus data sensitif (seperti kata sandi / token API) 
     * dari RAM setelah digunakan dengan mengisi array karakter dengan nol.
     * Hal ini jauh lebih aman daripada objek String yang nilainya tidak dapat diubah (immutable)
     * dan bertahan di garbage collector untuk waktu yang tidak ditentukan.
     */
    fun clearSensitiveData(charArray: CharArray?) {
        if (charArray == null) return
        for (i in charArray.indices) {
            charArray[i] = '\u0000'
        }
    }

    /**
     * Versi byte array untuk proteksi memori buffer data sensitif (seperti kunci kriptografi).
     */
    fun clearSensitiveBytes(byteArray: ByteArray?) {
        if (byteArray == null) return
        for (i in byteArray.indices) {
            byteArray[i] = 0
        }
    }
}
