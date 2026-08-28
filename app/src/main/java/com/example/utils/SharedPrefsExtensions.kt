package id.ideahousetech.prayertime_qibla.utils

import android.content.SharedPreferences

/**
 * Menyimpan nilai Double ke SharedPreferences menggunakan representasi bit Long
 * demi presisi penuh (64-bit IEEE 754 floating point).
 */
fun SharedPreferences.Editor.putDouble(key: String, value: Double): SharedPreferences.Editor {
    return this.putLong(key, java.lang.Double.doubleToRawLongBits(value))
}

/**
 * Mengambil nilai Double dari SharedPreferences dengan fallback transparan untuk
 * kompatibilitas mundur (bila sebelumnya disimpan sebagai Float).
 */
fun SharedPreferences.getDouble(key: String, default: Double): Double {
    if (!contains(key)) return default
    return try {
        java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))
    } catch (e: ClassCastException) {
        // Fallback transparan: Jika sebelumnya disimpan sebagai Float, baca lalu migrasikan ke Long secara otomatis
        val floatVal = try {
            getFloat(key, default.toFloat()).toDouble()
        } catch (e2: Exception) {
            default
        }
        try {
            edit().putLong(key, java.lang.Double.doubleToRawLongBits(floatVal)).apply()
        } catch (e3: Exception) {
            // Silently ignore if writing fails
        }
        floatVal
    }
}
