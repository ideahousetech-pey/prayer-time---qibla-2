package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*

/**
 * OnboardingScreen dengan 3 Halaman Petunjuk Interaktif bagi Pengguna Baru.
 * Didesain menggunakan gaya visual Islamic Luxury 2026, memadukan rona emerald,
 * aksen keemasan megah, dan Glassmorphism M3.
 */
@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.03f)
    ) {

        // Main layout container childs
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR: Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage < 2) {
                    Text(
                        text = "Lewati",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCompleted() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // MIDDLE CONTENT (With sliding transition animations based on currentPage)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "onboardingSlide"
                ) { page ->
                    when (page) {
                        0 -> OnboardingPageContent(
                            icon = Icons.Default.NotificationsActive,
                            iconColor = GoldPrimary,
                            title = "PENGINGAT JADWAL SHOLAT",
                            subTitle = "Jadwal Tepat Waktu & Alarm Adzan",
                            description = "Nikmati penentuan 5 waktu sholat fardhu yang presisi berdasarkan kordinat GPS Anda secara langsung. Dilengkapi seruan adzan merdu otomatis saat waktu sholat tiba."
                        )
                        1 -> OnboardingPageContent(
                            icon = Icons.Default.MenuBook,
                            iconColor = TealAccent,
                            title = "FITUR PERLENGKAPAN IBADAH",
                            subTitle = "Membaca Al-Qur'an & Dzikir",
                            description = "Akses Al-Qur'an digital bersertifikat luring kapan pun, diiringi kumpulan doa-doa harian pilihan serta alat hitung digital Tasbih yang ringkas untuk menyempurnakan ibadah harian."
                        )
                        2 -> OnboardingPageContent(
                            icon = Icons.Default.CompassCalibration,
                            iconColor = GoldPrimary,
                            title = "PANDUAN KOMPAS KIBLAT",
                            subTitle = "Kalibrasi & Akurasi Optimal",
                            description = "Sebelum melacak Ka'bah untuk sholat, luangkan waktu menggerakan ponsel Anda membentuk pola angka 'delapan' di udara guna menetralisir gangguan magnetik sekitar demi presisi maksimal."
                        )
                    }
                }
            }

            // BOTTOM BAR: Indicators and Navigation Button Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldPrimary else DividerLine)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom buttons layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    if (currentPage > 0) {
                        Button(
                            onClick = { currentPage-- },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardSurface,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(110.dp)
                                .border(1.dp, DividerLine, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.NavigateBefore,
                                contentDescription = "Sebelumnya",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kembali", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(110.dp))
                    }

                    // Next / Start button
                    Button(
                        onClick = {
                            if (currentPage < 2) {
                                currentPage++
                            } else {
                                onCompleted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPage == 2) TealAccent else GoldPrimary,
                            contentColor = DeepNight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(if (currentPage == 2) 160.dp else 110.dp)
                    ) {
                        if (currentPage == 2) {
                            Text("Mulai Sekarang!", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selesai",
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Lanjut", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.NavigateNext,
                                contentDescription = "Lanjut",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subTitle: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon container with Glassmorphism and Gold border highlights
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(GlassBackdrop, RoundedCornerShape(36.dp))
                .border(1.5.dp, GlassBorder, RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Subtle pulse background concentric circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(iconColor.copy(alpha = 0.08f), CircleShape)
                    .border(1.dp, iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Main Title (Khas Islamic Luxury Concept)
        Text(
            text = title,
            fontSize = 11.sp,
            letterSpacing = 3.sp,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // High contrast Subtitle
        Text(
            text = subTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-line detailed explanation
        Text(
            text = description,
            fontSize = 13.sp,
            lineHeight = 22.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
