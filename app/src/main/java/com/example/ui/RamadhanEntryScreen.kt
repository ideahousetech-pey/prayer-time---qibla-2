package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*

/**
 * Screen Pemilihan Peran (Entry Point) Fitur Musiman Tugas Ramadhan.
 * Menghubungkan pengguna ke alur Siswa atau alur Guru/Koordinator.
 */
@Composable
fun RamadhanEntryScreen(
    onSelectStudent: () -> Unit,
    onSelectTeacher: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape)
                        .testTag("entry_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = GoldPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Tugas Ramadhan",
                    fontSize = 20.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(16.dp))

            // Decorative Banner Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
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
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Lembar Amaliah Ramadhan",
                        fontSize = 18.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Pantau dan kerjakan tugas ibadah harian Ramadhan secara terstruktur untuk siswa SD (Kelas 1A-6A).",
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Pilih Peran Anda:",
                fontSize = 15.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Tombol "Saya Siswa"
            RoleOptionCard(
                title = "Saya Siswa",
                description = "Masuk menggunakan nama dan kode kelas untuk mengerjakan tugas harian.",
                icon = Icons.Filled.Face,
                accentColor = TealAccent,
                testTag = "role_student_button",
                onClick = onSelectStudent
            )

            Spacer(Modifier.height(16.dp))

            // Tombol "Saya Guru"
            RoleOptionCard(
                title = "Saya Guru / Tenaga Pendidik",
                description = "Kelola kelas, bagikan kode aktivasi, dan pantau perkembangan tugas siswa.",
                icon = Icons.Filled.School,
                accentColor = GoldPrimary,
                testTag = "role_teacher_button",
                onClick = onSelectTeacher
            )
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
