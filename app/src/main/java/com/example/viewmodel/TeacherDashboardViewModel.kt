package id.ideahousetech.prayertime_qibla.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import id.ideahousetech.prayertime_qibla.model.ramadhan.RamadhanClass
import id.ideahousetech.prayertime_qibla.model.ramadhan.RamadhanTask
import id.ideahousetech.prayertime_qibla.model.ramadhan.Student
import id.ideahousetech.prayertime_qibla.model.ramadhan.Submission
import id.ideahousetech.prayertime_qibla.model.ramadhan.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StudentProgressSummary(
    val student: Student,
    val classLabel: String,
    val completedCount: Int,
    val eligibleDayCount: Int
)

data class TeacherDashboardUiState(
    val isLoading: Boolean = false,
    val teacher: Teacher? = null,
    val isKoordinator: Boolean = false,
    val classes: List<RamadhanClass> = emptyList(),
    val selectedClass: RamadhanClass? = null,
    val filterClassId: String? = null, // null = Semua Kelas (khusus koordinator)
    val searchQuery: String = "",
    val students: List<StudentProgressSummary> = emptyList(),
    val isCreatingClass: Boolean = false,
    val classCreationMessage: String? = null,
    val errorMessage: String? = null,
    val lastVisibleDoc: DocumentSnapshot? = null,
    val hasMorePages: Boolean = false,
    // Detail Siswa Terpilih
    val selectedStudentDetail: Student? = null,
    val selectedStudentTasks: List<TaskItemUiModel> = emptyList(),
    val isLoadingDetail: Boolean = false
)

class TeacherDashboardViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val wibZoneId = ZoneId.of("Asia/Jakarta")

    private val _uiState = MutableStateFlow(TeacherDashboardUiState())
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    private var allTasksCache: List<RamadhanTask> = emptyList()

    fun initDashboard() {
        val currentUser = auth.currentUser ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val teacher = try {
                val teacherDoc = firestore.collection("teachers").document(currentUser.uid).get().await()
                teacherDoc.toObject(Teacher::class.java) ?: Teacher(
                    teacherId = currentUser.uid,
                    name = currentUser.displayName ?: "Guru",
                    email = currentUser.email ?: "",
                    role = Teacher.ROLE_GURU
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "[Tahap 1/3] Gagal memuat profil guru: ${e.localizedMessage}"
                )
                return@launch
            }
            val isKoordinator = teacher.isKoordinator()

            try {
                val tasksSnap = firestore.collection("tasks")
                    .whereEqualTo("classId", null)
                    .orderBy("dayIndex")
                    .get()
                    .await()
                allTasksCache = tasksSnap.documents.mapNotNull {
                    it.toObject(RamadhanTask::class.java)?.copy(taskId = it.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "[Tahap 2/3] Gagal memuat daftar tugas: ${e.localizedMessage}"
                )
                return@launch
            }

            val classesList = try {
                if (isKoordinator) {
                    val cSnap = firestore.collection("classes").get().await()
                    cSnap.documents.mapNotNull { it.toObject(RamadhanClass::class.java)?.copy(classId = it.id) }
                } else {
                    val cSnap = firestore.collection("classes")
                        .whereEqualTo("teacherId", currentUser.uid)
                        .get()
                        .await()
                    cSnap.documents.mapNotNull { it.toObject(RamadhanClass::class.java)?.copy(classId = it.id) }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "[Tahap 3/3] Gagal memuat daftar kelas: ${e.localizedMessage}"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                teacher = teacher,
                isKoordinator = isKoordinator,
                classes = classesList,
                selectedClass = classesList.firstOrNull()
            )

            loadStudents()
        }
    }

    fun selectClass(ramadhanClass: RamadhanClass?) {
        _uiState.value = _uiState.value.copy(
            selectedClass = ramadhanClass,
            filterClassId = ramadhanClass?.classId
        )
        loadStudents()
    }

    fun setFilterClass(classId: String?) {
        _uiState.value = _uiState.value.copy(filterClassId = classId)
        loadStudents()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadStudents()
    }

    fun loadStudents(loadMore: Boolean = false) {
        val state = _uiState.value
        val isKoordinator = state.isKoordinator
        val filterClassId = state.filterClassId
        val query = state.searchQuery.trim().lowercase()

        viewModelScope.launch {
            try {
                var queryRef: Query = firestore.collection("students")

                if (!isKoordinator) {
                    // Guru biasa: Wajib filter kelas milik guru
                    val targetClassId = state.selectedClass?.classId ?: return@launch
                    queryRef = queryRef.whereEqualTo("classId", targetClassId)
                } else {
                    // Koordinator: Opsional filter kelas
                    if (!filterClassId.isNullOrEmpty()) {
                        queryRef = queryRef.whereEqualTo("classId", filterClassId)
                    }
                }

                if (query.isNotEmpty()) {
                    queryRef = queryRef.orderBy("nameLower")
                        .startAt(query)
                        .endAt(query + "\uf8ff")
                } else {
                    queryRef = queryRef.orderBy("nameLower").limit(20)
                    if (loadMore && state.lastVisibleDoc != null) {
                        queryRef = queryRef.startAfter(state.lastVisibleDoc)
                    }
                }

                val snap = queryRef.get().await()
                val lastDoc = if (snap.documents.isNotEmpty()) snap.documents.last() else null

                val studentsList = snap.documents.mapNotNull { doc ->
                    doc.toObject(Student::class.java)?.copy(studentId = doc.id)
                }

                // Ambil kelas map untuk label
                val classMap = state.classes.associateBy { it.classId }

                // Hitung progres masing-masing siswa
                val today = LocalDate.now(wibZoneId)
                val eligibleDaysCount = allTasksCache.count { task ->
                    val taskLocalDate = Instant.ofEpochMilli(task.taskDate.toDate().time)
                        .atZone(wibZoneId)
                        .toLocalDate()
                    !taskLocalDate.isAfter(today)
                }.coerceAtLeast(1)

                val progressSummaries = studentsList.map { student ->
                    // Ambil pengumpulan tugas siswa
                    val subSnap = firestore.collection("submissions")
                        .whereEqualTo("studentId", student.studentId)
                        .whereEqualTo("status", Submission.STATUS_SELESAI)
                        .get()
                        .await()

                    val finishedCount = subSnap.size()
                    val classObj = classMap[student.classId]
                    val classLabel = classObj?.label ?: "-"

                    StudentProgressSummary(
                        student = student,
                        classLabel = classLabel,
                        completedCount = finishedCount,
                        eligibleDayCount = eligibleDaysCount
                    )
                }

                val combined = if (loadMore) state.students + progressSummaries else progressSummaries

                _uiState.value = _uiState.value.copy(
                    students = combined,
                    lastVisibleDoc = lastDoc,
                    hasMorePages = snap.documents.size >= 20
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal memuat siswa: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                )
            }
        }
    }

    /**
     * Membuat Kelas Baru oleh Guru (contoh: "6A")
     * Format kode aktivasi unik: "{label}-KRAMAT08" (misal "6A-KRAMAT08")
     */
    fun createClass(label: String, onSuccess: () -> Unit) {
        val trimmedLabel = label.trim().uppercase()
        if (trimmedLabel.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama label kelas tidak boleh kosong.")
            return
        }

        val currentUser = auth.currentUser ?: return
        val generatedClassCode = "$trimmedLabel-KRAMAT08"

        _uiState.value = _uiState.value.copy(isCreatingClass = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Cek dulu apakah classCode ini sudah dipakai kelas lain
                val existingQuery = firestore.collection("classes")
                    .whereEqualTo("classCode", generatedClassCode)
                    .get()
                    .await()

                if (!existingQuery.isEmpty) {
                    _uiState.value = _uiState.value.copy(
                        isCreatingClass = false,
                        errorMessage = "Kelas dengan label '$trimmedLabel' sudah terdaftar " +
                            "(mungkin oleh guru lain). Gunakan label berbeda, atau hubungi koordinator " +
                            "jika ini seharusnya kelas Anda."
                    )
                    return@launch
                }

                val newClassDoc = firestore.collection("classes").document()
                val ramadhanClass = RamadhanClass(
                    classId = newClassDoc.id,
                    label = trimmedLabel,
                    teacherId = currentUser.uid,
                    classCode = generatedClassCode,
                    isActive = true,
                    createdAt = Timestamp.now()
                )

                newClassDoc.set(ramadhanClass).await()

                // Perbarui daftar classIds milik teacher
                firestore.collection("teachers").document(currentUser.uid)
                    .update("classIds", FieldValue.arrayUnion(newClassDoc.id))
                    .await()

                _uiState.value = _uiState.value.copy(
                    isCreatingClass = false,
                    classCreationMessage = "Kelas $trimmedLabel berhasil dibuat dengan kode: $generatedClassCode",
                    classes = _uiState.value.classes + ramadhanClass,
                    selectedClass = ramadhanClass
                )
                onSuccess()
                loadStudents()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingClass = false,
                    errorMessage = "Gagal membuat kelas: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                )
            }
        }
    }

    /**
     * Membuka detail riwayat tugas 30 hari milik siswa tertentu (Read-Only).
     */
    fun openStudentDetail(student: Student) {
        _uiState.value = _uiState.value.copy(
            selectedStudentDetail = student,
            isLoadingDetail = true
        )

        viewModelScope.launch {
            try {
                val subSnap = firestore.collection("submissions")
                    .whereEqualTo("studentId", student.studentId)
                    .get()
                    .await()

                val subsMap = subSnap.documents.mapNotNull {
                    val s = it.toObject(Submission::class.java)
                    if (s != null) s.taskId to s else null
                }.toMap()

                val today = LocalDate.now(wibZoneId)
                val detailItems = allTasksCache.map { task ->
                    val taskLocalDate = Instant.ofEpochMilli(task.taskDate.toDate().time)
                        .atZone(wibZoneId)
                        .toLocalDate()

                    val lockState = when {
                        taskLocalDate.isEqual(today) -> TaskLockState.ACTIVE
                        taskLocalDate.isBefore(today) -> TaskLockState.EXPIRED
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

                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    selectedStudentTasks = detailItems
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    errorMessage = "Gagal memuat rincian siswa: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Kill-Switch Koordinator: Menonaktifkan kelas (isActive = false).
     * Siswa yang sudah terdaftar tidak terpengaruh, tapi guru pemilik tidak bisa lagi menerima pendaftaran siswa baru.
     */
    fun deactivateClass(classId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                firestore.collection("classes").document(classId)
                    .update("active", false)
                    .await()

                val updatedClasses = _uiState.value.classes.map { cls ->
                    if (cls.classId == classId) cls.copy(isActive = false) else cls
                }

                val updatedSelected = if (_uiState.value.selectedClass?.classId == classId) {
                    _uiState.value.selectedClass?.copy(isActive = false)
                } else {
                    _uiState.value.selectedClass
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    classes = updatedClasses,
                    selectedClass = updatedSelected,
                    classCreationMessage = "Kelas berhasil dinonaktifkan."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal menonaktifkan kelas: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            classCreationMessage = null
        )
    }

    fun closeStudentDetail() {
        _uiState.value = _uiState.value.copy(
            selectedStudentDetail = null,
            selectedStudentTasks = emptyList()
        )
    }
}
