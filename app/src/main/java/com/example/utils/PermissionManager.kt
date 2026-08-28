package id.ideahousetech.prayertime_qibla.utils

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Status kelayakan seluruh izin aplikasi (Permissions Status bundle).
 */
data class PermissionStatus(
    val hasCoarseLocation: Boolean,
    val hasFineLocation: Boolean,
    val hasNotifications: Boolean,
    val hasExactAlarms: Boolean,
    val isReadyForAdzan: Boolean
)

/**
 * Pengendali Izin Runtime (Runtime Permission Manager).
 * Memfasilitasi pemeriksaan status izin, penyediaan penjelasan rasionalitas (rationale),
 * penanganan penolakan permanen (permanently denied), dan navigasi ke halaman pengaturan sistem.
 */
class PermissionManager {

    companion object {
        const val REQUEST_CODE_SETTINGS = 1001
        
        // Daftar semua izin dasar yang sebaiknya diminta di awal / on-demand
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    /**
     * Memeriksa seluruh kelompok izin krusial yang diperlukan untuk fungsi utama aplikasi.
     */
    fun checkAllRequiredPermissions(context: Context): PermissionStatus {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Otomatis aktif di bawah Android 13
        }

        val hasExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true // Otomatis diizinkan di bawah Android 12
        }

        // Aplikasi dianggap "siap" beroperasi dasar jika minimal memiliki izin lokasi coarse dan notifikasi
        val isReady = hasCoarse && hasNotifications && hasExactAlarms

        return PermissionStatus(
            hasCoarseLocation = hasCoarse,
            hasFineLocation = hasFine,
            hasNotifications = hasNotifications,
            hasExactAlarms = hasExactAlarms,
            isReadyForAdzan = isReady
        )
    }

    /**
     * Meminta izin runtime tertentu dengan fungsionalitas callback rationale.
     * Catatan: Desain UX modern Jetpack Compose biasanya merekomendasikan penanganan 
     * menggunakan library Accompanist / Compose APIs, tetapi class penolong ini 
     * memfasilitasi integrasi logic penentuan rationale yang solid.
     */
    fun requestPermissionWithRationale(
        permission: String,
        rationale: String,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        // Logika penengah keputusan: dapat digunakan oleh Activity/Composable
        // untuk mendemonstrasikan rationale dialog jika shouldShowRequestPermissionRationale bernilai true.
    }

    /**
     * Menangani kasus penolakan izin secara permanen (permanently denied).
     * Membuka menu pengaturan aplikasi (App Settings) di sistem Android agar pengguna dapat mengaktifkan secara manual.
     */
    fun handlePermanentlyDenied(context: Context, permission: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Mengarahkan pengguna secara khusus ke pengaturan izin "Alarm & Reminder" (Exact Alarms) pada Android 12+.
     */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}
