package id.ideahousetech.prayertime_qibla.utils

import id.ideahousetech.prayertime_qibla.model.PrayerTime
import java.util.Calendar

/**
 * Fungsi utilitas untuk menghitung persentase progres waktu sholat saat ini
 * menuju ke sholat berikutnya.
 */
fun calculatePrayerProgress(
    todaySchedule: PrayerTime?,
    nextPrayerName: String
): Float {
    if (todaySchedule == null) return 0.5f
    try {
        val now = Calendar.getInstance()
        val nowMillis = now.timeInMillis
        
        fun parseTime(timeStr: String, isTomorrow: Boolean = false): Calendar {
            val parts = timeStr.split(":")
            if (parts.size < 2) return Calendar.getInstance()
            val hour = parts[0].toIntOrNull() ?: 0
            val min = parts[1].toIntOrNull() ?: 0
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, min)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (isTomorrow) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal
        }

        val subuhToday = parseTime(todaySchedule.fajr)
        val dzuhurToday = parseTime(todaySchedule.dhuhr)
        val asharToday = parseTime(todaySchedule.asr)
        val maghribToday = parseTime(todaySchedule.maghrib)
        val isyaToday = parseTime(todaySchedule.isha)
        val subuhTomorrow = parseTime(todaySchedule.fajr, isTomorrow = true)

        val pairs = listOf(
            "SUBUH" to (parseTime(todaySchedule.isha).apply { add(Calendar.DAY_OF_YEAR, -1) } to subuhToday),
            "DZUHUR" to (subuhToday to dzuhurToday),
            "ASHAR" to (dzuhurToday to asharToday),
            "MAGHRIB" to (asharToday to maghribToday),
            "ISYA" to (maghribToday to isyaToday),
            "SUBUH (ESOK)" to (isyaToday to subuhTomorrow)
        )

        val normalizedNextName = nextPrayerName.uppercase()
        val matchKey = if (normalizedNextName.contains("BESOK") || normalizedNextName.contains("ESOK")) {
            "SUBUH (ESOK)"
        } else if (normalizedNextName.contains("SUBUH") || normalizedNextName.contains("FAJR")) {
            "SUBUH"
        } else if (normalizedNextName.contains("DZUHUR") || normalizedNextName.contains("DHUHR")) {
            "DZUHUR"
        } else if (normalizedNextName.contains("ASHAR") || normalizedNextName.contains("ASR")) {
            "ASHAR"
        } else if (normalizedNextName.contains("MAGHRIB")) {
            "MAGHRIB"
        } else if (normalizedNextName.contains("ISYA") || normalizedNextName.contains("ISHA")) {
            "ISYA"
        } else {
            ""
        }

        val matchingPair = pairs.find { it.first == matchKey }?.second ?: (subuhToday to dzuhurToday)
        val prevMillis = matchingPair.first.timeInMillis
        val nextMillis = matchingPair.second.timeInMillis

        if (nextMillis <= prevMillis) return 0.5f
        val fraction = (nowMillis - prevMillis).toFloat() / (nextMillis - prevMillis).toFloat()
        return fraction.coerceIn(0f, 1f)
    } catch (e: Exception) {
        return 0.5f
    }
}
