package id.ideahousetech.prayertime_qibla.utils

/**
 * Mengkategorikan data berdasarkan tingkat sensitivitas keamanan.
 * Digunakan untuk memisahkan penanganan data Plain vs Encrypted secara eksplisit.
 */
enum class DataSensitivity {
    /**
     * Data yang sangat sensitif, wajib dienkripsi kuat menggunakan Android KeyStore (AES256-GCM).
     * Jika enkripsi gagal, data ini tidak boleh disimpan dalam bentuk plaintext mentah demi kepatuhan GDPR/APPI.
     */
    CRITICAL,

    /**
     * Data fungsionalitas pengguna yang cukup sensitif (seperti konfigurasi alarm harian, offset waktu).
     * Sangat disarankan dienkripsi, namun aman ditoleransi fallback terbatas jika KeyStore rusak.
     */
    SENSITIVE,

    /**
     * Data umum non-pribadi yang tidak membahayakan privasi pengguna (misal tema aplikasi gelap/terang, onboarding).
     * Boleh disimpan dalam Plaintext SharedPreferences standar tanpa enkripsi.
     */
    NON_SENSITIVE
}

/**
 * Menyediakan pemetaan tingkat sensitivitas untuk setiap pref key yang digunakan aplikasi.
 */
fun PrefsKeys.getSensitivity(key: String): DataSensitivity {
    return when (key) {
        PrefsKeys.CACHED_LAT,
        PrefsKeys.CACHED_LON,
        PrefsKeys.CACHED_ADDRESS -> DataSensitivity.CRITICAL

        PrefsKeys.ENABLE_ADZAN_ALARM,
        PrefsKeys.ENABLE_DAILY_REMINDER,
        PrefsKeys.STREAK_STRICT_MODE,
        PrefsKeys.PRAYER_TIME_OFFSET,
        PrefsKeys.CUSTOM_ADZAN_NAME,
        PrefsKeys.CUSTOM_ADZAN_FAJR_NAME -> DataSensitivity.SENSITIVE

        PrefsKeys.APP_THEME_MODE,
        PrefsKeys.IS_ONBOARDING_COMPLETED,
        PrefsKeys.IS_MANUAL_LOCATION -> DataSensitivity.NON_SENSITIVE

        else -> DataSensitivity.NON_SENSITIVE
    }
}
