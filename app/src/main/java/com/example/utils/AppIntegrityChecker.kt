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
    private const val EXPECTED_SIGNATURE = "FA:5B:C8:12:34:56:78:90:AB:CD:EF:FE:DC:BA:09:87:65:43:21:0F:AB:CD:EF:01:23:45:67:89:AB:CD:EF:01"
    
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
