package id.ideahousetech.prayertime_qibla.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.model.DailyInsightItem
import id.ideahousetech.prayertime_qibla.model.InsightType
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.DailyInsightUiState
import id.ideahousetech.prayertime_qibla.viewmodel.DailyInsightViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Komponen UI Utama untuk menampilkan sistem Daily Insight dengan gaya Islamic Luxury yang disederhanakan.
 * Hanya menampilkan Ayat Pilihan Harian secara default, dengan opsi ekspansi untuk memunculkan
 * Hadits Shohih dan Doa Harian bersesuaian guna mereduksi visual noise dan tinggi scroll halaman.
 */
@Composable
fun DailyInsightSection(
    viewModel: DailyInsightViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Judul Bagian Atas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "INSIGHT HARIAN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GoldDim
                )
                Text(
                    text = "Renungan Kehidupan Hari Ini",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            // Indikator status luring/offline-first
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GoldPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Offline-First",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GoldPrimary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        when (val state = uiState) {
            is DailyInsightUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary, strokeWidth = 2.dp)
                }
            }
            is DailyInsightUiState.Success -> {
                val items = state.items
                // Find main Quranic Ayat item
                val mainAyat = items.find { it.type == InsightType.AYAT } ?: items.firstOrNull()
                // Retrieve all non-ayat/secondary elements (Hadits/Doa)
                val secondaryItems = items.filter { it != mainAyat }

                if (mainAyat != null) {
                    // Main Ayat Card (Always visible, clean, compact layout)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(CornerMedium),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.22f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Card Header: Source Title & Type Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Book,
                                        contentDescription = null,
                                        tint = TealAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AYAT PILIHAN HARIAN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = TealAccent,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                Text(
                                    text = mainAyat.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Arabic scripture in beautiful Amiri Naskh
                            Text(
                                text = mainAyat.arabic,
                                fontFamily = AmiriQuranFont,
                                fontSize = 18.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Translation in clean Nunito font
                            Text(
                                text = "\"${mainAyat.translation}\"",
                                fontFamily = NunitoFont,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Reference & Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = mainAyat.reference,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GoldPrimary
                                )

                                // Action Buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { viewModel.copyInsightToClipboard(context, mainAyat) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(CardElevated, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Salin",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.shareInsight(context, mainAyat) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(CardElevated, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Bagikan",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            // Dynamic expand layout action
                            if (secondaryItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = DividerLine.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(10.dp))

                                val rotationAngle by animateFloatAsState(
                                    targetValue = if (isExpanded) 180f else 0f,
                                    animationSpec = tween(300),
                                    label = "chevron_rotation"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CardElevated.copy(alpha = 0.5f))
                                        .clickable { isExpanded = !isExpanded }
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isExpanded) "Sembunyikan Hadits & Doa" else "Tampilkan Hadits & Doa Hari Ini",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .rotate(rotationAngle)
                                    )
                                }
                            }
                        }
                    }

                    // Revealable Hadits & Doa expansion block
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(300)) + fadeOut(tween(300))
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            secondaryItems.forEach { item ->
                                val (typeName, themeColor, typeIcon) = when (item.type) {
                                    InsightType.HADITS -> Triple("HADITS SHOHIH", GoldPrimary, Icons.Outlined.ChatBubbleOutline)
                                    InsightType.DOA -> Triple("DOA HARIAN", WarningAmber, Icons.Outlined.Star)
                                    else -> Triple("INSIGHT HARIAN", GoldPrimary, Icons.Outlined.Book)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(CornerMedium),
                                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                                    border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = typeIcon,
                                                    contentDescription = null,
                                                    tint = themeColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = typeName,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = themeColor
                                                )
                                            }
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldLight,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = item.arabic,
                                            fontFamily = AmiriQuranFont,
                                            fontSize = 16.sp,
                                            color = TextPrimary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 26.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        if (!item.latin.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = item.latin,
                                                fontFamily = NunitoFont,
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "\"${item.translation}\"",
                                            fontFamily = NunitoFont,
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Rujukan: ${item.reference}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldDim
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                IconButton(
                                                    onClick = { viewModel.copyInsightToClipboard(context, item) },
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(CardElevated, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = "Salin",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { viewModel.shareInsight(context, item) },
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(CardElevated, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = "Bagikan",
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(10.dp)
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
            }
            is DailyInsightUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(CardSurface, RoundedCornerShape(CornerMedium))
                        .border(0.5.dp, ErrorRed, RoundedCornerShape(CornerMedium)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gagal memuat insight harian",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed
                    )
                }
            }
        }
    }
}
