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
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.StudentAuthViewModel

/**
 * Halaman Pendaftaran Siswa Baru Ramadhan.
 * Memerlukan Kode Kelas unik yang diberikan oleh Guru untuk aktivasi langsung.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegisterScreen(
    viewModel: StudentAuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("1A") }
    var classCode by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
                        .testTag("student_register_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Daftar Siswa Baru",
                    fontSize = 20.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Text(
                text = "Lengkapi data diri dan masukkan kode aktivasi kelas dari bapak/ibu guru.",
                fontSize = 13.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

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
                label = { Text("Nama Lengkap Siswa *") },
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
                    .testTag("register_name_input")
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
                    label = { Text("Pilih Kelas *") },
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
                        .testTag("register_class_dropdown")
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

            // Input Kode Kelas (WAJIB)
            OutlinedTextField(
                value = classCode,
                onValueChange = { 
                    classCode = it.uppercase()
                    viewModel.clearMessages()
                },
                label = { Text("Kode Kelas (WAJIB, misal: 6A-RMD26) *") },
                leadingIcon = {
                    Icon(Icons.Filled.Key, contentDescription = null, tint = GoldPrimary)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
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
                    .testTag("register_class_code_input")
            )

            Spacer(Modifier.height(14.dp))

            // Input Email Orang Tua (Opsional)
            OutlinedTextField(
                value = parentEmail,
                onValueChange = { 
                    parentEmail = it
                    viewModel.clearMessages()
                },
                label = { Text("Email orang tua (opsional, untuk reset password)") },
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = TextSecondary)
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
                    .testTag("register_parent_email_input")
            )

            Spacer(Modifier.height(14.dp))

            // Input Password
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    viewModel.clearMessages()
                },
                label = { Text("Password (minimal 6 karakter) *") },
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
                    .testTag("register_password_input")
            )

            Spacer(Modifier.height(14.dp))

            // Input Ulangi Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    viewModel.clearMessages()
                },
                label = { Text("Ulangi Password *") },
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
                        viewModel.register(
                            name = name,
                            classLabel = selectedClass,
                            classCode = classCode,
                            password = password,
                            confirmPass = confirmPassword,
                            parentEmail = parentEmail.ifBlank { null },
                            onSuccess = onRegisterSuccess
                        )
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
                    .testTag("register_confirm_password_input")
            )

            Spacer(Modifier.height(28.dp))

            // Tombol Submit Pendaftaran
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.register(
                        name = name,
                        classLabel = selectedClass,
                        classCode = classCode,
                        password = password,
                        confirmPass = confirmPassword,
                        parentEmail = parentEmail.ifBlank { null },
                        onSuccess = onRegisterSuccess
                    )
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_submit_button")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = DeepNight,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Daftar & Aktivasi",
                        fontSize = 16.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = DeepNight
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
