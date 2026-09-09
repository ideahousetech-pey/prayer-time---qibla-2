package id.ideahousetech.prayertime_qibla.model.ramadhan

import com.google.firebase.Timestamp

/**
 * Model data Pengumpulan / Pengerjaan Tugas Siswa.
 * [status] "belum" | "selesai"
 */
data class Submission(
    val submissionId: String = "",
    val studentId: String = "",
    val taskId: String = "",
    val prayerType: String = "",
    val status: String = STATUS_BELUM, // "belum" | "selesai"
    val updatedAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val STATUS_BELUM = "belum"
        const val STATUS_SELESAI = "selesai"

        const val PRAYER_SUBUH = "subuh"
        const val PRAYER_DZUHUR = "dzuhur"
        const val PRAYER_ASHAR = "ashar"
        const val PRAYER_MAGHRIB = "maghrib"
        const val PRAYER_ISYA = "isya"
        const val PRAYER_TARAWIH = "tarawih"
    }

    fun isSelesai(): Boolean = status == STATUS_SELESAI
}
