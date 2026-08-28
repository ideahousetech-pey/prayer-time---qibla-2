package id.ideahousetech.prayertime_qibla.model

enum class PrayerStatus(val displayName: String) {
    NONE("None"),
    MUNFARID("Munfarid"),
    JAMAAH("Jamaah"),
    MASBUQ("Masbuq"),
    HALANGAN("Halangan");

    companion object {
        fun fromString(value: String?): PrayerStatus {
            if (value == null) return NONE
            return entries.find { 
                it.displayName.equals(value, ignoreCase = true) || 
                it.name.equals(value, ignoreCase = true) 
            } ?: NONE
        }
    }
}
