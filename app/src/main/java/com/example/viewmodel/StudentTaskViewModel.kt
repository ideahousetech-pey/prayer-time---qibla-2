package id.ideahousetech.prayertime_qibla.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
import java.time.ZoneId

enum class TaskLockState {
    ACTIVE,         // Hari ini: checklist aktif dan dapat diubah
    EXPIRED,        // Sudah lewat hari: nonaktif + ikon gembok "Sudah lewat hari, tidak bisa diubah"
    NOT_STARTED     // Belum waktunya: nonaktif + "Belum waktunya"
}

data class TaskItemUiModel(
    val task: RamadhanTask,
    val submission: Submission?,
    val lockState: TaskLockState,
    val isSelesai: Boolean
)

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
    private val wibZoneId = ZoneId.of("Asia/Jakarta")

    private val _uiState = MutableStateFlow(StudentTaskUiState())
    val uiState: StateFlow<StudentTaskUiState> = _uiState.asStateFlow()

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun loadData() {
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

                val subsMap = subsQuery.documents.mapNotNull {
                    val sub = it.toObject(Submission::class.java)?.copy(submissionId = it.id)
                    if (sub != null) sub.taskId to sub else null
                }.toMap()

                val todayLocalDate = LocalDate.now(wibZoneId)

                val items = tasks.map { task ->
                    val taskLocalDate = Instant.ofEpochMilli(task.taskDate.toDate().time)
                        .atZone(wibZoneId)
                        .toLocalDate()

                    val lockState = when {
                        taskLocalDate.isEqual(todayLocalDate) -> TaskLockState.ACTIVE
                        taskLocalDate.isBefore(todayLocalDate) -> TaskLockState.EXPIRED
                        else -> TaskLockState.NOT_STARTED
                    }

                    val submission = subsMap[task.taskId]
                    val isSelesai = submission?.status == Submission.STATUS_SELESAI

                    TaskItemUiModel(
                        task = task,
                        submission = submission,
                        lockState = lockState,
                        isSelesai = isSelesai
                    )
                }

                val completed = items.count { it.isSelesai }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    student = student,
                    studentClass = studentClass,
                    taskItems = items,
                    completedCount = completed,
                    totalCount = items.size
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
     * Mengubah status pengerjaan tugas (checklist aktif).
     * Jika ditolak oleh Security Rules (misalnya lewat dari taskDate), exception ditangkap
     * dan menampilkan Snackbar tanpa crash aplikasi.
     */
    fun toggleTask(item: TaskItemUiModel) {
        val currentUser = auth.currentUser ?: return
        if (item.lockState != TaskLockState.ACTIVE) {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (item.lockState == TaskLockState.EXPIRED) 
                    "Sudah lewat hari, tugas tidak dapat diubah lagi." 
                else "Belum waktunya mengerjakan tugas ini."
            )
            return
        }

        val newStatus = if (item.isSelesai) Submission.STATUS_BELUM else Submission.STATUS_SELESAI
        val submissionId = "${currentUser.uid}_${item.task.taskId}"

        viewModelScope.launch {
            try {
                val submission = Submission(
                    submissionId = submissionId,
                    studentId = currentUser.uid,
                    taskId = item.task.taskId,
                    status = newStatus,
                    updatedAt = Timestamp.now()
                )

                firestore.collection("submissions").document(submissionId).set(submission).await()

                // Perbarui state lokal secara responsif
                val updatedItems = _uiState.value.taskItems.map {
                    if (it.task.taskId == item.task.taskId) {
                        it.copy(
                            submission = submission,
                            isSelesai = newStatus == Submission.STATUS_SELESAI
                        )
                    } else it
                }
                val completed = updatedItems.count { it.isSelesai }

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
}
