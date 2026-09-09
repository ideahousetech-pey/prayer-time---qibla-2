package id.ideahousetech.prayertime_qibla.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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

object RamadhanGreetingConstants {
    const val TITLE = "Selamat Menunaikan Ibadah Puasa"
    const val MESSAGE = "Kami keluarga besar SDN Kramat 08 mengucapkan Selamat Menjalankan Ibadah Puasa Ramadhan."

    const val QURAN_REF = "QS. Al-Baqarah: 183"
    const val QURAN_LABEL = "Dalil Al-Qur'an ($QURAN_REF):"
    const val QURAN_TEXT = "\"Hai orang-orang yang beriman, diwajibkan atas kamu berpuasa sebagaimana diwajibkan atas orang-orang sebelum kamu agar kamu bertakwa.\""

    const val HADITH_REF = "HR. Bukhari & Muslim"
    const val HADITH_LABEL = "Hadist ($HADITH_REF):"
    const val HADITH_TEXT = "\"Barangsiapa yang berpuasa Ramadhan karena iman dan mengharap pahala dari Allah, maka diampuni dosanya yang telah lalu.\""

    const val BUTTON_LABEL = "Aamiin"
}

@Composable
fun RamadhanGreetingDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .testTag("ramadhan_greeting_dialog"),
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
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = RamadhanGreetingConstants.TITLE,
                    fontSize = 18.sp,
                    fontFamily = NunitoFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Main Message
                Text(
                    text = RamadhanGreetingConstants.MESSAGE,
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
                            text = RamadhanGreetingConstants.QURAN_LABEL,
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = RamadhanGreetingConstants.QURAN_TEXT,
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
                            text = RamadhanGreetingConstants.HADITH_LABEL,
                            fontSize = 11.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = RamadhanGreetingConstants.HADITH_TEXT,
                            fontSize = 12.sp,
                            fontFamily = NunitoFont,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Aamiin Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ramadhan_greeting_aamiin_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(
                        text = RamadhanGreetingConstants.BUTTON_LABEL,
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
