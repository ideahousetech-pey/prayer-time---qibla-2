package id.ideahousetech.prayertime_qibla.model.ramadhan

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Model data Kelas Ramadhan (misal: 1A, 2A, ..., 6A).
 * [classCode] merupakan kode unik aktivasi untuk pendaftaran siswa (mis. "6A-RMD26").
 */
data class RamadhanClass(
    val classId: String = "",
    val label: String = "",           // contoh "6A"
    val teacherId: String = "",
    val classCode: String = "",       // kode unik aktivasi siswa, mis. "6A-RMD26"
    @get:PropertyName("active") @set:PropertyName("active")
    var isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)
