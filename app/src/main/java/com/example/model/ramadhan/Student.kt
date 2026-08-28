package id.ideahousetech.prayertime_qibla.model.ramadhan

import com.google.firebase.Timestamp

/**
 * Model data Siswa Ramadhan.
 * [studentId] sama dengan Firebase Auth UID milik siswa.
 * [nameLower] digunakan untuk query pencarian prefix berbasis index Firestore.
 */
data class Student(
    val studentId: String = "",
    val name: String = "",
    val nameLower: String = "",       // name.lowercase(), untuk search prefix
    val classId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
