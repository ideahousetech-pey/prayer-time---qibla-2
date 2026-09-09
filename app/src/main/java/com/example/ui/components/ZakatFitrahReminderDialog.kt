package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.ui.window.Dialog
import id.ideahousetech.prayertime_qibla.ui.theme.*

object ZakatReminderConstants {
    const val TITLE = "Jangan Lupa Zakat Fitrah"
    const val MESSAGE = "Ramadhan akan segera berakhir. Jangan lupa menunaikan Zakat Fitrah sebelum Shalat Idul Fitri."

    const val QURAN_REF = "QS. Al-Baqarah: 43"
    const val QURAN_LABEL = "Dalil Al-Qur'an tentang zakat ($QURAN_REF):"
    const val QURAN_TEXT = "\"Dan dirikanlah shalat, tunaikanlah zakat, dan ruku'lah beserta orang-orang yang ruku'.\""

    const val HADITH_REF = "HR. Bukhari & Muslim, dari Ibnu Umar"
    const val HADITH_LABEL = "Hadist tentang Zakat Fitrah ($HADITH_REF):"
    const val HADITH_TEXT = "\"Rasulullah SAW mewajibkan zakat fitrah dari bulan Ramadhan sebanyak satu sha' kurma atau satu sha' gandum atas setiap orang, baik hamba sahaya maupun orang merdeka, laki-laki maupun perempuan, anak-anak maupun orang dewasa dari kalangan kaum muslimin.\""

    const val BUTTON_LABEL = "Baik, Insya Allah"
}

@Composable
fun ZakatFitrahReminderDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .testTag("zakat_fitrah_reminder_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolunteerActivism,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = ZakatReminderConstants.TITLE,
                    fontSize = 18.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Main Message
                Text(
                    text = ZakatReminderConstants.MESSAGE,
                    fontSize = 14.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dalil Al-Qur'an Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardElevated,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = ZakatReminderConstants.QURAN_LABEL,
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ZakatReminderConstants.QURAN_TEXT,
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hadits Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardElevated,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = ZakatReminderConstants.HADITH_LABEL,
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ZakatReminderConstants.HADITH_TEXT,
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("zakat_reminder_confirm_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(
                        text = ZakatReminderConstants.BUTTON_LABEL,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
