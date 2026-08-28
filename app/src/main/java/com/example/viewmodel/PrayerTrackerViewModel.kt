package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.data.AppDatabase
import id.ideahousetech.prayertime_qibla.data.PrayerTracker
import id.ideahousetech.prayertime_qibla.data.PrayerTrackerRepository
import id.ideahousetech.prayertime_qibla.model.AchievementBadge
import id.ideahousetech.prayertime_qibla.model.BadgeCategory
import id.ideahousetech.prayertime_qibla.model.WeeklySpiritualSummary
import id.ideahousetech.prayertime_qibla.model.MonthlySpiritualSummary
import id.ideahousetech.prayertime_qibla.model.PrayerStatus
import id.ideahousetech.prayertime_qibla.model.PrayerName
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel mandiri untuk mengelola sirkulasi data pelacakan sholat harian (Prayer Tracker).
 * Menyediakan statistik, perhitungan streak, kalkulasi compliance, kontrol interaksi checklist,
 * serta kalkulasi luring berdasar Hadits Shalat untuk Achievement Badges, Weekly & Monthly summaries.
 */
class PrayerTrackerViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val db = AppDatabase.getInstance(appContext)
    private val repository = PrayerTrackerRepository(db.prayerTrackerDao())
    private val prefs = id.ideahousetech.prayertime_qibla.utils.SecurePrefs.get(appContext)

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Tanggal aktif terpilih untuk ditinjau / difilter
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Ambil data tracker untuk tanggal terpilih
    val selectedDateTracker: StateFlow<PrayerTracker?> = _selectedDate
        .flatMapLatest { date -> repository.getTrackerFlowForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Seluruh riwayat untuk kalkulasi statistik & visualisasi, dibatasi 365 hari untuk performa
    val allTrackers: StateFlow<List<PrayerTracker>> = repository.getTrackersFlowForLastNDays(365)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pasokan data terbatas (recent 30 hari) untuk weekly/monthly summary
    val recentTrackers: StateFlow<List<PrayerTracker>> = repository.getTrackersFlowForLastNDays(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State flow untuk memantau status strict/lenient secara reaktif
    private val _isStrictMode = MutableStateFlow(prefs.getBoolean(PrefsKeys.STREAK_STRICT_MODE, false))
    val isStrictMode: StateFlow<Boolean> = _isStrictMode.asStateFlow()

    /**
     * Memperbarui mode kalkulasi streak (strict vs lenient) secara reaktif.
     */
    fun updateStrictMode(enabled: Boolean) {
        prefs.edit().putBoolean(PrefsKeys.STREAK_STRICT_MODE, enabled).apply()
        _isStrictMode.value = enabled
    }

    // Pasokan live data streak berturut-turut aktif (diambil dari data 365 hari)
    val streakCount: StateFlow<Int> = combine(allTrackers, _isStrictMode) { list, isStrict ->
        calculateActiveStreak(list, isStrict)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Pasokan live data streak terbaik historis (diambil dari data 365 hari)
    val bestStreakCount: StateFlow<Int> = allTrackers
        .map { list -> calculateBestStreak(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Pasokan total jumlah sholat berjamaah (high engagement metric) langsung dihitung di DB via SQL aggregation
    val jamaahCount: StateFlow<Int> = repository.getTotalCountByStatus("Jamaah")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Pasokan total sholat tepat waktu / munfarid + jamaah + masbuq langsung dihitung di DB via SQL aggregation
    val totalDoneCount: StateFlow<Int> = repository.getTotalDoneCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filter tracker sebulan berjalan (untuk visualisasi kalender pelacak)
    private val _calendarMonthQuery = MutableStateFlow(getCurrentMonthQuery())
    val calendarMonthTrackers: StateFlow<List<PrayerTracker>> = _calendarMonthQuery
        .flatMapLatest { query -> repository.getTrackersFlowForMonth(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // LIVE STREAM BADGES STATE (Dihitung Realtime, Tanpa Tulis Ulang DB)
    // -------------------------------------------------------------
    val badges: StateFlow<List<AchievementBadge>> = allTrackers
        .map { list -> computeDynamicBadges(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // LIVE WEEKLY RECAP STATE (Past 7 Days Calculator) menggunakan 30 hari data terakhir
    // -------------------------------------------------------------
    val weeklySummary: StateFlow<WeeklySpiritualSummary> = recentTrackers
        .map { list -> computeWeeklySummary(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createDefaultWeeklySummary())

    // -------------------------------------------------------------
    // LIVE MONTHLY RECAP STATE (Past 30 Days Calculator) menggunakan 30 hari data terakhir + status badge
    // -------------------------------------------------------------
    val monthlySummary: StateFlow<MonthlySpiritualSummary> = combine(recentTrackers, badges) { recentList, badgeList ->
        computeMonthlySummary(recentList, badgeList.count { it.isUnlocked })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), createDefaultMonthlySummary())


    /**
     * Memperoleh tanggal hari ini berfomrat standar.
     */
    fun getTodayDateString(): String {
        return sdf.format(Date())
    }

    private fun getCurrentMonthQuery(): String {
        val monthSdf = SimpleDateFormat("yyyy-MM-", Locale.US)
        return "${monthSdf.format(Date())}%"
    }

    /**
     * Mengatur tanggal tinjauan tracker secara dinamis.
     */
    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
        // Update query bulan jika seandainya berganti bulan
        if (dateString.length >= 7) {
            val monthPart = dateString.substring(0, 8) // "yyyy-MM-"
            _calendarMonthQuery.value = "$monthPart%"
        }
    }

    /**
     * Memperbarui status sholat tertentu pada tanggal tertentu secara luring cepat.
     */
    fun updatePrayerStatus(date: String, prayerName: String, newStatus: String) {
        viewModelScope.launch {
            val existing = repository.getTrackerForDateDirect(date) ?: PrayerTracker(date = date)
            val pName = PrayerName.fromString(prayerName)
            val pStatus = PrayerStatus.fromString(newStatus)
            val updated = if (pName != null) {
                pName.updateTrackerStatus(existing, pStatus)
            } else {
                existing
            }
            repository.saveOrUpdateTracker(updated)
        }
    }

    /**
     * Mengonversi string tanggal "yyyy-MM-dd" menjadi nomor hari Julian secara matematis murni.
     * Pendekatan ini sangat cepat tanpa SimpleDateFormat parsing ataupun alokasi objek Date.
     */
    fun toJulianDayNumber(dateStr: String): Int {
        val parts = dateStr.split("-")
        if (parts.size != 3) return 0
        val year = parts[0].toIntOrNull() ?: return 0
        val month = parts[1].toIntOrNull() ?: return 0
        val day = parts[2].toIntOrNull() ?: return 0

        // Algoritma konversi standar kalender Gregorian ke Julian Day Number (JDN)
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private val MAX_STREAK_CHECK_DAYS = 365

    /**
     * Mengkalkulasi streak hari ibadah penuh secara beruntun dengan pilihan strict atau lenient.
     * @param useStrict true untuk mode ketat (tidak ada record dianggap streak putus),
     *                  false untuk mode toleran (tidak ada record dilewati, streak tetap hidup).
     */
    fun calculateActiveStreak(trackers: List<PrayerTracker>, useStrict: Boolean = false): Int {
        return if (useStrict) {
            calculateActiveStreakStrict(trackers)
        } else {
            calculateActiveStreakLenient(trackers)
        }
    }

    private fun calculateActiveStreakStrict(trackers: List<PrayerTracker>): Int {
        if (trackers.isEmpty()) return 0
        
        val trackerMap = trackers.associateBy { it.date }
        var streak = 0
        val cal = Calendar.getInstance()
        
        val todayStr = sdf.format(cal.time)
        val todayTracker = trackerMap[todayStr]
        
        // Bila hari ini belum tuntas, cek apakah kemarin tuntas untuk menjaga streak tetap hidup
        val startCal = if (todayTracker != null && todayTracker.isFullyCompleted()) {
            cal
        } else {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        }

        var checkStr = sdf.format(startCal.time)
        var daysChecked = 0
        
        while (daysChecked < MAX_STREAK_CHECK_DAYS) {
            val tracker = trackerMap[checkStr]
            if (tracker != null && tracker.isFullyCompleted()) {
                streak++
                startCal.add(Calendar.DAY_OF_YEAR, -1)
                checkStr = sdf.format(startCal.time)
                daysChecked++
            } else {
                // Strict: tidak ada record atau tidak tuntas = langsung putus!
                break
            }
        }
        return streak
    }

    private fun calculateActiveStreakLenient(trackers: List<PrayerTracker>): Int {
        if (trackers.isEmpty()) return 0
        
        val trackerMap = trackers.associateBy { it.date }
        
        // Temukan batas tanggal paling awal yang tersimpan di DB agar pencarian tidak mubazir mundur 365 hari penuh
        val julianDays = trackers.map { toJulianDayNumber(it.date) }.filter { it > 0 }
        if (julianDays.isEmpty()) return 0
        val minJulianDay = julianDays.minOrNull() ?: 0

        var streak = 0
        val cal = Calendar.getInstance()
        
        val todayStr = sdf.format(cal.time)
        val todayTracker = trackerMap[todayStr]
        
        // Bila hari ini belum tuntas, cek apakah kemarin tuntas untuk menjaga streak tetap hidup
        val startCal = if (todayTracker != null && todayTracker.isFullyCompleted()) {
            cal
        } else {
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        }

        var checkStr = sdf.format(startCal.time)
        var daysChecked = 0
        
        while (daysChecked < MAX_STREAK_CHECK_DAYS) {
            val currentJulian = toJulianDayNumber(checkStr)
            if (currentJulian < minJulianDay) {
                // Sudah melebihi batas data historis paling awal di database, hentikan pencarian
                break
            }
            
            val tracker = trackerMap[checkStr]
            if (tracker == null) {
                // Lenient: tidak ada record = dilewati (tidak dihitung putus), lanjut periksa hari kemarin
                startCal.add(Calendar.DAY_OF_YEAR, -1)
                checkStr = sdf.format(startCal.time)
                daysChecked++
            } else if (tracker.isFullyCompleted()) {
                streak++
                startCal.add(Calendar.DAY_OF_YEAR, -1)
                checkStr = sdf.format(startCal.time)
                daysChecked++
            } else {
                // Ada record tapi tidak penuh/bolong -> streak terputus!
                break
            }
        }
        return streak
    }

    /**
     * Mengkalkulasi streak terpanjang sepanjang masa secara deterministik.
     * Sangat efisien dengan perbandingan JDN langsung tanpa SimpleDateFormat parse di dalam loop.
     */
    fun calculateBestStreak(trackers: List<PrayerTracker>): Int {
        if (trackers.isEmpty()) return 0
        
        // Kelompokkan tanggal lengkap berstatus diselesaikan (100% full)
        val completedDatesSorted = trackers
            .filter { it.isFullyCompleted() }
            .map { it.date }
            .sorted()
            
        if (completedDatesSorted.isEmpty()) return 0
        
        var maxStreak = 0
        var currentStreak = 0
        var prevJulianDay: Int? = null
        
        for (dateStr in completedDatesSorted) {
            val currentJulianDay = toJulianDayNumber(dateStr)
            if (currentJulianDay == 0) continue
            
            if (prevJulianDay == null) {
                currentStreak = 1
            } else {
                val diffDays = currentJulianDay - prevJulianDay
                if (diffDays == 1) {
                    currentStreak++
                } else if (diffDays > 1) {
                    if (currentStreak > maxStreak) {
                        maxStreak = currentStreak
                    }
                    currentStreak = 1
                }
            }
            prevJulianDay = currentJulianDay
        }
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
        }
        return maxStreak
    }

    /**
     * Pemetaan dinamis luring penentuan Milestone Spiritual (Achievement Badges)
     */
    private fun computeDynamicBadges(trackers: List<PrayerTracker>): List<AchievementBadge> {
        // Parameter pendukung kalkulasi
        val subuhDoneCount = trackers.count { t ->
            t.subuhStatus != PrayerStatus.NONE && t.subuhStatus != PrayerStatus.HALANGAN
        }
        
        val totalJamaahCount = trackers.sumOf { t ->
            listOf(t.subuhStatus, t.dhuhrStatus, t.asrStatus, t.maghribStatus, t.isyaStatus)
                .count { it == PrayerStatus.JAMAAH }
        }
        
        val bestStreak = calculateBestStreak(trackers)
        
        // Hari di mana Subuh & Isya sekaligus terlaksana secara Jamaah
        val subuhIsyaJamaahCount = trackers.count { t ->
            t.subuhStatus == PrayerStatus.JAMAAH && t.isyaStatus == PrayerStatus.JAMAAH
        }
        
        val dzuhurAsharCount = trackers.sumOf { t ->
            listOf(t.dhuhrStatus, t.asrStatus).count { it != PrayerStatus.NONE && it != PrayerStatus.HALANGAN }
        }
        
        val isyaJamaahCount = trackers.count { t -> t.isyaStatus == PrayerStatus.JAMAAH }

        // Bikin daftar Badge
        return listOf(
            AchievementBadge(
                id = "fajr_warrior",
                title = "Mujahid Subuh",
                description = "Tegakkan salat Fajar secara konsisten untuk menyulut energi keberkahan hari.",
                requirementText = "Selesaikan 7x sholat Subuh tepat waktu",
                isUnlocked = subuhDoneCount >= 7,
                progress = (subuhDoneCount / 7.0f).coerceIn(0f, 1f),
                progressText = "$subuhDoneCount / 7 Subuh",
                hadithSource = "HR. Muslim (Dua rakaat Fajar lebih baik daripada dunia beserta isinya)",
                spiritualBenefit = "Mendapatkan jaminan perlindungan penuh dari Allah dan menyamai salat semalam suntuk.",
                category = BadgeCategory.SPECIFIC_PRAYER
            ),
            AchievementBadge(
                id = "jamaah_champion",
                title = "Duta Jama'ah",
                description = "Mengikat ukhuwah shaf dan melipatgandakan derajat ketaatan berjamaah.",
                requirementText = "Shalat berjamaah sebanyak 15x di Masjid/Iringan",
                isUnlocked = totalJamaahCount >= 15,
                progress = (totalJamaahCount / 15.0f).coerceIn(0f, 1f),
                progressText = "$totalJamaahCount / 15 Jamaah",
                hadithSource = "HR. Bukhari & Muslim (Salat berjamaah melampaui shalat sendirian sebanyak 27 derajat)",
                spiritualBenefit = "Melejitkan kepasrahan diri dan mendoakan ampunan malaikat penunggu shaf.",
                category = BadgeCategory.JAMAAH
            ),
            AchievementBadge(
                id = "seven_day_istiqomah",
                title = "Benteng Istiqomah",
                description = "Membangun pertahanan disiplin diri sepekan penuh tanpa bolong.",
                requirementText = "Pertahankan 7 hari berturut-turut full selesai gratis",
                isUnlocked = bestStreak >= 7,
                progress = (bestStreak / 7.0f).coerceIn(0f, 1f),
                progressText = "$bestStreak / 7 Hari",
                hadithSource = "HR. Bukhari (Amalan yang paling dicintai Allah adalah yang berkelanjutan)",
                spiritualBenefit = "Mengubah ketaatan menjadi tabiat natural nurani yang melunakkan kerasnya hati.",
                category = BadgeCategory.STREAK
            ),
            AchievementBadge(
                id = "perfect_light",
                title = "Cahaya Sempurna",
                description = "Menembus kegelapan fajar dan isya demi jaminan keselamatan di hari akhir.",
                requirementText = "Subuh & Isya berjamaah secara simultan selama 3 hari",
                isUnlocked = subuhIsyaJamaahCount >= 3,
                progress = (subuhIsyaJamaahCount / 3.0f).coerceIn(0f, 1f),
                progressText = "$subuhIsyaJamaahCount / 3 Hari",
                hadithSource = "HR. Abu Dawud (Gembirakanlah pencari masjid di kegelapan dengan cahaya terang di akhirat)",
                spiritualBenefit = "Memberikan navigasi jernih berupa kilatan nur saat menyeberangi jembatan sirath.",
                category = BadgeCategory.COMPRESSIVE
            ),
            AchievementBadge(
                id = "midday_guardian",
                title = "Pilar Dzuhur & Ashar",
                description = "Menjaga sumbu tengah hari di kala kesibukan duniawi memuncak.",
                requirementText = "Tegakkan 10x Dzuhur dan 10x Ashar tepat waktu",
                isUnlocked = dzuhurAsharCount >= 20,
                progress = (dzuhurAsharCount / 20.0f).coerceIn(0f, 1f),
                progressText = "$dzuhurAsharCount / 20 Salat",
                hadithSource = "HR. Ahmad (Barangsiapa menjaga salat sebelum ashar, dirahmati Allah jiwanya)",
                spiritualBenefit = "Menjaga keseimbangan raga dari kelengahan di jam produktif duniawi.",
                category = BadgeCategory.SPECIFIC_PRAYER
            ),
            AchievementBadge(
                id = "isya_lover",
                title = "Pecinta Isya",
                description = "Menyegel hari dengan sujud tenang penenang sirkadian tubuh.",
                requirementText = "Tegakkan Isya berjamaah sebanyak 7 kali",
                isUnlocked = isyaJamaahCount >= 7,
                progress = (isyaJamaahCount / 7.0f).coerceIn(0f, 1f),
                progressText = "$isyaJamaahCount / 7 Isya",
                hadithSource = "HR. Muslim (Siapa salat Isya berjamaah maka laksana salat setengah malam)",
                spiritualBenefit = "Pembersihan toksin batin sebelum rehat malam diiringi selimut doa malaikat.",
                category = BadgeCategory.JAMAAH
            )
        )
    }

    /**
     * Menghitung Ringkasan Spiritual Mingguan (7 Hari terakhir) secara dinamis.
     */
    private fun computeWeeklySummary(trackers: List<PrayerTracker>): WeeklySpiritualSummary {
        val daysAgo7 = getPastDateStrings(7)
        val trackerMap = trackers.associateBy { it.date }
        
        val totalOpportunities = 35 // 7 hari * 5 waktu
        val stats = PrayerStats()

        for (dateStr in daysAgo7) {
            val t = trackerMap[dateStr] ?: continue
            processPrayerStatus(t.subuhStatus, stats) { stats.subuhCount++ }
            processPrayerStatus(t.dhuhrStatus, stats) { stats.dhuhrCount++ }
            processPrayerStatus(t.asrStatus, stats) { stats.asrCount++ }
            processPrayerStatus(t.maghribStatus, stats) { stats.maghribCount++ }
            processPrayerStatus(t.isyaStatus, stats) { stats.isyaCount++ }
        }

        // Penyesuaian total peluang jika ada halangan (udzur syar'i tidak dinilai bolong)
        val adjustedTotal = totalOpportunities - stats.halangan
        val completionPercentage = if (adjustedTotal > 0) stats.completedCount.toFloat() / adjustedTotal else 0.0f
        
        // Tentukan tausiyah nasihat khusus berbasis kelemahan salat
        val (advice, source) = when {
            completionPercentage >= 0.90f -> {
                Pair(
                    "Maa Syaa Allah, pekan yang dipenuhi ketaatan mulia! Pertahankan cahaya kepatuhan ini dan lengkapi dengan salat sunnah rawatib sebelum/sesudah salat fardhu guna membangun istana di Surga kelak.",
                    "HR. At-Tirmidzi no. 414"
                )
            }
            stats.subuhCount < 4 -> {
                Pair(
                    "Tercatat kelonggaran dalam menegakkan shalat Subuh pekan ini. Ingatlah, salat Subuh adalah saksi penutup malam yang dihadiri langsung oleh para malaikat Fajar. Letakkan alarm menjauh dari pembaringan raga.",
                    "QS. Al-Isra: 78"
                )
            }
            stats.asrCount < 4 -> {
                Pair(
                    "Pilar Ashar Anda melemah di tengah hantaman kesibukan dunia harian. Shalat ula (Ashar) memiliki tempat agung; barangsiapa melewatkannya dengan sengaja, terancam gugur amalan usahanya.",
                    "HR. Bukhari no. 553"
                )
            }
            stats.jamaah < 6 -> {
                Pair(
                    "Ketukan langkah menuju salat Berjamaah masih minim pekan ini. Usahakan minimal mengejar Isya atau Subuh berjamaah untuk menghalau kemunafikan batin dan meraih 27 derajat ketaatan.",
                    "HR. Bukhari no. 644"
                )
            }
            else -> {
                Pair(
                    "Alhamdulillah, fondasi shalat lima waktu Anda tegak berdiri. Evaluasi sedikit sholat sendirian Anda agar perlahan naik kelas menuju jamaah demi ketenangan rohani yang seutuhnya.",
                    "Nasihat Istiqomah Ulama"
                )
            }
        }

        val displayFormatter = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        val cal = Calendar.getInstance()
        val endLabel = displayFormatter.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startLabel = displayFormatter.format(cal.time)

        return WeeklySpiritualSummary(
            startDateLabel = startLabel,
            endDateLabel = endLabel,
            completionPercentage = completionPercentage,
            totalJamaah = stats.jamaah,
            totalMunfarid = stats.munfarid,
            totalMasbuq = stats.masbuq,
            totalHalangan = stats.halangan,
            highestStreakThisWeek = Math.min(7, calculateActiveStreak(trackers)),
            prayerDistribution = mapOf(
                "Subuh" to stats.subuhCount,
                "Dzuhur" to stats.dhuhrCount,
                "Ashar" to stats.asrCount,
                "Maghrib" to stats.maghribCount,
                "Isya" to stats.isyaCount
            ),
            adviceTherapy = advice,
            adviceSource = source
        )
    }

    /**
     * Memproses status sholat untuk akumulasi statistik mingguan secara DRY.
     */
    private fun processPrayerStatus(status: PrayerStatus, stats: PrayerStats, incrementPrayerCount: () -> Unit) {
        if (status != PrayerStatus.NONE) {
            if (status == PrayerStatus.HALANGAN) {
                stats.halangan++
            } else {
                stats.completedCount++
                when (status) {
                    PrayerStatus.JAMAAH -> {
                        stats.jamaah++
                        incrementPrayerCount()
                    }
                    PrayerStatus.MUNFARID -> {
                        stats.munfarid++
                        incrementPrayerCount()
                    }
                    PrayerStatus.MASBUQ -> {
                        stats.masbuq++
                        incrementPrayerCount()
                    }
                    else -> {}
                }
            }
        }
    }


    /**
     * Menghitung Ringkasan Spiritual Bulanan (30 Hari terakhir) secara dinamis.
     */
    private fun computeMonthlySummary(trackers: List<PrayerTracker>, unlockedCount: Int): MonthlySpiritualSummary {
        val daysAgo30 = getPastDateStrings(30)
        val trackerMap = trackers.associateBy { it.date }
        
        var totalOpportunities = 150
        var completedCount = 0
        var jamaahCount = 0
        var munfaridCount = 0
        var masbuqCount = 0
        var activeDays = 0
        var halanganCount = 0

        for (dateStr in daysAgo30) {
            val t = trackerMap[dateStr]
            if (t != null) {
                activeDays++
                val sList = listOf(t.subuhStatus, t.dhuhrStatus, t.asrStatus, t.maghribStatus, t.isyaStatus)
                completedCount += sList.count { it != PrayerStatus.NONE && it != PrayerStatus.HALANGAN }
                halanganCount += sList.count { it == PrayerStatus.HALANGAN }
                jamaahCount += sList.count { it == PrayerStatus.JAMAAH }
                munfaridCount += sList.count { it == PrayerStatus.MUNFARID }
                masbuqCount += sList.count { it == PrayerStatus.MASBUQ }
            }
        }

        val adjustedTotal = totalOpportunities - halanganCount
        val completionPercentage = if (adjustedTotal > 0) completedCount.toFloat() / adjustedTotal else 0.0f
        
        // Nama bulan saat ini
        val currentMonthLabel = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())

        val (primaryBadge, assessment) = when {
            completionPercentage >= 0.85 -> Pair(
                "Tangguh Istiqomah",
                "Masya Allah, sebulan penuh menegakkan salat dengan kedisiplinan luar biasa! Kestabilan rohani Anda berada pada puncak pengabdian yang sangat tinggi."
            )
            completionPercentage >= 0.50 -> Pair(
                "Penjelajah Saf",
                "Alhamdulillah, Anda secara konsisten mencetak rekor ketaatan shalat yang tangguh. Beberapa bolongan kecil di sela kesibukan dapat ditutupi dengan taubat salat sunnah."
            )
            else -> Pair(
                "Pencari Hidayah",
                "Bulan ini membuktikan komitmen Anda untuk terus mencoba bersujud meski didera kesibukan luar biasa. Jangan menyerah, mulailah berfokus pada kelengkapan satu hari demi satu hari."
            )
        }

        return MonthlySpiritualSummary(
            monthLabel = currentMonthLabel,
            completionPercentage = completionPercentage,
            totalJamaah = jamaahCount,
            totalMunfarid = munfaridCount,
            totalMasbuq = masbuqCount,
            activeDaysCount = activeDays,
            unlockedBadgesCount = unlockedCount,
            bestStreakThisMonth = Math.min(30, calculateBestStreak(trackers)),
            primaryAchievedMilestone = primaryBadge,
            generalAssessment = assessment
        )
    }

    private fun getPastDateStrings(days: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0 until days) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list
    }

    private fun createDefaultWeeklySummary() = WeeklySpiritualSummary(
        startDateLabel = "-", endDateLabel = "-", completionPercentage = 0f,
        totalJamaah = 0, totalMunfarid = 0, totalMasbuq = 0, totalHalangan = 0, highestStreakThisWeek = 0,
        prayerDistribution = emptyMap(), adviceTherapy = "Catat riwayat sholat Anda sepekan untuk mengukur pencapaian spiritual.",
        adviceSource = "Fitur Luring"
    )

    private fun createDefaultMonthlySummary() = MonthlySpiritualSummary(
        monthLabel = "-", completionPercentage = 0f, totalJamaah = 0, totalMunfarid = 0, totalMasbuq = 0,
        activeDaysCount = 0, unlockedBadgesCount = 0, bestStreakThisMonth = 0, primaryAchievedMilestone = "Pencari Hidayah",
        generalAssessment = "Mulai mencatatkan aktivitas sholat Anda untuk memperoleh insight spiritual bulanan."
    )
}

class PrayerTrackerViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerTrackerViewModel::class.java)) {
            return PrayerTrackerViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Data class penampung akumulasi statistik pelacakan sholat.
 */
data class PrayerStats(
    var completedCount: Int = 0,
    var halangan: Int = 0,
    var jamaah: Int = 0,
    var munfarid: Int = 0,
    var masbuq: Int = 0,
    var subuhCount: Int = 0,
    var dhuhrCount: Int = 0,
    var asrCount: Int = 0,
    var maghribCount: Int = 0,
    var isyaCount: Int = 0
)


