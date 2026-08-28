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
    val status: String = STATUS_BELUM, // "belum" | "selesai"
    val updatedAt: Timestamp = Timestamp.now()
) {
    companion object {
        const val STATUS_BELUM = "belum"
        const val STATUS_SELESAI = "selesai"
    }

    fun isSelesai(): Boolean = status == STATUS_SELESAI
}
