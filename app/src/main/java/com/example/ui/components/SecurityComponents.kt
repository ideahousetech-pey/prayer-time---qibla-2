package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import id.ideahousetech.prayertime_qibla.utils.AppSecurityManager
import id.ideahousetech.prayertime_qibla.utils.AppSecurityManager.SecurityLevel

/**
 * Dialog Peringatan Keamanan ("Gentle Security Warning Dialog").
 * Menampilkan saran/peringatan sekali saja kepada pengguna apabila perangkat di-root
 * atau APK terdeteksi dimodifikasi secara tidak sah, tanpa memblokir akses ibadah mereka.
 */
@Composable
fun SecurityWarningDialog(
    securityLevel: SecurityLevel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0C241B) // StaticCardSurface dari palette luxury
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Color(0xFFE5C158).copy(alpha = 0.5f) // StaticGoldPrimary translucent border
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                val headerIcon = when (securityLevel) {
                    SecurityLevel.COMPROMISED -> Icons.Filled.GppBad
                    else -> Icons.Filled.Warning
                }
                val iconColor = when (securityLevel) {
                    SecurityLevel.COMPROMISED -> Color(0xFFE63946) // Red warning
                    else -> Color(0xFFE5C158) // Gold primary warning
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = "Ikon Peringatan Keamanan",
                        modifier = Modifier.size(36.dp),
                        tint = iconColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                val titleText = when (securityLevel) {
                    SecurityLevel.COMPROMISED -> "Deteksi Modifikasi APK"
                    else -> "Notifikasi Keamanan Sistem"
                }
                Text(
                    text = titleText,
                    color = Color(0xFFF5FCF8), // StaticTextPrimary
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message Body
                val messageText = when (securityLevel) {
                    SecurityLevel.COMPROMISED -> {
                        "Peringatan! Aplikasi Anda terdeteksi telah dimodifikasi secara tidak sah (tanda tangan digital tidak valid). " +
                                "Demi keamanan Anda, silakan unduh versi resmi aplikasi di toko aplikasi resmi untuk menghindari risiko malware atau pencurian data."
                    }
                    else -> {
                        "Kami mendeteksi perangkat Anda saat ini memiliki akses Root (Modifikasi Sistem).\n\n" +
                                "Aplikasi ibadah ini tetap dapat Anda gunakan sepenuhnya secara aman. Namun, demi menjaga keamanan data Anda, mohon hindari memberikan izin root kepada aplikasi asing yang mencurigakan."
                    }
                }
                Text(
                    text = messageText,
                    color = Color(0xFFA5C5B5), // StaticTextSecondary
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5C158), // StaticGoldPrimary
                        contentColor = Color(0xFF030A07) // StaticDeepNight
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Saya Mengerti & Lanjutkan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Lencana Status Keamanan ("Security Status Badge").
 * Ditampilkan di SettingsScreen untuk menginformasikan level keamanan kepada user secara transparan.
 */
@Composable
fun SecurityStatusBadge(
    level: SecurityLevel,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor, icon) = when (level) {
        SecurityLevel.HIGH -> Quadruple(
            "Sistem Terlindungi",
            Color(0x1A2EC4B6), // Light turquoise translucent
            Color(0xFF2EC4B6), // Turquoise accent
            Icons.Filled.CheckCircle
        )
        SecurityLevel.MEDIUM -> Quadruple(
            "Debug/Sandbox Aktif",
            Color(0x1A537A68),
            Color(0xFFA5C5B5),
            Icons.Filled.BugReport
        )
        SecurityLevel.LOW -> Quadruple(
            "Perangkat Di-root",
            Color(0x1AE5C158),
            Color(0xFFE5C158),
            Icons.Filled.Warning
        )
        SecurityLevel.COMPROMISED -> Quadruple(
            "Integritas Rusak",
            Color(0x1AE63946),
            Color(0xFFE63946),
            Icons.Filled.GppBad
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper quadruple data holder
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
