package id.ideahousetech.prayertime_qibla.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ideahousetech.prayertime_qibla.model.ramadhan.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class StudentAuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentStudent: Student? = null,
    val isRegistered: Boolean = false
)

class StudentAuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(StudentAuthState())
    val uiState: StateFlow<StudentAuthState> = _uiState.asStateFlow()

    init {
        checkCurrentStudent()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    /**
     * Membentuk email sintetis internal untuk akun siswa (tanpa mewajibkan email asli anak SD).
     * Format: {nama_bersih}.{classId}@siswa.internal
     */
    fun createSyntheticEmail(name: String, classId: String): String {
        val sanitized = name.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        val sanitizedClass = classId.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
        return "$sanitized.$sanitizedClass@siswa.internal"
    }

    fun checkCurrentStudent() {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email?.endsWith("@siswa.internal") == true) {
            viewModelScope.launch {
                try {
                    val doc = firestore.collection("students").document(currentUser.uid).get().await()
                    val student = doc.toObject(Student::class.java)
                    _uiState.value = _uiState.value.copy(currentStudent = student)
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(currentStudent = null)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(currentStudent = null)
        }
    }

    /**
     * Login Siswa menggunakan Nama + Kelas + Password.
     */
    fun login(
        name: String,
        classId: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama siswa tidak boleh kosong.")
            return
        }
        if (classId.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Silakan pilih kelas terlebih dahulu.")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password minimal 6 karakter.")
            return
        }

        val syntheticEmail = createSyntheticEmail(trimmedName, classId)
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(syntheticEmail, password).await()
                val uid = result.user?.uid ?: throw IllegalStateException("UID tidak ditemukan")

                // Ambil data profil siswa dari Firestore
                val studentDoc = firestore.collection("students").document(uid).get().await()
                val student = studentDoc.toObject(Student::class.java)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentStudent = student,
                    errorMessage = null
                )
                onSuccess()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password", ignoreCase = true) == true ||
                    e.message?.contains("credential", ignoreCase = true) == true ||
                    e.message?.contains("user-not-found", ignoreCase = true) == true ->
                        "Nama, kelas, atau password salah. Pastikan Anda sudah terdaftar."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Koneksi internet bermasalah. Periksa jaringan Anda."
                    else -> "Gagal masuk: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
            }
        }
    }

    /**
     * Pendaftaran Siswa Baru:
     * 1. Buat user Auth dulu agar authenticated.
     * 2. Setelah authenticated, query collection 'classes' untuk validasi classCode (aktif & sesuai).
     * 3. Jika kode tidak valid: panggil currentUser?.delete() untuk membatalkan akun Auth baru.
     * 4. Jika valid: simpan dokumen 'students/{uid}'.
     */
    fun register(
        name: String,
        classLabel: String,
        classCode: String,
        password: String,
        confirmPass: String,
        parentEmail: String?,
        onSuccess: () -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedCode = classCode.trim().uppercase()

        if (trimmedName.length < 3) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama siswa minimal 3 karakter.")
            return
        }
        if (classLabel.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Silakan pilih kelas.")
            return
        }
        if (trimmedCode.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Kode kelas wajib diisi.")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password minimal 6 karakter.")
            return
        }
        if (password != confirmPass) {
            _uiState.value = _uiState.value.copy(errorMessage = "Konfirmasi password tidak cocok.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            var createdUserUid: String? = null
            try {
                // 1. Buat akun Firebase Auth DULU dengan email sintetis sehingga request.auth != null
                val syntheticEmail = createSyntheticEmail(trimmedName, classLabel)
                val authResult = auth.createUserWithEmailAndPassword(syntheticEmail, password).await()
                val user = authResult.user ?: throw IllegalStateException("Gagal membuat akun siswa")
                createdUserUid = user.uid

                // 2. SETELAH authenticated, baru query collection 'classes' untuk validasi classCode
                val classQuery = firestore.collection("classes")
                    .whereEqualTo("classCode", trimmedCode)
                    .get()
                    .await()

                if (classQuery.isEmpty) {
                    user.delete().await()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Kode kelas '$trimmedCode' tidak ditemukan. Silakan hubungi guru Anda."
                    )
                    return@launch
                }

                val classDoc = classQuery.documents.first()
                val isClassActive = classDoc.getBoolean("isActive") ?: false
                val matchedClassLabel = classDoc.getString("label") ?: ""
                val resolvedClassId = classDoc.id

                if (!isClassActive) {
                    user.delete().await()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Kelas dengan kode '$trimmedCode' sedang tidak aktif."
                    )
                    return@launch
                }

                if (!matchedClassLabel.equals(classLabel.trim(), ignoreCase = true)) {
                    user.delete().await()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Kode kelas '$trimmedCode' tidak cocok untuk kelas $classLabel (terdaftar untuk kelas $matchedClassLabel)."
                    )
                    return@launch
                }

                // 3. Jika kode valid, buat dokumen siswa di Firestore 'students/{studentId}'
                val student = Student(
                    studentId = user.uid,
                    name = trimmedName,
                    nameLower = trimmedName.lowercase(),
                    classId = resolvedClassId,
                    createdAt = Timestamp.now()
                )

                firestore.collection("students").document(user.uid).set(student).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRegistered = true,
                    successMessage = "Pendaftaran berhasil! Silakan masuk dengan nama & kelas Anda."
                )
                onSuccess()
            } catch (e: Exception) {
                // Rollback user Auth jika sempat terbuat tapi terjadi error sebelum tersimpan
                if (createdUserUid != null && auth.currentUser?.uid == createdUserUid) {
                    try {
                        auth.currentUser?.delete()?.await()
                    } catch (_: Exception) {}
                }

                val msg = when {
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                        "Nama siswa '$trimmedName' di kelas $classLabel sudah terdaftar. Silakan login."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Gagal terhubung ke server. Periksa jaringan internet Anda."
                    else -> "Gagal mendaftar: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        _uiState.value = StudentAuthState()
        onSuccess()
    }
}
