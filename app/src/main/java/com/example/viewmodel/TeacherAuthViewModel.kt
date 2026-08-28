package id.ideahousetech.prayertime_qibla.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ideahousetech.prayertime_qibla.model.ramadhan.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TeacherAuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentTeacher: Teacher? = null
)

class TeacherAuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(TeacherAuthState())
    val uiState: StateFlow<TeacherAuthState> = _uiState.asStateFlow()

    init {
        checkCurrentTeacher()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun checkCurrentTeacher() {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email?.endsWith("@siswa.internal") == false) {
            viewModelScope.launch {
                try {
                    val doc = firestore.collection("teachers").document(currentUser.uid).get().await()
                    val teacher = doc.toObject(Teacher::class.java)
                    _uiState.value = _uiState.value.copy(currentTeacher = teacher)
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(currentTeacher = null)
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(currentTeacher = null)
        }
    }

    /**
     * Login Guru menggunakan Email dan Password.
     */
    fun login(
        email: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email tidak boleh kosong.")
            return
        }
        if (pass.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password tidak boleh kosong.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, pass).await()
                val uid = result.user?.uid ?: throw IllegalStateException("UID tidak ditemukan")

                val doc = firestore.collection("teachers").document(uid).get().await()
                var teacher = doc.toObject(Teacher::class.java)

                // Jika dokumen teacher belum ada (misal akun baru dibuat di console), inisialisasi default
                if (teacher == null) {
                    teacher = Teacher(
                        teacherId = uid,
                        name = result.user?.displayName ?: trimmedEmail.substringBefore("@"),
                        email = trimmedEmail,
                        role = Teacher.ROLE_GURU,
                        classIds = emptyList()
                    )
                    firestore.collection("teachers").document(uid).set(teacher).await()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTeacher = teacher,
                    errorMessage = null
                )
                onSuccess()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("password", ignoreCase = true) == true ||
                    e.message?.contains("credential", ignoreCase = true) == true ||
                    e.message?.contains("user-not-found", ignoreCase = true) == true ->
                        "Email atau password salah. Periksa kembali data Anda."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Koneksi internet bermasalah. Periksa jaringan Anda."
                    else -> "Gagal masuk: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
            }
        }
    }

    /**
     * Registrasi Akun Guru Baru.
     * CATATAN: Role selalu default "guru". Role "koordinator" hanya diatur oleh developer di Firestore Console.
     */
    fun registerTeacher(
        name: String,
        email: String,
        pass: String,
        confirmPass: String,
        onSuccess: () -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (trimmedName.length < 3) {
            _uiState.value = _uiState.value.copy(errorMessage = "Nama lengkap minimal 3 karakter.")
            return
        }
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Format email tidak valid.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password minimal 6 karakter.")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = _uiState.value.copy(errorMessage = "Konfirmasi password tidak cocok.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
                val uid = result.user?.uid ?: throw IllegalStateException("UID tidak ditemukan")

                // Role dikunci ke "guru" sesuai ketentuan ketat keamanan
                val newTeacher = Teacher(
                    teacherId = uid,
                    name = trimmedName,
                    email = trimmedEmail,
                    role = Teacher.ROLE_GURU,
                    classIds = emptyList()
                )

                firestore.collection("teachers").document(uid).set(newTeacher).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTeacher = newTeacher,
                    successMessage = "Pendaftaran guru berhasil!"
                )
                onSuccess()
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                        "Email $trimmedEmail sudah terdaftar. Silakan masuk."
                    else -> "Gagal mendaftar: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
            }
        }
    }

    /**
     * Kirim email reset password.
     */
    fun sendPasswordReset(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Masukkan email yang valid untuk reset password.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(trimmedEmail).await()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Tautan reset password telah dikirim ke $trimmedEmail."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal mengirim email reset: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                )
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        _uiState.value = TeacherAuthState()
        onSuccess()
    }
}
