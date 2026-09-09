package id.ideahousetech.prayertime_qibla

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import id.ideahousetech.prayertime_qibla.ui.theme.TextSecondary
import id.ideahousetech.prayertime_qibla.ui.theme.WarningAmber
import id.ideahousetech.prayertime_qibla.ui.CalendarScreen
import id.ideahousetech.prayertime_qibla.ui.DoaScreen
import id.ideahousetech.prayertime_qibla.ui.HomeScreen
import id.ideahousetech.prayertime_qibla.ui.MonthlyScheduleScreen
import id.ideahousetech.prayertime_qibla.ui.QiblaScreen
import id.ideahousetech.prayertime_qibla.ui.DailyScheduleScreen
import id.ideahousetech.prayertime_qibla.ui.QuranScreen
import id.ideahousetech.prayertime_qibla.ui.TasbihScreen
import id.ideahousetech.prayertime_qibla.ui.theme.MyApplicationTheme
import id.ideahousetech.prayertime_qibla.ui.theme.AppBackgroundGradient
import id.ideahousetech.prayertime_qibla.ui.theme.GoldPrimary
import id.ideahousetech.prayertime_qibla.ui.theme.islamicBackground
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.LocationViewModelFactory
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModelFactory
import id.ideahousetech.prayertime_qibla.viewmodel.QiblaViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.QiblaViewModelFactory
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModelFactory
import id.ideahousetech.prayertime_qibla.viewmodel.ExploreViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.ExploreViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import id.ideahousetech.prayertime_qibla.ui.ExploreScreen
import id.ideahousetech.prayertime_qibla.ui.ProfileScreen
import id.ideahousetech.prayertime_qibla.ui.SettingsScreen
import id.ideahousetech.prayertime_qibla.ui.components.FloatingBottomBar
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.AppConfig
import id.ideahousetech.prayertime_qibla.utils.AppSecurityManager
import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.ui.components.SecurityWarningDialog
import id.ideahousetech.prayertime_qibla.ui.components.RamadhanGreetingDialog
import id.ideahousetech.prayertime_qibla.ui.components.ZakatFitrahReminderDialog
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import id.ideahousetech.prayertime_qibla.ui.RamadhanEntryScreen
import id.ideahousetech.prayertime_qibla.ui.StudentLoginScreen
import id.ideahousetech.prayertime_qibla.ui.StudentRegisterScreen
import id.ideahousetech.prayertime_qibla.ui.StudentTaskScreen
import id.ideahousetech.prayertime_qibla.ui.TeacherLoginScreen
import id.ideahousetech.prayertime_qibla.ui.TeacherRegisterScreen
import id.ideahousetech.prayertime_qibla.ui.TeacherDashboardScreen
import id.ideahousetech.prayertime_qibla.viewmodel.StudentAuthViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.StudentTaskViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.TeacherAuthViewModel
import id.ideahousetech.prayertime_qibla.viewmodel.TeacherDashboardViewModel

/**
 * Aktivitas utama (MainActivity) yang bertindak sebagai gerbang masuk aplikasi.
 * Menghandle perizinan modular runtime (GPS + Notifikasi di Android 13+).
 * Memiliki Bottom Navigation Bar custom Material 3 dengan penayangan 5 halaman penting realtime.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi Security Manager sebelum render UI
        AppSecurityManager.initialize(this)

        // 1. Load theme SYNCHRONOUSLY before setContent to prevent any theme flash/flicker
        val context = this
        val prefs = id.ideahousetech.prayertime_qibla.utils.SecurePrefs.get(context)
        val savedThemeMode = prefs.getString(PrefsKeys.APP_THEME_MODE, "dark") ?: "dark"
        val systemIsDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        id.ideahousetech.prayertime_qibla.ui.theme.AppThemeState.updateTheme(savedThemeMode, systemIsDark)

        setContent {
            val currentContext = LocalContext.current
            val currentPrefs = remember { id.ideahousetech.prayertime_qibla.utils.SecurePrefs.get(currentContext) }
            
            var showSplash by remember { mutableStateOf(true) }
            var showOnboarding by remember {
                mutableStateOf(!currentPrefs.getBoolean(PrefsKeys.IS_ONBOARDING_COMPLETED, false))
            }
            
            // Only delay splash screen timeout here; theme mode is already loaded synchronously
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(AppConfig.SPLASH_DURATION_MS)
                showSplash = false
            }
            
            // Reactive theme tracking from our central singleton using 'by' delegation
            val themeMode by id.ideahousetech.prayertime_qibla.ui.theme.AppThemeState.currentThemeMode
 
            // Inisialisasi ViewModel secara mandiri menggunakan Factory untuk kelayakan siklus hidup & pencegahan context leak
            val locationViewModel: LocationViewModel = viewModel(factory = LocationViewModelFactory(currentContext))
            val prayerViewModel: PrayerViewModel = viewModel(factory = PrayerViewModelFactory(currentContext))
            val trackerViewModel: PrayerTrackerViewModel = viewModel(factory = PrayerTrackerViewModelFactory(currentContext))
            val qiblaViewModel: QiblaViewModel = viewModel(factory = QiblaViewModelFactory(currentContext))
            val exploreViewModel: ExploreViewModel = viewModel(factory = ExploreViewModelFactory(currentContext))
 
            MyApplicationTheme(themeMode = themeMode) {
                if (showSplash) {
                    SplashScreen()
                } else if (showOnboarding) {
                    id.ideahousetech.prayertime_qibla.ui.OnboardingScreen(
                        onCompleted = {
                            currentPrefs.edit().putBoolean(PrefsKeys.IS_ONBOARDING_COMPLETED, true).apply()
                            showOnboarding = false
                        }
                    )
                } else {
                    MainLayout(
                        locationViewModel = locationViewModel,
                        prayerViewModel = prayerViewModel,
                        trackerViewModel = trackerViewModel,
                        qiblaViewModel = qiblaViewModel,
                        exploreViewModel = exploreViewModel
                    )
                }
            }
        }
    }
}

/**
 * Definisikan enum halaman/menu navigasi utama
 */
