package id.ideahousetech.prayertime_qibla.data

import androidx.room.TypeConverter
import id.ideahousetech.prayertime_qibla.model.PrayerStatus

class PrayerStatusConverter {
    @TypeConverter
    fun fromString(value: String?): PrayerStatus {
        return PrayerStatus.fromString(value)
    }

    @TypeConverter
    fun toString(status: PrayerStatus?): String {
        return status?.displayName ?: PrayerStatus.NONE.displayName
    }
}
