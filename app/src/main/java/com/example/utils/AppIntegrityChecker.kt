package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest
import java.util.Locale

/**
 * Memverifikasi integritas berkas APK dari modifikasi tidak sah (anti-tamper).
 * Memeriksa kesesuaian package name dan tanda tangan sertifikat digital (Signature Fingerprint).
 */
object AppIntegrityChecker {

    // Ganti ini dengan SHA-256 asli sertifikat rilis Anda jika sudah siap dipublikasikan
    private const val EXPECTED_SIGNATURE = "85:E7:64:14:0C:82:94:9B:43:45:90:6E:EA:DA:DE:3F:5A:2A:DD:FA:9F:4B:68:BB:80:C5:1B:5A:8C:5D:4F:EB"
    
    // Package name resmi aplikasi
    private const val EXPECTED_PACKAGE_NAME = "id.ideahousetech.prayertime_qibla"

    /**
     * Memeriksa apakah tanda tangan digital aplikasi valid.
     * Catatan: Kami melonggarkan pengecekan untuk mode DEBUG agar tidak menghalangi
     * proses development di emulator/AI Studio, namun akan sangat ketat pada build rilis (Production).
     */
    fun isSignatureValid(context: Context): Boolean {
        // Pengecekan Nama Paket
        val currentPackageName = context.packageName
        if (currentPackageName != EXPECTED_PACKAGE_NAME) {
            return false
        }

        // Pengecekan Fingerprint Sertifikat
        val currentSignature = getAppSignature(context)
        
        // bypass jika dalam mode debug untuk kenyamanan dev
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            return true
        }

        return currentSignature.equals(EXPECTED_SIGNATURE, ignoreCase = true)
    }

    /**
     * Mendapatkan SHA-256 fingerprint dari tanda tangan sertifikat aplikasi saat ini.
     */
    fun getAppSignature(context: Context): String {
        return try {
            val pm = context.packageManager
            val packageName = context.packageName
            val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures ?: emptyArray()
            }

            if (signatures.isNotEmpty()) {
                val certBytes = signatures[0].toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(certBytes)
                
                // Ubah menjadi format heksadesimal ber-titik dua (e.g. AA:BB:CC...)
                digest.joinToString(":") { byte -> 
                    String.format("%02X", byte) 
                }
            } else {
                "NO_SIGNATURE_FOUND"
            }
        } catch (e: Exception) {
            "ERROR_GETTING_SIGNATURE"
        }
    }
}