enum class AppScreen(val title: String, val icon: ImageVector) {
    SHOLAT("Sholat", Icons.Default.Timer),
    KIBLAT("Kiblat", Icons.Default.CompassCalibration),
    KALENDER("Kalender", Icons.Default.CalendarMonth),
    JADWAL("Jadwal", Icons.Default.Schedule),
    DOA("Doa-Doa", Icons.Default.MenuBook),
    JADWAL_HARIAN("Jadwal Harian", Icons.Default.Schedule),
    QURAN("Al-Qur'an", Icons.Default.MenuBook),
    TASBIH("Tasbih", Icons.Default.Cached),
    TRACKER("Pelacak Sholat", Icons.Default.Check),
    EXPLORE("Eksplor", Icons.Default.Explore),
    PROFILE("Pengaturan", Icons.Default.Settings),
    RAMADHAN_ENTRY("Tugas Ramadhan", Icons.Default.School),
    RAMADHAN_STUDENT_LOGIN("Login Siswa", Icons.Default.School),
    RAMADHAN_STUDENT_REGISTER("Daftar Siswa", Icons.Default.School),
    RAMADHAN_STUDENT_TASK("Tugas Siswa", Icons.Default.School),
    RAMADHAN_TEACHER_LOGIN("Login Guru", Icons.Default.School),
    RAMADHAN_TEACHER_REGISTER("Daftar Guru", Icons.Default.School),
    RAMADHAN_TEACHER_DASHBOARD("Dashboard Guru", Icons.Default.School)
}

