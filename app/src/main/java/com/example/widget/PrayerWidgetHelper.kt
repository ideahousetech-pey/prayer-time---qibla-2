package id.ideahousetech.prayertime_qibla.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import id.ideahousetech.prayertime_qibla.MainActivity
import id.ideahousetech.prayertime_qibla.R
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.service.PrayerService
import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import id.ideahousetech.prayertime_qibla.utils.getDouble
import id.ideahousetech.prayertime_qibla.utils.pendingIntentFlags
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import id.ideahousetech.prayertime_qibla.utils.IntentSecurityUtils

/**
 * Utilitas helper mandiri untuk mengelola, mengkalkulasi, dan merender views widget Waktu Sholat dan Kiblat.
 * Mendukung pembaharuan luring cepat, kalkulasi deterministik bebas leak, serta pencocokan warna visual Islamic Luxury.
 */
object PrayerWidgetHelper {

    /**
     * Memperbarui seluruh widget yang ada di layar dengan data terbaru secara luring.
     */
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Update Small widget
        val smallComponent = ComponentName(context, PrayerSmallWidgetProvider::class.java)
        val smallIds = appWidgetManager.getAppWidgetIds(smallComponent)
        if (smallIds.isNotEmpty()) {
            updateSmallWidgets(context, appWidgetManager, smallIds)
        }

        // Update Medium widget
        val mediumComponent = ComponentName(context, PrayerMediumWidgetProvider::class.java)
        val mediumIds = appWidgetManager.getAppWidgetIds(mediumComponent)
        if (mediumIds.isNotEmpty()) {
            updateMediumWidgets(context, appWidgetManager, mediumIds)
        }

