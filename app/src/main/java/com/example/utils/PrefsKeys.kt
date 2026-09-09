package id.ideahousetech.prayertime_qibla.utils

object PrefsKeys {
    const val APP_THEME_MODE = "app_theme_mode"
    const val ENABLE_ADZAN_ALARM = "enable_adzan_alarm"
    const val IS_ONBOARDING_COMPLETED = "is_onboarding_completed"
    const val IS_MANUAL_LOCATION = "is_manual_location"
    const val CACHED_LAT = "cached_lat"
    const val CACHED_LON = "cached_lon"
    const val CACHED_ADDRESS = "cached_address"
    const val PRAYER_TIME_OFFSET = "prayer_time_offset"
    const val ENABLE_DAILY_REMINDER = "enable_daily_reminder"
    const val STREAK_STRICT_MODE = "streak_strict_mode"
    const val CUSTOM_ADZAN_NAME = "custom_adzan_name"
    const val CUSTOM_ADZAN_FAJR_NAME = "custom_adzan_fajr_name"

    // Flag popup musiman Ramadhan & Zakat Fitrah per tahun Hijriah
    const val PREFIX_RAMADHAN_GREETING_SHOWN = "ramadhan_greeting_shown_"
    const val PREFIX_ZAKAT_REMINDER_SHOWN = "zakat_reminder_shown_"

    fun getRamadhanGreetingKey(hijriYear: Int): String = "$PREFIX_RAMADHAN_GREETING_SHOWN${hijriYear}H"
    fun getZakatReminderKey(hijriYear: Int): String = "$PREFIX_ZAKAT_REMINDER_SHOWN${hijriYear}H"
}
