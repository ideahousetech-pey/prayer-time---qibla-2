package id.ideahousetech.prayertime_qibla.model

import id.ideahousetech.prayertime_qibla.data.PrayerTracker

enum class PrayerName(val arabicName: String, val indonesianName: String) {
    SUBUH("الفجر", "Subuh"),
    DZUHUR("الظهر", "Dzuhur"),
    ASHAR("العصر", "Ashar"),
    MAGHRIB("المغرب", "Maghrib"),
    ISYA("العشاء", "Isya");

    /**
     * Helper untuk mengupdate status sholat terkait pada entity [PrayerTracker]
     */
    fun updateTrackerStatus(tracker: PrayerTracker, status: PrayerStatus): PrayerTracker {
        return when (this) {
            SUBUH -> tracker.copy(subuhStatus = status)
            DZUHUR -> tracker.copy(dhuhrStatus = status)
            ASHAR -> tracker.copy(asrStatus = status)
            MAGHRIB -> tracker.copy(maghribStatus = status)
            ISYA -> tracker.copy(isyaStatus = status)
        }
    }

    companion object {
        fun fromString(value: String?): PrayerName? {
            if (value == null) return null
            val upper = value.uppercase()
            return entries.find { 
                it.name == upper || 
                it.indonesianName.uppercase() == upper ||
                it.arabicName == value ||
                (upper == "FAJR" && it == SUBUH) ||
                (upper == "DHUHR" && it == DZUHUR) ||
                (upper == "ASR" && it == ASHAR) ||
                (upper == "ISHA" && it == ISYA)
            }
        }
    }
}
