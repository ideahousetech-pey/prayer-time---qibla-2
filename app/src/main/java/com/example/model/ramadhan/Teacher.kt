package id.ideahousetech.prayertime_qibla.model.ramadhan

import com.google.firebase.Timestamp

/**
 * Model data Guru / Tenaga Pendidik.
 * Role:
 * - "guru" (default) : hanya mengelola kelas yang dimilikinya.
 * - "koordinator"     : dapat memantau seluruh kelas 1A-6A (read-only lintas kelas).
 */
data class Teacher(
    val teacherId: String = "",
    val name: String = "",
    val email: String = "",
    val classIds: List<String> = emptyList(),
    val role: String = ROLE_GURU
) {
    companion object {
        const val ROLE_GURU = "guru"
        const val ROLE_KOORDINATOR = "koordinator"
    }

    fun isKoordinator(): Boolean = role == ROLE_KOORDINATOR
}
