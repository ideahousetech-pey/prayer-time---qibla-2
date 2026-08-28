package id.ideahousetech.prayertime_qibla.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import id.ideahousetech.prayertime_qibla.data.AppDatabase
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.getDouble
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Penerima Broadcast Pemulihan Sistem (Boot & Update Broadcast Receiver).
 * Memulihkan dan menjadwalkan ulang alarm Adzan yang dihapus oleh OS saat perangkat dimatikan/reboot
 * atau saat aplikasi diperbarui.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "onReceive dipanggil dengan aksi: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action || Intent.ACTION_MY_PACKAGE_REPLACED == action) {
            val pendingResult = goAsync()
            
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    rescheduleAlarms(context)
                    Log.i(TAG, "Sukses memulihkan penjadwalan alarm Adzan setelah reboot/update.")
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal memulihkan alarm Adzan: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    /**
     * Membaca konfigurasi koordinat terakhir dari cache aman, menarik jadwal sholat, 
     * dan mendaftarkan ulang alarm harian.
     */
    private suspend fun rescheduleAlarms(context: Context) {
        val prefs = SecurePrefs.get(context)
        
        // Baca koordinat terakhir, gunakan Jakarta sebagai fallback standar nasional
        val lastLat = prefs.getDouble(PrefsKeys.CACHED_LAT, -6.2088)
        val lastLon = prefs.getDouble(PrefsKeys.CACHED_LON, 106.8456)

        Log.d(TAG, "Melakukan pemulihan alarm dengan lokasi: Lat $lastLat, Lon $lastLon")

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH adalah 0-indexed
        val year = calendar.get(Calendar.YEAR)

        // Tarik jadwal dari PrayerService (mendukung cache database lokal & perhitungan offline otonom)
        val prayerService = PrayerService(context)
        val monthlyList = try {
            prayerService.getMonthlyPrayerTimes(lastLat, lastLon, month, year)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat jadwal sholat dari PrayerService, mencoba kalkulasi offline murni...", e)
            emptyList()
        }

        if (monthlyList.isNotEmpty()) {
            val dayIndex = (day - 1).coerceIn(0, monthlyList.size - 1)
            val todayData = monthlyList[dayIndex]
            
            // Daftarkan ulang alarm harian lewat NotificationService
            val notificationService = NotificationService.getInstance(context)
            notificationService.scheduleDailyAlarms(todayData)
            Log.d(TAG, "Alarms harian dijadwalkan ulang untuk tanggal hari ini: $day-$month-$year")
        } else {
            Log.e(TAG, "Gagal memulihkan alarm karena daftar jadwal sholat kosong.")
        }
    }
}
