package id.ideahousetech.prayertime_qibla.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.service.QiblaService
import id.ideahousetech.prayertime_qibla.ui.theme.*
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.QiblaViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun QiblaScreen(
    locationViewModel: LocationViewModel,
    qiblaViewModel: QiblaViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val azimuth by qiblaViewModel.azimuthFlow.collectAsState()
    val userLocation by locationViewModel.userLocation.collectAsState()
    val sensorAccuracy by qiblaViewModel.sensorAccuracy.collectAsState()

    var hasCalibratedOnce by remember { mutableStateOf(false) }
    var dismissBanner by remember { mutableStateOf(false) }

    LaunchedEffect(sensorAccuracy) {
        if (sensorAccuracy >= 2) {
            hasCalibratedOnce = true
        }
    }

    // Menggunakan LifecycleObserver untuk mengaktifkan sensor saat ON_RESUME dan menonaktifkannya saat ON_PAUSE
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                qiblaViewModel.startListening()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                qiblaViewModel.stopListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            qiblaViewModel.stopListening()
        }
    }

    // Mengoptimalkan kalkulasi bearing dan jarak agar tidak dijalankan pada setiap frame perubahan azimuth
    val qiblaBearing = remember(userLocation) {
        val lat = userLocation?.latitude ?: -6.175115
        val lon = userLocation?.longitude ?: 106.827157
        qiblaViewModel.calculateQiblaDirection(lat, lon)
    }

    val distanceToKabah = remember(userLocation) {
        val lat = userLocation?.latitude ?: -6.175115
        val lon = userLocation?.longitude ?: 106.827157
        calculateDistanceToKabah(lat, lon)
    }

    // Arah rotasi murni jarum kompas terhadap utara magnetik
    val bearing = qiblaBearing - azimuth

    // Fungsi helper untuk menghitung sudut terpendek (shortest path) guna mengatasi wrap-around 0/360 derajat
    fun getShortestAngle(target: Float, current: Float): Float {
        val diff = (target - current) % 360f
        val shortestDiff = when {
            diff > 180f -> diff - 360f
            diff < -180f -> diff + 360f
            else -> diff
        }
        return current + shortestDiff
    }

    var continuousBearing by remember { mutableStateOf(bearing.toFloat()) }
    var continuousAzimuth by remember { mutableStateOf(-azimuth) }

    LaunchedEffect(bearing) {
        continuousBearing = getShortestAngle(bearing.toFloat(), continuousBearing)
    }

    LaunchedEffect(azimuth) {
        continuousAzimuth = getShortestAngle(-azimuth, continuousAzimuth)
    }

    // Animasi rotasi pegas (spring) yang luwes tanpa efek memantul liar (DampingRatioNoBouncy seperti kompas minyak)
    val animatedRotation by animateFloatAsState(
        targetValue  = continuousBearing,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "compassRotation"
    )

    // Animasi rotasi arah Utara
    val animatedNorthRotation by animateFloatAsState(
        targetValue  = continuousAzimuth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "northRotation"
    )

    val displayDegrees = ((bearing.toInt() % 360) + 360) % 360

    Box(
        modifier = modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header & Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CardSurface, CircleShape)
                        .border(1.dp, DividerLine, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Menu Utama",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "KOMPAS KIBLAT",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = CinzelFont,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration warning banner
            if (sensorAccuracy < 2 && !hasCalibratedOnce && !dismissBanner) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "Kalibrasi",
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Akurasi Kompas Rendah",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Harap gerakkan ponsel membentuk pola angka 8 untuk mengkalibrasi sensor magnetik.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        IconButton(
                            onClick = { dismissBanner = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Big Degree Display
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${displayDegrees}°",
                    fontSize = 52.sp,
                    fontFamily = CinzelFont,
                    color = GoldPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "ARAH KIBLAT",
                    fontSize = 11.sp,
                    color = TextMuted,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compass Visualizer Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circle with Glow Background
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White.copy(alpha = 0.01f), CircleShape)
                        .border(1.5.dp, GoldDim, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(GoldGlow, Color.Transparent),
                                center = center,
                                radius = size.width / 2
                            ),
                            radius = size.width / 2
                        )
                    }

                    // Ticks (36 lines, 10 degrees space)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val radius = size.width / 2

                        for (i in 0 until 360 step 10) {
                            val radians = Math.toRadians((i - 90).toDouble())
                            val angleCos = cos(radians).toFloat()
                            val angleSin = sin(radians).toFloat()

                            val isCardinal = i % 90 == 0
                            val len = if (isCardinal) 12.dp.toPx() else 6.dp.toPx()
                            val thickness = if (isCardinal) 2.dp.toPx() else 1.dp.toPx()
                            val color = if (isCardinal) GoldPrimary else GoldDim.copy(alpha = 0.4f)

                            drawLine(
                                color = color,
                                start = Offset(cx + (radius - len) * angleCos, cy + (radius - len) * angleSin),
                                end = Offset(cx + radius * angleCos, cy + radius * angleSin),
                                strokeWidth = thickness
                            )
                        }
                    }

                    // Cardinal direction static labels
                    Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Text("U", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.TopCenter))
                        Text("S", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.align(Alignment.BottomCenter))
                        Text("T", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterEnd))
                        Text("B", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterStart))
                    }

                    // Rotating Needles
                    // Jarum Utara (TextSecondary triangle)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedNorthRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            val len = size.width / 2 - 25.dp.toPx()

                            val path = Path().apply {
                                moveTo(cx, cy - len)
                                lineTo(cx - 8.dp.toPx(), cy - 10.dp.toPx())
                                lineTo(cx + 8.dp.toPx(), cy - 10.dp.toPx())
                                close()
                            }
                            drawPath(path = path, color = TextSecondary)
                        }
                    }

                    // Jarum Kiblat (GoldPrimary triangle)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(animatedRotation),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2
                            val cy = size.height / 2
                            val len = size.width / 2 - 14.dp.toPx()

                            val path = Path().apply {
                                moveTo(cx, cy - len)
                                lineTo(cx - 12.dp.toPx(), cy - 10.dp.toPx())
                                lineTo(cx + 12.dp.toPx(), cy - 10.dp.toPx())
                                close()
                            }
                            drawPath(path = path, color = GoldPrimary)
                        }
                    }

                    // Center Hub Card Surface
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CardSurface, CircleShape)
                            .border(1.5.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🕋", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Information Card Grid/Detail
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerLine),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = TealAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Loxodrome Bearing",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "%.2f° N".format(qiblaBearing),
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerLine))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Jarak Ke Ka'bah",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "%,.0f KM".format(distanceToKabah),
                            fontSize = 15.sp,
                            color = GoldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardElevated, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TealAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pegang perangkat secara mendatar rata air dan jauhi benda logam magnetis untuk mendapatkan akurasi optimal.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun calculateDistanceToKabah(userLat: Double, userLon: Double): Double {
    val r = 6371.0 // Radius Bumi dalam Kilometer
    val lat1Rad = Math.toRadians(userLat)
    val lon1Rad = Math.toRadians(userLon)
    val lat2Rad = Math.toRadians(QiblaService.KABAH_LATITUDE)
    val lon2Rad = Math.toRadians(QiblaService.KABAH_LONGITUDE)

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return r * c
}
