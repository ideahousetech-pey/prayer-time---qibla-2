package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import id.ideahousetech.prayertime_qibla.viewmodel.StudentAuthViewModel

/**
 * Halaman Masuk Siswa.
 * Menggunakan Nama Siswa + Pilihan Kelas + Password.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoginScreen(
    viewModel: StudentAuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("1A") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val classOptions = listOf(
        "1A", "1B", "2A", "2B", "3A", "3B", 
        "4A", "4B", "5A", "5B", "6A", "6B"
    )

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
                    .padding(top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                        .testTag("student_login_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Masuk Siswa",
                    fontSize = 20.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Icon Avatar Siswa
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(TealAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Selamat Datang, Siswa Hebat!",
                fontSize = 18.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Masukkan nama lengkap dan kelas untuk membuka lembar tugas harian Anda.",
                fontSize = 13.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Pesan Error jika ada
            if (uiState.errorMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
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

            // Input Nama Siswa
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    viewModel.clearMessages()
                },
                label = { Text("Nama Lengkap Siswa") },
                leadingIcon = {
                    Icon(Icons.Filled.Face, contentDescription = null, tint = GoldPrimary)
                },
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
                    .testTag("student_name_input")
            )

            Spacer(Modifier.height(14.dp))

            // Exposed Dropdown Kelas
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "Kelas $selectedClass",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilih Kelas") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CardElevated,
                        focusedLabelColor = GoldPrimary,
                        unfocusedContainerColor = CardSurface,
                        focusedContainerColor = CardSurface
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("student_class_dropdown")
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    classOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text("Kelas $option", fontFamily = NunitoFont) },
                            onClick = {
                                selectedClass = option
                                isDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Input Password
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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.login(name, selectedClass, password, onLoginSuccess)
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
                    .testTag("student_password_input")
            )

            Spacer(Modifier.height(28.dp))

            // Tombol Login
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login(name, selectedClass, password, onLoginSuccess)
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("student_login_submit_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = DeepNight,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Masuk",
                        fontSize = 16.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = DeepNight
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tombol Belum Punya Akun
            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.testTag("student_go_to_register_button")
            ) {
                Text(
                    text = "Belum punya akun? Daftar Siswa Baru di sini",
                    fontSize = 13.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary
                )
            }
        }
    }
}
