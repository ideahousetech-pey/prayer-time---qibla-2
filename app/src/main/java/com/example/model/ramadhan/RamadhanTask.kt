package id.ideahousetech.prayertime_qibla.model.ramadhan

import com.google.firebase.Timestamp

/**
 * Model data Tugas Harian Ramadhan (30 Hari).
 * [taskDate] adalah tanggal kalender konkret (jam 00:00 WIB) sebagai sumber kebenaran validasi lock.
 * [classId] null menandakan tugas default untuk semua kelas.
 */
data class RamadhanTask(
    val taskId: String = "",
    val title: String = "",
    val dayIndex: Int = 1,             // hari ke berapa dalam Ramadhan, 1-30
    val taskDate: Timestamp = Timestamp.now(), // tanggal kalender KONKRET jam 00:00 WIB
    val classId: String? = null,       // null = tugas default untuk semua kelas
    val createdBy: String = "system"   // teacherId, atau "system"
)
