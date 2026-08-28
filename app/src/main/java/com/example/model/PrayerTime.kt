package id.ideahousetech.prayertime_qibla.model

import com.squareup.moshi.JsonClass

/**
 * Model data untuk mempresentasikan jadwal waktu sholat harian.
 * Berisi waktu sholat untuk Subuh (Fajr), Dzuhur (Dhuhr), Ashar (Asr), Maghrib, dan Isya (Isha).
 * Dilengkapi dengan format tanggal Gregorian serta tanggal Hijriah terkait.
 */
@JsonClass(generateAdapter = true)
data class PrayerTime(
    val dateGregorian: String,      // Format: "Rabu, 13 Mei 2026"
    val dateHijri: String,          // Format: "13 Dzulqa'dah 1447 H"
    val fajr: String,               // Subuh: "04:35"
    val dhuhr: String,              // Dzuhur: "12:00"
    val asr: String,                // Ashar: "15:15"
    val maghrib: String,            // Maghrib: "18:05"
    val isha: String                // Isya: "19:15"
)

/**
 * Model data untuk hari besar / hari penting Islam.
 * Digunakan untuk menandai kalender Hijriah dan menampilkan popup dihalaman utama.
 */
data class IslamicHoliday(
    val hijriDate: String,          // Format "DD-MM" (contoh: "01-10" untuk Syawal)
    val name: String,               // Nama hari besar, misal: "Hari Raya Idul Fitri"
    val description: String,        // Amalan/Deskripsi singkat mengenai hari besar tersebut
    val history: String = "",       // Sejarah singkat hari raya tersebut
    val quranHadith: String = ""    // Dalil Al-Qur'an / Hadits terkait keutamaannya
)
