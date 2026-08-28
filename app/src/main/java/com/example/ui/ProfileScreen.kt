package id.ideahousetech.prayertime_qibla.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys

/**
 * ProfileScreen mendirikan pusat pengaturan terpadu (Profile Tab 5).
 * Mengintegrasikan theme toggling, alarm preference binding, settings, sync, dan premium card.
 */
@Composable
fun ProfileScreen(
    onOpenSettingsDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs.get(context) }

    // Interaktif tema
    val currentTheme = AppThemeState.currentThemeMode.value
    val scope = rememberCoroutineScope()

    // Alarm Adzan preference binding
    var enableAlarm by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true)) }
    var enableDailyReminder by remember { mutableStateOf(prefs.getBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, true)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. PROFILE HEADER
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GoldGlow)
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 36.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "HAMBA ALLAH",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GoldPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Umat Rasulullah SAW",
                        fontFamily = NunitoFont,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldGlow)
                            .border(0.5.dp, GoldPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PREMIUM MEMBER",
                            fontSize = 9.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 2. THEME SELECTOR CARD
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileSectionHeader(title = "TEMA VISUAL ASTRONOMI", icon = Icons.Outlined.LightMode)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf("dark" to "Obsidian Gelap", "light" to "Alabaster Terang")
                            options.forEach { (mode, label) ->
                                val isActive = currentTheme == mode
                                Button(
                                    onClick = {
                                        prefs.edit().putString(PrefsKeys.APP_THEME_MODE, mode).apply()
                                        AppThemeState.currentThemeMode.value = mode
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .border(
                                            width = if (isActive) 1.dp else 0.dp,
                                            color = if (isActive) GoldPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) CardElevated else Color.Transparent,
                                        contentColor = if (isActive) GoldPrimary else TextSecondary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontFamily = NunitoFont,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. NOTIFICATION SETTINGS CARD
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileSectionHeader(title = "ALARM & PENGINGAT HARIAN", icon = Icons.Outlined.NotificationsActive)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Row Alarm Adzan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Alarm Adzan 5 Waktu",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Putar suara adzan otomatis saat waktu sholat masuk",
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = enableAlarm,
                                    onCheckedChange = { checked ->
                                        enableAlarm = checked
                                        prefs.edit().putBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, checked).apply()
                                        Toast.makeText(context, if (checked) "Alarm Adzan diaktifkan" else "Alarm Adzan dimatikan", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldPrimary,
                                        checkedTrackColor = TealDim,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color.Transparent
                                    )
                                )
                            }

                            HorizontalDivider(color = DividerLine)

                            // Row Hikmah Note
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pesan Hikmah & Tidings",
                                        fontSize = 12.sp,
                                        fontFamily = CinzelFont,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tampilkan ayat / pengingat harian kalbu di beranda",
                                        fontSize = 9.sp,
                                        fontFamily = NunitoFont,
                                        color = TextSecondary
                                    )
                                }
                                Switch(
                                    checked = enableDailyReminder,
                                    onCheckedChange = { checked ->
                                        enableDailyReminder = checked
                                        prefs.edit().putBoolean(PrefsKeys.ENABLE_DAILY_REMINDER, checked).apply()
                                        Toast.makeText(context, if (checked) "Pengingat diaktifkan" else "Pengingat dimatikan", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = GoldPrimary,
                                        checkedTrackColor = TealDim,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. CONFIGURATION PORTAL
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileSectionHeader(title = "KONFIGURASI PRAYER ENGINE", icon = Icons.Outlined.Settings)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = onOpenSettingsDialog
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CardElevated, RoundedCornerShape(10.dp))
                                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lokasi, Selisih & Nada Adzan",
                                    fontSize = 13.sp,
                                    fontFamily = CinzelFont,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Text(
                                    text = "Kalibrasi waktu manual, unggah MP3 adzan kustom",
                                    fontSize = 9.sp,
                                    fontFamily = NunitoFont,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 5. CLOUD SYNC SETTINGS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileSectionHeader(title = "CADANGAN & SINKRONISASI", icon = Icons.Outlined.CloudQueue)
                    Spacer(Modifier.height(8.dp))
                    IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sinkronisasi Awan Otomatis",
                                    fontSize = 12.sp,
                                    fontFamily = CinzelFont,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Cadangkan streak, target pencapaian & catatan takzim",
                                    fontSize = 9.sp,
                                    fontFamily = NunitoFont,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Sinkron terakhir: Hari ini, 12:45 UTC",
                                    fontSize = 8.sp,
                                    fontFamily = NunitoFont,
                                    fontWeight = FontWeight.Bold,
                                    color = TealAccent
                                )
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Mencadangkan pencapaian harian ke awan...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CardElevated,
                                    contentColor = GoldPrimary
                                ),
                                border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Sync Now", fontSize = 10.sp, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6. PREMIUM CORNER
            item {
                IslamicGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    useGlow = true
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "👑 KEUNTUNGAN AKTIF PREMIUM SECURE",
                            fontSize = 11.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        listOf(
                            "Tanpa Iklan & Tanpa Noise Bebas Khusyu",
                            "Akses Full Server Suara Adzan Makkah & Madinah",
                            "Pencadangan Unlimited Riwayat Istiqomah",
                            "Akurasi Kompas Navigasi Gyro-Stabilized"
                        ).forEach { perk ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TealAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = perk,
                                    fontSize = 11.sp,
                                    fontFamily = NunitoFont,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 7. ABOUT CARD
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Waktu Sholat & Qiblah 2026",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = GoldDim
                    )
                    Text(
                        text = "Versi 1.1.2 Build PRO (Premium Licensed)",
                        fontFamily = NunitoFont,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "© 2026 IdeaHouse Tech. All Rights Reserved.",
                        fontFamily = NunitoFont,
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontFamily = CinzelFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = GoldPrimary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        HorizontalDivider(color = DividerLine, modifier = Modifier.weight(1f))
    }
}
