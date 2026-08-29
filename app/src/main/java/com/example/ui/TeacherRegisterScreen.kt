package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.TeacherAuthViewModel

/**
 * Halaman Khusus Registrasi Guru Baru dengan Validasi Kode Aktivasi Guru.
 */
@Composable
fun TeacherRegisterScreen(
    viewModel: TeacherAuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var activationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                        .testTag("teacher_register_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Daftar Guru Baru",
                    fontSize = 19.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Avatar Pendidik
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Registrasi Pendidik",
                fontSize = 18.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Daftarkan akun pendidik menggunakan kode aktivasi resmi dari koordinator sekolah.",
                fontSize = 13.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Pesan Error / Sukses
            if (uiState.errorMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (uiState.successMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TealAccent.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        color = TealAccent,
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Form Input
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    viewModel.clearMessages()
                },
                label = { Text("Nama Lengkap Pendidik") },
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = GoldPrimary)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardElevated,
                    focusedLabelColor = GoldPrimary,
                    unfocusedContainerColor = CardSurface,
                    focusedContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_register_name_input")
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    viewModel.clearMessages()
                },
                label = { Text("Email Guru") },
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = GoldPrimary)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardElevated,
                    focusedLabelColor = GoldPrimary,
                    unfocusedContainerColor = CardSurface,
                    focusedContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_register_email_input")
            )

            Spacer(Modifier.height(12.dp))

            // Field WAJIB: Kode Aktivasi Guru
            OutlinedTextField(
                value = activationCode,
                onValueChange = { 
                    activationCode = it
                    viewModel.clearMessages()
                },
                label = { Text("Kode Aktivasi Guru (Wajib)") },
                placeholder = { Text("Diberikan oleh koordinator", fontSize = 12.sp, color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Filled.Key, contentDescription = null, tint = GoldPrimary)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardElevated,
                    focusedLabelColor = GoldPrimary,
                    unfocusedContainerColor = CardSurface,
                    focusedContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_activation_code_input")
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    viewModel.clearMessages()
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldPrimary)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Sembunyikan" else "Tampilkan",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardElevated,
                    focusedLabelColor = GoldPrimary,
                    unfocusedContainerColor = CardSurface,
                    focusedContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_register_password_input")
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    viewModel.clearMessages()
                },
                label = { Text("Konfirmasi Password") },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldPrimary)
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.registerTeacher(name, email, activationCode, password, confirmPassword, onRegisterSuccess)
                    }
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CardElevated,
                    focusedLabelColor = GoldPrimary,
                    unfocusedContainerColor = CardSurface,
                    focusedContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_register_confirm_password_input")
            )

            Spacer(Modifier.height(24.dp))

            // Tombol Submit Registrasi
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registerTeacher(name, email, activationCode, password, confirmPassword, onRegisterSuccess)
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("teacher_register_submit_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = DeepNight,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Daftar Akun Guru",
                        fontSize = 16.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = DeepNight
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("teacher_register_to_login_button")
            ) {
                Text(
                    text = "Sudah punya akun? Masuk di sini",
                    fontSize = 13.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary
                )
            }
        }
    }
}
