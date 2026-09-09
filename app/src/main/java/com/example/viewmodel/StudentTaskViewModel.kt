package id.ideahousetech.prayertime_qibla.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.model.ramadhan.RamadhanClass
import id.ideahousetech.prayertime_qibla.model.ramadhan.RamadhanTask
import id.ideahousetech.prayertime_qibla.model.ramadhan.Student
import id.ideahousetech.prayertime_qibla.model.ramadhan.Submission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class TaskLockState {
    ACTIVE,         // Waktu aktif: checklist dapat diubah
    EXPIRED,        // Waktu sudah lewat: terkunci
    NOT_STARTED     // Belum masuk waktunya: terkunci
}

data class PrayerSubmissionUiModel(
    val prayerType: String,
    val label: String,          // "Subuh", "Dzuhur", dst
    val submission: Submission?,
    val lockState: TaskLockState,
    val isSelesai: Boolean
)

data class TaskItemUiModel(
    val task: RamadhanTask,
    val prayerItems: List<PrayerSubmissionUiModel>  // selalu 6 item per hari
) {
    // Helper untuk kompatibilitas ke belakang
    val isSelesai: Boolean
        get() = prayerItems.isNotEmpty() && prayerItems.all { it.isSelesai }
    val submission: Submission?
        get() = prayerItems.firstOrNull { it.submission != null }?.submission
    val lockState: TaskLockState
        get() = prayerItems.firstOrNull()?.lockState ?: TaskLockState.NOT_STARTED
}

data class StudentTaskUiState(
    val isLoading: Boolean = false,
    val student: Student? = null,
    val studentClass: RamadhanClass? = null,
    val taskItems: List<TaskItemUiModel> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val snackbarMessage: String? = null,
    val errorMessage: String? = null
)

class StudentTaskViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var cachedTodayPrayerTime: PrayerTime? = null

    private val _uiState = MutableStateFlow(StudentTaskUiState())
    val uiState: StateFlow<StudentTaskUiState> = _uiState.asStateFlow()

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    private fun parseTimeSafe(timeStr: String?): LocalTime? {
        if (timeStr.isNullOrBlank()) return null
        return try {
            val clean = timeStr.trim().split(" ")[0]
            val parts = clean.split(":")
            LocalTime.of(parts[0].toInt(), parts[1].toInt())
        } catch (_: Exception) {
            null
        }
    }

    /*
     * CATATAN KEAMANAN:
     * Validasi lockState per-waktu-sholat ini HANYA berada di sisi client.
     * Firestore Rules cuma menjamin submission tidak bisa dibuat di luar RENTANG HARI (taskDate s/d taskDate + 1 hari).
     * Firestore Rules TIDAK bisa memverifikasi jam sholat spesifik karena itu bergantung pada lokasi GPS tiap siswa
     * yang tidak tersedia di rules. Ini adalah keterbatasan yang disengaja/diterima untuk skala project ini.
     */
    private fun calculatePrayerLockState(
        prayerType: String,
        now: LocalTime,
        fajr: LocalTime,
        dhuhr: LocalTime,
        asr: LocalTime,
        maghrib: LocalTime,
        isha: LocalTime
    ): TaskLockState {
        return when (prayerType) {
            Submission.PRAYER_SUBUH -> when {
                now.isBefore(fajr) -> TaskLockState.NOT_STARTED
                now.isBefore(dhuhr) -> TaskLockState.ACTIVE
                else -> TaskLockState.EXPIRED
            }
            Submission.PRAYER_DZUHUR -> when {
                now.isBefore(dhuhr) -> TaskLockState.NOT_STARTED
                now.isBefore(asr) -> TaskLockState.ACTIVE
                else -> TaskLockState.EXPIRED
            }
            Submission.PRAYER_ASHAR -> when {
                now.isBefore(asr) -> TaskLockState.NOT_STARTED
                now.isBefore(maghrib) -> TaskLockState.ACTIVE
                else -> TaskLockState.EXPIRED
            }
            Submission.PRAYER_MAGHRIB -> when {
                now.isBefore(maghrib) -> TaskLockState.NOT_STARTED
                now.isBefore(isha) -> TaskLockState.ACTIVE
                else -> TaskLockState.EXPIRED
            }
            Submission.PRAYER_ISYA, Submission.PRAYER_TARAWIH -> when {
                now.isBefore(isha) -> TaskLockState.NOT_STARTED
                else -> TaskLockState.ACTIVE
            }
            else -> TaskLockState.NOT_STARTED
        }
    }

    fun loadData(todayPrayerTime: PrayerTime? = cachedTodayPrayerTime) {
        if (todayPrayerTime != null) {
            cachedTodayPrayerTime = todayPrayerTime
        }
        val prayerTime = todayPrayerTime ?: cachedTodayPrayerTime

        val currentUser = auth.currentUser ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // 1. Ambil data Siswa
                val studentDoc = firestore.collection("students").document(currentUser.uid).get().await()
                val student = studentDoc.toObject(Student::class.java)

                var studentClass: RamadhanClass? = null
                if (student != null && student.classId.isNotEmpty()) {
                    val classDoc = firestore.collection("classes").document(student.classId).get().await()
                    studentClass = classDoc.toObject(RamadhanClass::class.java)
                }

                // 2. Ambil daftar Tugas (Tasks) urut berdasarkan dayIndex
                val tasksQuery = firestore.collection("tasks")
                    .whereEqualTo("classId", null)
                    .orderBy("dayIndex")
                    .get()
                    .await()

                val tasks = tasksQuery.documents.mapNotNull { it.toObject(RamadhanTask::class.java)?.copy(taskId = it.id) }

                // 3. Ambil pengumpulan (Submissions) milik siswa ini
                val subsQuery = firestore.collection("submissions")
                    .whereEqualTo("studentId", currentUser.uid)
                    .get()
                    .await()

                val subsMap = subsQuery.documents.mapNotNull { doc ->
                    val sub = doc.toObject(Submission::class.java)?.copy(submissionId = doc.id)
                    if (sub != null) {
                        val key = if (sub.prayerType.isNotEmpty()) "${sub.taskId}_${sub.prayerType}" else sub.taskId
                        key to sub
                    } else null
                }.toMap()

                val deviceZone = ZoneId.systemDefault()
                val todayLocalDate = LocalDate.now(deviceZone)
                val nowTime = LocalTime.now(deviceZone)

                val fajrTime = parseTimeSafe(prayerTime?.fajr)
                val dhuhrTime = parseTimeSafe(prayerTime?.dhuhr)
                val asrTime = parseTimeSafe(prayerTime?.asr)
                val maghribTime = parseTimeSafe(prayerTime?.maghrib)
                val ishaTime = parseTimeSafe(prayerTime?.isha)

                val prayerDefinitions = listOf(
                    Submission.PRAYER_SUBUH to "Subuh",
                    Submission.PRAYER_DZUHUR to "Dzuhur",
                    Submission.PRAYER_ASHAR to "Ashar",
                    Submission.PRAYER_MAGHRIB to "Maghrib",
                    Submission.PRAYER_ISYA to "Isya",
                    Submission.PRAYER_TARAWIH to "Tarawih"
                )

                val items = tasks.map { task ->
                    val taskLocalDate = Instant.ofEpochMilli(task.taskDate.toDate().time)
                        .atZone(deviceZone)
                        .toLocalDate()

                    val isPastDay = taskLocalDate.isBefore(todayLocalDate)
                    val isFutureDay = taskLocalDate.isAfter(todayLocalDate)

                    val prayerItems = prayerDefinitions.map { (pType, pLabel) ->
                        val subKey = "${task.taskId}_$pType"
                        val submission = subsMap[subKey] ?: subsMap[task.taskId]
                        val isSelesai = submission?.status == Submission.STATUS_SELESAI

                        val lockState = when {
                            isPastDay -> TaskLockState.EXPIRED
                            isFutureDay -> TaskLockState.NOT_STARTED
                            prayerTime == null -> TaskLockState.NOT_STARTED
                            fajrTime != null && dhuhrTime != null && asrTime != null && maghribTime != null && ishaTime != null -> {
                                calculatePrayerLockState(pType, nowTime, fajrTime, dhuhrTime, asrTime, maghribTime, ishaTime)
                            }
                            else -> TaskLockState.NOT_STARTED
                        }

                        PrayerSubmissionUiModel(
                            prayerType = pType,
                            label = pLabel,
                            submission = submission,
                            lockState = lockState,
                            isSelesai = isSelesai
                        )
                    }

                    TaskItemUiModel(
                        task = task,
                        prayerItems = prayerItems
                    )
                }

                val completed = items.sumOf { item -> item.prayerItems.count { it.isSelesai } }
                val total = items.size * 6

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    student = student,
                    studentClass = studentClass,
                    taskItems = items,
                    completedCount = completed,
                    totalCount = total
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat tugas: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                )
            }
        }
    }

    /**
     * Mengubah status pengerjaan sub-item waktu sholat (checklist aktif).
     * Jika ditolak oleh Security Rules, exception ditangkap dan menampilkan Snackbar tanpa crash aplikasi.
     */
    fun toggleTask(item: TaskItemUiModel, prayerItem: PrayerSubmissionUiModel) {
        val currentUser = auth.currentUser ?: return
        if (prayerItem.lockState != TaskLockState.ACTIVE) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (prayerItem.lockState == TaskLockState.EXPIRED) 
                    "Waktu ibadah ${prayerItem.label} sudah lewat, tidak dapat diubah lagi." 
                else "Belum masuk waktu ibadah ${prayerItem.label}."
            )
            return
        }

        val newStatus = if (prayerItem.isSelesai) Submission.STATUS_BELUM else Submission.STATUS_SELESAI
        val submissionId = "${currentUser.uid}_${item.task.taskId}_${prayerItem.prayerType}"

        viewModelScope.launch {
            try {
                val submission = Submission(
                    submissionId = submissionId,
                    studentId = currentUser.uid,
                    taskId = item.task.taskId,
                    prayerType = prayerItem.prayerType,
                    status = newStatus,
                    updatedAt = Timestamp.now()
                )

                firestore.collection("submissions").document(submissionId).set(submission).await()

                // Perbarui state lokal secara responsif
                val updatedItems = _uiState.value.taskItems.map { taskItem ->
                    if (taskItem.task.taskId == item.task.taskId) {
                        val updatedPrayers = taskItem.prayerItems.map { pItem ->
                            if (pItem.prayerType == prayerItem.prayerType) {
                                pItem.copy(
                                    submission = submission,
                                    isSelesai = newStatus == Submission.STATUS_SELESAI
                                )
                            } else pItem
                        }
                        taskItem.copy(prayerItems = updatedPrayers)
                    } else taskItem
                }
                val completed = updatedItems.sumOf { it.prayerItems.count { p -> p.isSelesai } }

                _uiState.value = _uiState.value.copy(
                    taskItems = updatedItems,
                    completedCount = completed
                )
            } catch (e: FirebaseFirestoreException) {
                // Tangkap jika ditolak oleh Firestore Security Rules (contoh: race condition lewat tengah malam)
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Waktu pengerjaan tugas ini sudah berakhir."
                )
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Gagal memperbarui tugas: ${e.localizedMessage ?: "Koneksi terputus"}"
                )
            }
        }
    }

    /**
     * Fallback bila toggleTask dipanggil tanpa argumen prayerItem
     */
    fun toggleTask(item: TaskItemUiModel) {
        val targetPrayer = item.prayerItems.firstOrNull { it.lockState == TaskLockState.ACTIVE }
            ?: item.prayerItems.firstOrNull()
        if (targetPrayer != null) {
            toggleTask(item, targetPrayer)
        }
    }
}
