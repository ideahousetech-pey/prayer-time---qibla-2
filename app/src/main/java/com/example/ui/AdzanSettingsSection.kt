package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.AdzanSettingsViewModel
import id.ideahousetech.prayertime_qibla.service.PreviewState

data class AdzanChoice(
    val id: String,
    val displayName: String,
    val fileName: String,
    val description: String
)

val adzanChoices = listOf(
    AdzanChoice("makkah", "Adzan Makkah", "adzan.mp3", "Masjidil Haram"),
    AdzanChoice("madinah", "Adzan Madinah", "adzan_fajr.mp3", "Masjid Nabawi")
)

@Composable
fun AdzanSettingsSection(
    viewModel: AdzanSettingsViewModel,
    modifier: Modifier = Modifier
) {
    val enableAdzanAlarm by viewModel.enableAdzanAlarm.collectAsState()
    val adzanSubuhSound by viewModel.adzanSubuhSound.collectAsState()
    val adzanDhuhrSound by viewModel.adzanDhuhrSound.collectAsState()
    val adzanAsrSound by viewModel.adzanAsrSound.collectAsState()
    val adzanMaghribSound by viewModel.adzanMaghribSound.collectAsState()
    val adzanIshaSound by viewModel.adzanIshaSound.collectAsState()
    val adzanVolume by viewModel.adzanVolume.collectAsState()
    val enablePreReminder by viewModel.enablePreReminder.collectAsState()
    val preReminderMinutes by viewModel.preReminderMinutes.collectAsState()
    val activeTestPrayer by viewModel.activeTestPrayer.collectAsState()
    val previewState by viewModel.previewState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsSectionHeader(title = "PENGATURAN SUARA ADZAN", icon = Icons.Outlined.MusicNote)

        IslamicGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 1. Switch Aktifkan Alarm Adzan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aktifkan Alarm Adzan",
                            fontSize = 12.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (enableAdzanAlarm) "Suara berkumandang saat masuk waktu sholat" else "Notifikasi sunyi (silent) tanpa suara adzan",
                            fontSize = 9.sp,
                            fontFamily = NunitoFont,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = enableAdzanAlarm,
                        onCheckedChange = { viewModel.updateEnableAdzanAlarm(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = TealDim,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CardElevated,
                            uncheckedBorderColor = DividerLine
                        )
                    )
                }

                if (enableAdzanAlarm) {
                    HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                    // 2. Volume Adzan
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Volume Adzan",
                                fontSize = 11.sp,
                                fontFamily = CinzelFont,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "$adzanVolume%",
                                fontSize = 11.sp,
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeMute,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Slider(
                                value = adzanVolume.toFloat(),
                                onValueChange = { viewModel.updateAdzanVolume(it.toInt()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary,
                                    inactiveTrackColor = DividerLine
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                    // 3. Pilihan Adzan Per Waktu Sholat
                    Text(
                        text = "Suara Adzan Per Waktu Sholat",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )

                    val prayers = listOf(
                        Triple("Subuh", adzanSubuhSound) { s: String -> viewModel.updateAdzanSubuhSound(s) },
                        Triple("Dzuhur", adzanDhuhrSound) { s: String -> viewModel.updateAdzanDhuhrSound(s) },
                        Triple("Ashar", adzanAsrSound) { s: String -> viewModel.updateAdzanAsrSound(s) },
                        Triple("Maghrib", adzanMaghribSound) { s: String -> viewModel.updateAdzanMaghribSound(s) },
                        Triple("Isya", adzanIshaSound) { s: String -> viewModel.updateAdzanIshaSound(s) }
                    )

                    prayers.forEach { (name, currentSound, onSoundSelected) ->
                        PrayerSoundSelectorRow(
                            prayerName = name,
                            selectedSoundId = currentSound,
                            onSoundSelected = onSoundSelected,
                            isActiveTest = activeTestPrayer == name,
                            previewState = previewState,
                            onTestClicked = { viewModel.testAdzan(name) }
                        )
                    }
                }

                HorizontalDivider(color = DividerLine, thickness = 0.5.dp)

                // 4. Pengingat Sebelum Sholat (Pre-reminder)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aktifkan Pengingat Sebelum Sholat",
                            fontSize = 12.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Dapatkan pemberitahuan persiapan sebelum masuk waktu sholat",
                            fontSize = 9.sp,
                            fontFamily = NunitoFont,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = enablePreReminder,
                        onCheckedChange = { viewModel.updateEnablePreReminder(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldPrimary,
                            checkedTrackColor = TealDim,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CardElevated,
                            uncheckedBorderColor = DividerLine
                        )
                    )
                }

                if (enablePreReminder) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Waktu Pengingat",
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )

                        var expandedMinutes by remember { mutableStateOf(false) }
                        val minuteOptions = listOf(5, 10, 15)

                        Box {
                            Button(
                                onClick = { expandedMinutes = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CardElevated,
                                    contentColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$preReminderMinutes Menit",
                                        fontSize = 11.sp,
                                        fontFamily = NunitoFont,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = expandedMinutes,
                                onDismissRequest = { expandedMinutes = false },
                                modifier = Modifier.background(CardSurface)
                            ) {
                                minuteOptions.forEach { mins ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "$mins Menit Sebelum",
                                                fontFamily = NunitoFont,
                                                fontSize = 12.sp,
                                                color = if (mins == preReminderMinutes) GoldPrimary else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            viewModel.updatePreReminderMinutes(mins)
                                            expandedMinutes = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerSoundSelectorRow(
    prayerName: String,
    selectedSoundId: String,
    onSoundSelected: (String) -> Unit,
    isActiveTest: Boolean,
    previewState: PreviewState,
    onTestClicked: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = prayerName,
            fontSize = 12.sp,
            fontFamily = NunitoFont,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.width(60.dp)
        )

        // Dropdown Pilihan Adzan
        Box(modifier = Modifier.weight(1f)) {
            val selectedChoice = adzanChoices.find { it.id == selectedSoundId } ?: adzanChoices.first()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardElevated)
                    .clickable { expandedDropdown = true }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(
                        text = selectedChoice.displayName,
                        fontSize = 11.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = selectedChoice.description,
                        fontSize = 8.sp,
                        fontFamily = NunitoFont,
                        color = TextMuted
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier.background(CardSurface)
            ) {
                adzanChoices.forEach { choice ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = choice.displayName,
                                    fontFamily = NunitoFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (choice.id == selectedSoundId) GoldPrimary else TextPrimary
                                )
                                Text(
                                    text = choice.description,
                                    fontFamily = NunitoFont,
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                        },
                        onClick = {
                            onSoundSelected(choice.id)
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // Tombol Test ▶ / ⏹
        Button(
            onClick = onTestClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isActiveTest) WarningAmber.copy(alpha = 0.2f) else TealAccent.copy(alpha = 0.15f),
                contentColor = if (isActiveTest) WarningAmber else TealAccent
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .width(75.dp)
                .height(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isActiveTest && previewState is PreviewState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = WarningAmber
                    )
                    Text(text = "Loading", fontSize = 10.sp, fontFamily = NunitoFont)
                } else if (isActiveTest) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(12.dp)
                    )
                    Text(text = "Stop", fontSize = 10.sp, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test",
                        modifier = Modifier.size(12.dp)
                    )
                    Text(text = "Test", fontSize = 10.sp, fontFamily = NunitoFont, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
