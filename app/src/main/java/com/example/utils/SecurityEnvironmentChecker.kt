package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Debug

/**
 * Utilitas untuk memverifikasi kondisi lingkungan eksekusi aplikasi.
 * Mendeteksi debugger yang terpasang (debugging) dan mendeteksi eksekusi dalam emulator (Android Virtual Device).
 */
object SecurityEnvironmentChecker {

    /**
     * Memeriksa apakah debugger sedang terhubung ke proses aplikasi saat ini.
     */
    fun isDebuggerConnected(context: Context): Boolean {
        // Cek koneksi debugger runtime langsung
        if (Debug.isDebuggerConnected() || Debug.waitingForDebugger()) {
            return true
        }
        
        // Periksa juga apakah flag debuggable aktif di manifes aplikasi
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        
        // Debugger hanya menjadi isu keamanan sejati bila aktif di luar build mode debug kita.
        return isDebuggable && Debug.isDebuggerConnected()
    }

    /**
     * Memeriksa apakah aplikasi berjalan di atas lingkungan emulator.
     * Menggunakan kriteria heuristik ringan (lightweight) tanpa memblokir testing emulator.
     */
    fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BOARD == "goldfish"
                || Build.BOARD == "vbox86"
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.HARDWARE.contains("vbox86")
                || Build.PRODUCT.contains("sdk_gphone")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("google_sdk"))
    }
}