@Composable
fun MainLayout(
    locationViewModel: LocationViewModel,
    prayerViewModel: PrayerViewModel,
    trackerViewModel: id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModel,
    qiblaViewModel: QiblaViewModel,
    exploreViewModel: ExploreViewModel
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.SHOLAT) }
    var screenHistory by remember { mutableStateOf(listOf<AppScreen>()) }

    val studentAuthViewModel: StudentAuthViewModel = viewModel()
    val studentTaskViewModel: StudentTaskViewModel = viewModel()
    val teacherAuthViewModel: TeacherAuthViewModel = viewModel()
    val teacherDashboardViewModel: TeacherDashboardViewModel = viewModel()

    val studentAuthUiState by studentAuthViewModel.uiState.collectAsState()
    val teacherAuthUiState by teacherAuthViewModel.uiState.collectAsState()

    var showSecurityWarning by remember { mutableStateOf(AppSecurityManager.shouldShowSecurityWarning()) }
    if (showSecurityWarning) {
        SecurityWarningDialog(
            securityLevel = AppSecurityManager.securityLevel,
            onDismiss = { showSecurityWarning = false }
        )
    }

    // Popup musiman Ramadhan & Zakat Fitrah (sekali per tahun Hijriah)
    val prefs = remember { SecurePrefs.get(context) }
    val currentHijriYear = remember { HijriDateUtils.getCurrentHijriYear() }
    val ramadhanGreetingKey = remember(currentHijriYear) { PrefsKeys.getRamadhanGreetingKey(currentHijriYear) }
    val zakatReminderKey = remember(currentHijriYear) { PrefsKeys.getZakatReminderKey(currentHijriYear) }

    var showRamadhanGreeting by remember {
        mutableStateOf(
            HijriDateUtils.isFirstDayOfRamadhan() && !prefs.getBoolean(ramadhanGreetingKey, false)
        )
    }

    var showZakatReminder by remember {
        mutableStateOf(
            HijriDateUtils.isFiveDaysBeforeEidFitr() && !prefs.getBoolean(zakatReminderKey, false)
        )
    }

    if (showRamadhanGreeting) {
        RamadhanGreetingDialog(
            onDismiss = {
                prefs.edit().putBoolean(ramadhanGreetingKey, true).apply()
                showRamadhanGreeting = false
            }
        )
    } else if (showZakatReminder) {
        ZakatFitrahReminderDialog(
            onDismiss = {
                prefs.edit().putBoolean(zakatReminderKey, true).apply()
                showZakatReminder = false
            }
        )
    }

    fun navigateTo(screen: AppScreen) {
        if (currentScreen != screen) {
            screenHistory = screenHistory + currentScreen
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            val prev = screenHistory.last()
            screenHistory = screenHistory.dropLast(1)
            currentScreen = prev
        } else {
            currentScreen = AppScreen.SHOLAT
        }
    }

    fun navigateToClearingHistory(screen: AppScreen) {
        screenHistory = emptyList()
        currentScreen = screen
    }

    // Intersepsi tombol kembali (back gesture/physical back button)
    BackHandler(enabled = currentScreen != AppScreen.SHOLAT) {
        navigateBack()
    }

    val userLocation by locationViewModel.userLocation.collectAsState()

    // 0. Mulai AdzanForegroundService jika alarm diset aktif hulu-hilir
    LaunchedEffect(Unit) {
        val prefs = id.ideahousetech.prayertime_qibla.utils.SecurePrefs.get(context)
        if (prefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true)) {
            val intent = android.content.Intent(context, id.ideahousetech.prayertime_qibla.service.AdzanForegroundService::class.java).apply {
                action = id.ideahousetech.prayertime_qibla.service.AdzanForegroundService.ACTION_START
            }
            id.ideahousetech.prayertime_qibla.utils.IntentSecurityUtils.signIntent(intent)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Gagal mendirikan AdzanForegroundService: ${e.message}")
            }
        }
    }

    // 1. Integrasi Izin Lokasi dan Notifikasi di Awal Startup (Android Runtime Permissions)
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Memicu pencarian lokasi dan mulai tracking jika izin disetujui
            locationViewModel.startLocationTracking()
            locationViewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        // Cek izin satu per satu secara aman luring
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Minta akses notifikasi untuk Alarm Adzan di Android 13+ (Tiramisu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocation) {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Izin sudah ada hulu, panggil pemindai lokasi realtime langsung
            locationViewModel.refreshLocation()
        }
    }

    // 2. Pemutakhiran Jadwal Sholat Otomatis Berdasarkan Koordinat GPS Aktif
    LaunchedEffect(userLocation) {
        userLocation?.let {
            prayerViewModel.loadPrayerTimesForLocation(it.latitude, it.longitude)
        }
    }

    // Penanganan Edge-to-Edge dengan windowInsetsPadding untuk area Notch / Drawing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .islamicBackground(0.04f)
    ) {

        val isMainTabScreen = currentScreen in listOf(
            AppScreen.SHOLAT,
            AppScreen.QURAN,
            AppScreen.TRACKER,
            AppScreen.EXPLORE,
            AppScreen.PROFILE
        )

        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            bottomBar = {
                if (isMainTabScreen) {
                    FloatingBottomBar(
                        currentScreen = currentScreen,
                        onTabSelected = { screen ->
                            navigateTo(screen)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize()) {
                if (isMainTabScreen && (studentAuthUiState.currentStudent != null || teacherAuthUiState.currentTeacher != null)) {
                    val isStudent = studentAuthUiState.currentStudent != null
                    val sessionName = if (isStudent) {
                        studentAuthUiState.currentStudent?.name ?: "Siswa"
                    } else {
                        teacherAuthUiState.currentTeacher?.name ?: "Guru"
                    }
                    val roleLabel = if (isStudent) "Siswa" else "Guru"

                    Surface(
                        color = GoldPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Assalamualaikum, $sessionName",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Peran: $roleLabel",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            TextButton(
                                onClick = {
                                    if (isStudent) {
                                        studentAuthViewModel.logout {}
                                    } else {
                                        teacherAuthViewModel.logout {}
                                    }
                                }
                            ) {
                                Text("Logout", fontSize = 12.sp, color = WarningAmber, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        AppScreen.SHOLAT -> HomeScreen(
                            prayerViewModel = prayerViewModel,
                            locationViewModel = locationViewModel,
                            trackerViewModel = trackerViewModel,
                            onNavigateToScreen = { screen ->
                                if (screen == AppScreen.RAMADHAN_ENTRY) {
                                    when {
                                        studentAuthUiState.currentStudent != null ->
                                            navigateToClearingHistory(AppScreen.RAMADHAN_STUDENT_TASK)
                                        teacherAuthUiState.currentTeacher != null ->
                                            navigateToClearingHistory(AppScreen.RAMADHAN_TEACHER_DASHBOARD)
                                        else ->
                                            navigateTo(AppScreen.RAMADHAN_ENTRY)
                                    }
                                } else {
                                    navigateTo(screen)
                                }
                            },
                            onRamadhanClick = {
                                when {
                                    studentAuthUiState.currentStudent != null ->
                                        navigateToClearingHistory(AppScreen.RAMADHAN_STUDENT_TASK)
                                    teacherAuthUiState.currentTeacher != null ->
                                        navigateToClearingHistory(AppScreen.RAMADHAN_TEACHER_DASHBOARD)
                                    else ->
                                        navigateTo(AppScreen.RAMADHAN_ENTRY)
                                }
                            }
                        )
                    AppScreen.EXPLORE -> ExploreScreen(
                        locationViewModel = locationViewModel,
                        exploreViewModel = exploreViewModel,
                        onNavigateToScreen = { screen ->
                            navigateTo(screen)
                        }
                    )
                    AppScreen.PROFILE -> {
                        SettingsScreen(
                            locationViewModel = locationViewModel,
                            prayerViewModel = prayerViewModel,
                            onReminderToggle = { enabled ->
                                // Any custom trigger or reload if required
                            }
                        )
                    }
                    AppScreen.KIBLAT -> QiblaScreen(
                        locationViewModel = locationViewModel,
                        qiblaViewModel = qiblaViewModel,
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.KALENDER -> CalendarScreen(
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.JADWAL -> MonthlyScheduleScreen(
                        prayerViewModel = prayerViewModel,
                        locationViewModel = locationViewModel,
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.DOA -> DoaScreen(
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.JADWAL_HARIAN -> DailyScheduleScreen(
                        prayerViewModel = prayerViewModel,
                        locationViewModel = locationViewModel,
                        onBackClick = { navigateBack() },
                        onNavigateToMonthly = { navigateTo(AppScreen.JADWAL) }
                    )
                    AppScreen.QURAN -> QuranScreen(
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.TASBIH -> TasbihScreen(
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.TRACKER -> id.ideahousetech.prayertime_qibla.ui.PrayerTrackerScreen(
                        trackerViewModel = trackerViewModel,
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_ENTRY -> RamadhanEntryScreen(
                        onSelectStudent = { navigateTo(AppScreen.RAMADHAN_STUDENT_LOGIN) },
                        onSelectTeacher = { navigateTo(AppScreen.RAMADHAN_TEACHER_LOGIN) },
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_STUDENT_LOGIN -> StudentLoginScreen(
                        viewModel = studentAuthViewModel,
                        onLoginSuccess = { navigateToClearingHistory(AppScreen.RAMADHAN_STUDENT_TASK) },
                        onNavigateToRegister = { navigateTo(AppScreen.RAMADHAN_STUDENT_REGISTER) },
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_STUDENT_REGISTER -> StudentRegisterScreen(
                        viewModel = studentAuthViewModel,
                        onRegisterSuccess = { navigateTo(AppScreen.RAMADHAN_STUDENT_LOGIN) },
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_STUDENT_TASK -> StudentTaskScreen(
                        viewModel = studentTaskViewModel,
                        onLogout = {
                            studentAuthViewModel.logout {
                                navigateToClearingHistory(AppScreen.RAMADHAN_ENTRY)
                            }
                        },
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_TEACHER_LOGIN -> TeacherLoginScreen(
                        viewModel = teacherAuthViewModel,
                        onLoginSuccess = { navigateToClearingHistory(AppScreen.RAMADHAN_TEACHER_DASHBOARD) },
                        onNavigateToRegister = { navigateTo(AppScreen.RAMADHAN_TEACHER_REGISTER) },
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_TEACHER_REGISTER -> TeacherRegisterScreen(
                        viewModel = teacherAuthViewModel,
                        onRegisterSuccess = { navigateTo(AppScreen.RAMADHAN_TEACHER_LOGIN) },
                        onBackClick = { navigateBack() }
                    )
                    AppScreen.RAMADHAN_TEACHER_DASHBOARD -> TeacherDashboardScreen(
                        viewModel = teacherDashboardViewModel,
                        onLogout = {
                            teacherAuthViewModel.logout {
                                navigateToClearingHistory(AppScreen.RAMADHAN_ENTRY)
                            }
                        },
                        onBackClick = { navigateBack() }
                    )
                }
            }
        }
    }
}
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackgroundGradient),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Majestic Glowing Image Container
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, GoldPrimary, RoundedCornerShape(32.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_artwork),
                    contentDescription = "Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "WAKTU SHOLAT & KIBLAT",
                color = GoldPrimary, // Majestic Gold
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mari Tegakkan Sholat Tepat Waktu",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
