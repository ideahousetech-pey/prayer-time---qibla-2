package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.ramadhan.RamadhanClass
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.StudentProgressSummary
import id.ideahousetech.prayertime_qibla.viewmodel.TeacherDashboardViewModel

/**
 * Dashboard Pendidik Ramadhan (Mendukung peran Guru Kelas dan Koordinator Sekolah).
 */
@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherDashboardViewModel,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var newClassLabel by remember { mutableStateOf("") }
    var classToDeactivate by remember { mutableStateOf<RamadhanClass?>(null) }

    LaunchedEffect(Unit) {
        viewModel.initDashboard()
    }

    // Jika detail siswa tertentu dibuka
    if (uiState.selectedStudentDetail != null) {
        StudentProgressDetailScreen(
            student = uiState.selectedStudentDetail!!,
            taskItems = uiState.selectedStudentTasks,
            isLoading = uiState.isLoadingDetail,
            onBackClick = { viewModel.closeStudentDetail() }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                        .testTag("teacher_dashboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (uiState.isKoordinator) "Dashboard Koordinator" else "Dashboard Guru",
                        fontSize = 18.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = uiState.teacher?.name ?: "Pendidik",
                        fontSize = 12.sp,
                        fontFamily = NunitoFont,
                        color = GoldPrimary
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, WarningAmber.copy(alpha = 0.3f), CircleShape)
                        .testTag("teacher_dashboard_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Keluar",
                        tint = WarningAmber
                    )
                }
            }

            // Badge Khusus Mode Koordinator
            if (uiState.isKoordinator) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarningAmber.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SupervisorAccount,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Mode Koordinator: Memantau seluruh kelas 1A-6A (Read-Only)",
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    }
                }
            }

            // Kotak Pesan Sukses / Error
            if (uiState.classCreationMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TealAccent.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiState.classCreationMessage ?: "",
                        fontSize = 12.sp,
                        fontFamily = NunitoFont,
                        color = TealAccent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Area Kontrol: Guru vs Koordinator
            if (!uiState.isKoordinator) {
                // Peran Guru Biasa: Pemilihan & Pembuatan Kelas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kelas Saya:",
                        fontSize = 14.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    TextButton(
                        onClick = {
                            newClassLabel = ""
                            showCreateClassDialog = true
                        },
                        modifier = Modifier.testTag("create_class_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("+ Buat Kelas Baru", color = GoldPrimary, fontSize = 13.sp, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                    }
                }

                // Daftar Chip Kelas Guru
                if (uiState.classes.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.classes, key = { it.classId }) { cls ->
                            val isSelected = uiState.selectedClass?.classId == cls.classId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectClass(cls) },
                                label = { Text("Kelas ${cls.label}", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DeepNight,
                                    containerColor = CardSurface,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) GoldPrimary else CardElevated,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    // Banner Kode Kelas Aktif
                    uiState.selectedClass?.let { cls ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Kode Kelas: ${cls.classCode}",
                                        fontSize = 14.sp,
                                        fontFamily = NunitoFont,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                    Text(
                                        text = "Bagikan kode ini hanya ke siswa kelas ${cls.label} untuk pendaftaran.",
                                        fontSize = 11.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Anda belum memiliki kelas aktif. Klik '+ Buat Kelas Baru' di atas untuk memulai.",
                                fontSize = 13.sp,
                                fontFamily = NunitoFont,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                // Peran Koordinator: Filter Kelas 1A-6A & Search Bar
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    // Filter Chips Kelas
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.filterClassId == null,
                                onClick = { viewModel.setFilterClass(null) },
                                label = { Text("Semua Kelas", fontFamily = NunitoFont, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DeepNight,
                                    containerColor = CardSurface,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                        items(uiState.classes, key = { it.classId }) { cls ->
                            val isSelected = uiState.filterClassId == cls.classId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilterClass(cls.classId) },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Kelas ${cls.label}", fontFamily = NunitoFont)
                                        if (!cls.isActive) {
                                            Spacer(Modifier.width(4.dp))
                                            Text("(Nonaktif)", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = DeepNight,
                                    containerColor = CardSurface,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }

                    // Card Status Kelas & Kill-Switch untuk Koordinator jika memilih kelas tertentu
                    val filteredClass = uiState.classes.firstOrNull { it.classId == uiState.filterClassId }
                    if (filteredClass != null) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (filteredClass.isActive) CardElevated else MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Kelas ${filteredClass.label} (Kode: ${filteredClass.classCode})",
                                            fontSize = 14.sp,
                                            fontFamily = NunitoFont,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "Status: ",
                                                fontSize = 12.sp,
                                                fontFamily = NunitoFont,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = if (filteredClass.isActive) "Aktif" else "Nonaktif",
                                                fontSize = 12.sp,
                                                fontFamily = NunitoFont,
                                                fontWeight = FontWeight.Bold,
                                                color = if (filteredClass.isActive) TealAccent else MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    if (filteredClass.isActive) {
                                        Button(
                                            onClick = { classToDeactivate = filteredClass },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("deactivate_class_button_${filteredClass.classId}")
                                        ) {
                                            Text(
                                                text = "Nonaktifkan",
                                                fontSize = 12.sp,
                                                fontFamily = NunitoFont,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Catatan: Hanya nonaktifkan kelas yang benar-benar mencurigakan — siswa yang sudah terdaftar tidak akan terpengaruh, tapi guru pemilik tidak bisa lagi menerima siswa baru sampai diaktifkan kembali manual lewat Firestore Console.",
                                    fontSize = 11.sp,
                                    fontFamily = NunitoFont,
                                    color = TextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Kolom Pencarian Nama Siswa
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Cari nama siswa...", fontSize = 13.sp, color = TextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = GoldPrimary)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Hapus", tint = TextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = CardElevated,
                            unfocusedContainerColor = CardSurface,
                            focusedContainerColor = CardSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coordinator_search_input")
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Daftar Siswa & Ringkasan Progres
            Text(
                text = "Daftar Siswa (${uiState.students.size}):",
                fontSize = 14.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else if (uiState.students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada siswa yang terdaftar.",
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                ) {
                    items(uiState.students, key = { it.student.studentId }) { summary ->
                        StudentRowCard(
                            summary = summary,
                            onClick = { viewModel.openStudentDetail(summary.student) }
                        )
                    }

                    if (uiState.hasMorePages) {
                        item {
                            Button(
                                onClick = { viewModel.loadStudents(loadMore = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSurface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            ) {
                                Text("Muat Lebih Banyak Siswa", color = GoldPrimary, fontFamily = NunitoFont)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Buat Kelas Baru
    if (showCreateClassDialog) {
        AlertDialog(
            onDismissRequest = { showCreateClassDialog = false },
            title = {
                Text(
                    text = "Buat Kelas Ramadhan Baru",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan nama / label kelas (misal: 6A, 5B, 1A):",
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newClassLabel,
                        onValueChange = { newClassLabel = it.uppercase() },
                        label = { Text("Label Kelas (contoh: 6A)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createClass(newClassLabel) {
                            showCreateClassDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Buat Kelas", color = DeepNight, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateClassDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }

    // Dialog Konfirmasi Kill-Switch Koordinator
    if (classToDeactivate != null) {
        val targetClass = classToDeactivate!!
        AlertDialog(
            onDismissRequest = { classToDeactivate = null },
            title = {
                Text(
                    text = "Konfirmasi Nonaktifkan Kelas",
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin menonaktifkan Kelas ${targetClass.label} (Kode: ${targetClass.classCode})?",
                        fontSize = 14.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Hanya nonaktifkan kelas yang benar-benar mencurigakan — siswa yang sudah terdaftar tidak akan terpengaruh, tapi guru pemilik tidak bisa lagi menerima siswa baru sampai diaktifkan kembali manual lewat Firestore Console.",
                        fontSize = 12.sp,
                        fontFamily = NunitoFont,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cId = targetClass.classId
                        classToDeactivate = null
                        viewModel.deactivateClass(cId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_deactivate_class_button")
                ) {
                    Text("Nonaktifkan (Kill-Switch)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDeactivate = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }
}

@Composable
private fun StudentRowCard(
    summary: StudentProgressSummary,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardElevated, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("student_row_${summary.student.studentId}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(TealAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.student.name,
                    fontSize = 15.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Kelas ${summary.classLabel}  •  ${summary.completedCount}/${summary.eligibleDayCount} hari selesai",
                    fontSize = 12.sp,
                    fontFamily = NunitoFont,
                    color = if (summary.completedCount > 0) TealAccent else TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Detail",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