        // Update Large widget
        val largeComponent = ComponentName(context, PrayerLargeWidgetProvider::class.java)
        val largeIds = appWidgetManager.getAppWidgetIds(largeComponent)
        if (largeIds.isNotEmpty()) {
            updateLargeWidgets(context, appWidgetManager, largeIds)
        }
    }

    /**
     * Merender data ke widget ukuran Small (2x2)
     */
    fun updateSmallWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val data = getTodayWidgetData(context) ?: return
        val pendingIntent = createClickIntent(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_small_layout)
            
            views.setTextViewText(R.id.widget_prayer_name, data.nextPrayerName)
            views.setTextViewText(R.id.widget_prayer_time, data.nextPrayerTime)
            views.setTextViewText(R.id.widget_prayer_countdown, data.countdownString)
            
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Merender data ke widget ukuran Medium (4x2)
     */
    fun updateMediumWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val data = getTodayWidgetData(context) ?: return
        val pendingIntent = createClickIntent(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_medium_layout)
            
            views.setTextViewText(R.id.widget_prayer_name, data.nextPrayerName)
            views.setTextViewText(R.id.widget_prayer_time, "${data.nextPrayerTime} WIB")
            views.setTextViewText(R.id.widget_prayer_countdown, "Hingga ${data.nextPrayerName}: ${data.countdownString}")
            views.setTextViewText(R.id.widget_hijri_date, data.hijriDate)
            views.setTextViewText(R.id.widget_gregorian_date, data.gregorianDate)
            views.setTextViewText(R.id.widget_location, data.address)
            
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Merender data ke widget ukuran Large (4x4)
     */
    fun updateLargeWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val data = getTodayWidgetData(context) ?: return
        val pendingIntent = createClickIntent(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_large_layout)
            
            views.setTextViewText(R.id.widget_next_prayer_title, "Hingga ${data.nextPrayerName}")
            views.setTextViewText(R.id.widget_prayer_countdown, data.countdownString)
            views.setTextViewText(R.id.widget_hijri_date, data.hijriDate)
            views.setTextViewText(R.id.widget_gregorian_date, data.gregorianDate)
            views.setTextViewText(R.id.widget_location, data.address)

            // Atur waktu sholat dari 5 jadwal utama hari ini
            views.setTextViewText(R.id.time_fajr, data.prayerTime.fajr)
            views.setTextViewText(R.id.time_dhuhr, data.prayerTime.dhuhr)
            views.setTextViewText(R.id.time_asr, data.prayerTime.asr)
            views.setTextViewText(R.id.time_maghrib, data.prayerTime.maghrib)
            views.setTextViewText(R.id.time_isha, data.prayerTime.isha)

            // Beri visual highlight transparan pada sholat berikutnya jika perlu (bisa dengan backgroud warna)
            // Di RemoteViews kita bisa mengubah visibilitas atau background jika diinginkan.
            // Biar tampilan senantiasa elegan & solid, kita fokus menyajikan nilai teks presisi.

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * Memicu Intent untuk meluncurkan aplikasi utama saat widget diketuk.
     */
    private fun createClickIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return IntentSecurityUtils.createSecurePendingIntent(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT,
            IntentSecurityUtils.PendingIntentType.ACTIVITY
        )
    }

    /**
     * Ambil data kalkulasi terbaru luring.
     */
    fun getTodayWidgetData(context: Context): ModelWidgetData? {
        try {
            val cal = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH) + 1
            val year = cal.get(Calendar.YEAR)

            // Tarik info lokasi ter-cache
            val cachePrefs = context.getSharedPreferences("user_location_cache", Context.MODE_PRIVATE)
            val lat = cachePrefs.getDouble("cached_lat", -6.175115)
            val lon = cachePrefs.getDouble("cached_lon", 106.827157)
            val address = cachePrefs.getString("cached_address", "Menteng, Jakarta Pusat") ?: "Menteng, Jakarta Pusat"

            // Hitung jadwal sholat luring untuk hari ini
            val prayerService = PrayerService(context)
            val monthlyList = prayerService.calculateOfflineMonthlyPrayerTimes(lat, lon, month, year)
            val index = (day - 1).coerceIn(0, monthlyList.size - 1)
            val todayPrayer = if (monthlyList.isNotEmpty()) monthlyList[index] else return null

            // Format penanggalan masehi & hijriah
            val formatG = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            val gregorianDate = formatG.format(cal.time)
            val hijriDate = HijriDateUtils.convertToHijri(cal).formatted

            // Kalkulasi Sholat Berikutnya & Teks Countdown
            val currentMillis = cal.timeInMillis
            val prayerTimesList = listOf(
                Triple("Subuh", todayPrayer.fajr, false),
                Triple("Dzuhur", todayPrayer.dhuhr, false),
                Triple("Ashar", todayPrayer.asr, false),
                Triple("Maghrib", todayPrayer.maghrib, false),
                Triple("Isya", todayPrayer.isha, false)
            )

            var foundNext = false
            var nextPrName = "Subuh"
            var nextPrTime = todayPrayer.fajr
            var isNextPrTomorrow = false

            for (p in prayerTimesList) {
                val pCal = parseTimeStringToCalendar(p.second, false)
                if (pCal.timeInMillis > currentMillis) {
                    nextPrName = p.first
                    nextPrTime = p.second
                    foundNext = true
                    break
                }
            }

            if (!foundNext) {
                nextPrName = "Subuh"
                nextPrTime = todayPrayer.fajr
                isNextPrTomorrow = true
            }

            val targetCal = parseTimeStringToCalendar(nextPrTime, isNextPrTomorrow)
            val diff = targetCal.timeInMillis - currentMillis
            val countdown = formatMillisToCountdown(diff)

            return ModelWidgetData(
                gregorianDate = gregorianDate,
                hijriDate = hijriDate,
                address = address,
                nextPrayerName = nextPrName,
                nextPrayerTime = nextPrTime,
                countdownString = countdown,
                prayerTime = todayPrayer
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseTimeStringToCalendar(timeStr: String, isTomorrow: Boolean): Calendar {
        val parts = timeStr.split(":")
        val h = parts[0].trim().toInt()
        val m = parts[1].trim().toInt()

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (isTomorrow) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }

    private fun formatMillisToCountdown(millis: Long): String {
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        
        return if (hours > 0) {
            "%02d:%02d:%02d".format(Locale.US, hours, minutes, seconds)
        } else {
            "%02d:%02d".format(Locale.US, minutes, seconds)
        }
    }

    data class ModelWidgetData(
        val gregorianDate: String,
        val hijriDate: String,
        val address: String,
        val nextPrayerName: String,
        val nextPrayerTime: String,
        val countdownString: String,
        val prayerTime: PrayerTime
    )
}
