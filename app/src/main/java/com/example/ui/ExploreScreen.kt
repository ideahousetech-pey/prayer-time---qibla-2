package id.ideahousetech.prayertime_qibla.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.ideahousetech.prayertime_qibla.AppScreen
import id.ideahousetech.prayertime_qibla.model.Mosque
import id.ideahousetech.prayertime_qibla.model.MosqueUiState
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.ExploreViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel

/**
 * ExploreScreen mendirikan sentral eksplorasi fitur ibadah sekunder.
 * Memadukan pencarian Masjid Terdekat real menggunakan Google Places API & fallback Mock yang andal.
 */
@Composable
fun ExploreScreen(
    locationViewModel: LocationViewModel,
    exploreViewModel: ExploreViewModel,
    onNavigateToScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userLocation by locationViewModel.userLocation.collectAsState()
    val locationName by locationViewModel.locationName.collectAsState()
    val isLocationLoading by locationViewModel.isLoading.collectAsState()
    val isTrackingActive by locationViewModel.isTrackingActive.collectAsState()

    val mosqueState by exploreViewModel.mosqueState.collectAsState()
    val searchRadius by exploreViewModel.searchRadius.collectAsState()

    // 1. FIX UTAMA: Trigger pencarian secara reaktif saat latitude/longitude berubah
    LaunchedEffect(userLocation?.latitude, userLocation?.longitude) {
        userLocation?.let { loc ->
            exploreViewModel.searchMosques(loc.latitude, loc.longitude)
        }
    }

    // 2. Lifecycle observer untuk start/stop tracking secara otomatis
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                locationViewModel.startLocationTracking()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                locationViewModel.stopLocationTracking()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
            // 1. HEADER EXPLORE
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "EKSPLORASI RUHANI",
                        fontSize = 11.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = GoldDim,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Perdalam ibadah dan sempurnakan adab harian Anda",
                        fontSize = 13.sp,
                        fontFamily = NunitoFont,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. GRID MENU UTAMA
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Kompas Kiblat",
                            description = "Presisi dan bersensor kompas",
                            icon = Icons.Outlined.Explore,
                            onClick = { onNavigateToScreen(AppScreen.KIBLAT) }
                        )
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Dzikir & Tasbih",
                            description = "Hitung ketukan dzikir harian",
                            icon = Icons.Outlined.Cached,
                            onClick = { onNavigateToScreen(AppScreen.TASBIH) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Kalender Hijriah",
                            description = "Penanggalan & hari besar Islam",
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = { onNavigateToScreen(AppScreen.KALENDER) }
                        )
                        ExploreGridItem(
                            modifier = Modifier.weight(1f),
                            title = "Doa & Hadits",
                            description = "Riyadhus sholihin & doa harian",
                            icon = Icons.Outlined.MenuBook,
                            onClick = { onNavigateToScreen(AppScreen.DOA) }
                        )
                    }
                }
            }

            // 3. MASJID TERDEKAT HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MASJID TERDEKAT POSISI ANDA",
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DividerLine)
                }
            }

            // Live Tracking Indicator
            item {
                LiveTrackingIndicator(isActive = isTrackingActive)
            }

            // 4. MASJID TERDEKAT CONTENT STATES
            val currentLoc = userLocation
            if (currentLoc == null) {
                // a & b. State Menunggu GPS / No GPS
                item {
                    MasjidGpsStateView(
                        isLoading = isLocationLoading,
                        locationName = locationName,
                        onRefreshLocation = { locationViewModel.refreshLocation() }
                    )
                }
            } else {
                when (val state = mosqueState) {
                    is MosqueUiState.Idle -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Mempersiapkan pencarian...", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                    is MosqueUiState.Loading -> {
                        // c. MasjidLoadingState (Placeholder / Shimmer)
                        items(3) {
                            ShimmerMosqueItem()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    is MosqueUiState.Success -> {
                        // d. MasjidSuccessState
                        item {
                            Text(
                                text = "Radius pencarian aktif: ${searchRadius / 1000} km di sekitar $locationName",
                                color = TextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        items(state.mosques) { mosque ->
                            MasjidNearbyItem(
                                mosque = mosque,
                                onClick = {
                                    openMosqueInMaps(context, mosque)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        // Opsi untuk memperlebar pencarian jika butuh area lebih luas
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { exploreViewModel.expandSearchRadius(currentLoc.latitude, currentLoc.longitude) },
                                    enabled = searchRadius < 10000,
                                    colors = ButtonDefaults.textButtonColors(contentColor = GoldLight)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Perluas Area Pencarian (+2 km)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { exploreViewModel.retry(currentLoc.latitude, currentLoc.longitude) }
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    is MosqueUiState.Empty -> {
                        // e. MasjidEmptyState
                        item {
                            MasjidEmptyStateView(
                                radiusKm = searchRadius / 1000,
                                onExpandRadius = { exploreViewModel.expandSearchRadius(currentLoc.latitude, currentLoc.longitude) },
                                onRetry = { exploreViewModel.retry(currentLoc.latitude, currentLoc.longitude) }
                            )
                        }
                    }
                    is MosqueUiState.NoInternet -> {
                        // g. MasjidNoInternetState
                        item {
                            MasjidNoInternetStateView {
                                exploreViewModel.retry(currentLoc.latitude, currentLoc.longitude)
                            }
                        }
                    }
                    is MosqueUiState.NoPermission -> {
                        item {
                            MasjidPermissionStateView {
                                locationViewModel.refreshLocation()
                            }
                        }
                    }
                    is MosqueUiState.Error -> {
                        // f. MasjidErrorState
                        item {
                            MasjidErrorStateView(
                                message = state.message,
                                onRetry = { exploreViewModel.retry(currentLoc.latitude, currentLoc.longitude) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Live Tracking Indicator dengan animasi pulsa dot hijau cerah.
 */
@Composable
fun LiveTrackingIndicator(isActive: Boolean) {
    if (!isActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF10B981).copy(alpha = 0.1f))
            .border(0.5.dp, Color(0xFF10B981).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GPS Aktif & Tracking",
                color = Color(0xFF10B981),
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ExploreGridItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IslamicGlassCard(
        modifier = modifier
            .height(115.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        cornerRadius = CornerMedium
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(CardElevated, RoundedCornerShape(8.dp))
                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontFamily = CinzelFont,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 9.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary,
                    lineHeight = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MasjidNearbyItem(
    mosque: Mosque,
    onClick: () -> Unit
) {
    IslamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CardElevated, CircleShape)
                    .border(0.5.dp, GoldPrimary.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🕌", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mosque.name,
                        fontSize = 13.sp,
                        fontFamily = CinzelFont,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    mosque.rating?.let { rating ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Rating", tint = GoldPrimary, modifier = Modifier.size(12.dp))
                            Text(
                                text = " $rating",
                                fontSize = 10.sp,
                                fontFamily = NunitoFont,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mosque.address,
                    fontSize = 10.sp,
                    fontFamily = NunitoFont,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusText = if (mosque.isOpen == true) "Buka" else if (mosque.isOpen == false) "Tutup" else "N/A"
                    val statusColor = if (mosque.isOpen == true) Color(0xFF10B981) else if (mosque.isOpen == false) ErrorRed else TextMuted
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 8.sp,
                            fontFamily = NunitoFont,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = mosque.formattedDistance,
                fontSize = 11.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ShimmerMosqueItem() {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DividerLine.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DividerLine.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DividerLine.copy(alpha = 0.15f))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DividerLine.copy(alpha = 0.2f))
            )
        }
    }
}

@Composable
fun MasjidGpsStateView(
    isLoading: Boolean,
    locationName: String,
    onRefreshLocation: () -> Unit
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📍 Mencari Lokasi GPS Anda...",
                fontSize = 13.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Status: $locationName",
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(24.dp))
            } else {
                Button(
                    onClick = onRefreshLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh Lokasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun MasjidEmptyStateView(
    radiusKm: Int,
    onExpandRadius: () -> Unit,
    onRetry: () -> Unit
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🕌 🚫", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Masjid Tidak Ditemukan",
                fontSize = 14.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tidak ada masjid atau musholla terdeteksi dalam radius $radiusKm km.",
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onExpandRadius,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Perluas Area", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                OutlinedButton(
                    onClick = onRetry,
                    border = BorderStroke(1.dp, GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                ) {
                    Text("Coba Lagi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MasjidNoInternetStateView(
    onRetry: () -> Unit
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌐 ❌", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Koneksi Offline",
                fontSize = 14.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Periksa koneksi internet Anda untuk memuat lokasi masjid real.",
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Coba Lagi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun MasjidPermissionStateView(
    onRequestPermission: () -> Unit
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔒 📍", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Izin Lokasi Diperlukan",
                fontSize = 14.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = GoldLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aktifkan GPS & izinkan akses lokasi untuk mencari masjid terdekat secara presisi.",
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Izinkan & Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun MasjidErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    IslamicGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gagal Memuat Data",
                fontSize = 14.sp,
                fontFamily = CinzelFont,
                fontWeight = FontWeight.Bold,
                color = ErrorRed,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 11.sp,
                fontFamily = NunitoFont,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Coba Lagi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

private fun openMosqueInMaps(context: android.content.Context, mosque: Mosque) {
    val query = Uri.encode(mosque.name)
    val uri = "geo:${mosque.lat},${mosque.lon}?q=$query"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
        setPackage("com.google.android.apps.maps")
    }

    val pm = context.packageManager
    if (intent.resolveActivity(pm) != null) {
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openInBrowserFallback(context, mosque)
        }
    } else {
        openInBrowserFallback(context, mosque)
    }
}

private fun openInBrowserFallback(context: android.content.Context, mosque: Mosque) {
    try {
        val webQuery = Uri.encode(mosque.name)
        val webUri = "https://www.google.com/maps/search/?api=1&query=$webQuery"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
        context.startActivity(webIntent)
    } catch (ex: Exception) {
        Log.e("ExploreScreen", "Semua intent navigasi peta gagal dijalankan", ex)
    }
}
