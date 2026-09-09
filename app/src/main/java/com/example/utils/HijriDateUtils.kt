package id.ideahousetech.prayertime_qibla.utils

import id.ideahousetech.prayertime_qibla.model.IslamicHoliday
import java.util.Calendar
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/**
 * Utilitas untuk mengkonversi tanggal Gregorian (Masehi) ke Hijriah (Komariah) secara astronomis.
 * Mengklasifikasikan Hari Raya / Hari Besar Islam nasional dan mengembalikan deskripsi amalan khusus terkait.
 * Berguna untuk menghias kalender dan memunculkan pop-up ibadah di halaman utama.
 */
object HijriDateUtils {

    // Daftar Hari Raya yang divalidasi dengan format "DD-MM" Hijriah dengan Sejarah & Dalil Lengkap
    private val holidays = listOf(
        IslamicHoliday(
            "01-01", 
            "Tahun Baru Hijriah (1 Muharram)", 
            "Semangat berhijrah menjadi pribadi yang lebih baik, memperbanyak doa akhir tahun dan awal tahun.",
            "Merujuk pada peristiwa hijrahnya Rasulullah SAW dari Makkah ke Madinah pada tahun 622 M. Peristiwa bersejarah ini disepakati oleh Khalifah Umar bin Khattab dan para sahabat sebagai tonggak awal penanggalan kalender Islam (Hijriah) karena memisahkan yang haq dan yang bathil.",
            "QS. At-Taubah: 20: \"Orang-orang yang beriman dan berhijrah serta berjihad di jalan Allah dengan harta benda dan diri mereka, adalah lebih tinggi derajatnya di sisi Allah.\" Rasulullah SAW juga bersabda: \"Hijrah tidak akan terhenti hingga pintu taubat tertutup, dan taubat tidak terhenti hingga matahari terbit dari barat.\" (HR. Abu Dawud)"
        ),
        IslamicHoliday(
            "10-01", 
            "Hari Asyura (10 Muharram)", 
            "Disunnahkan berpuasa Asyura pelebur dosa setahun yang lalu.",
            "Hari agung di mana Allah SWT menyelamatkan Nabi Musa AS dan Bani Israil dari kejaran pasukan raja zalim Firaun dengan membelah Laut Merah secara mukjizat. Nabi Musa AS kemudian berpuasa pada hari ini sebagai ungkapan syukur yang mendalam kepada Allah.",
            "Dari hadits Ibnu Abbas RA, Rasulullah SAW bersabda: \"Puasa hari Asyura, sungguh aku berharap kepada Allah akan menghapuskan dosa setahun yang lalu.\" (HR. Muslim). Rasulullah SAW dan para sahabat senantiasa menjaga keutamaan puasa sunnah ini."
        ),
        IslamicHoliday(
            "12-03", 
            "Maulid Nabi Muhammad SAW", 
            "Memperbanyak membaca Shalawat Nabi dan mendalami sirah perjuangan Rasulullah SAW.",
            "Kelahiran agung Baginda Rasulullah SAW di kota suci Makkah pada Tahun Gajah (12 Rabi'ul Awwal). Beliau diutus Allah SWT untuk membimbing umat manusia keluar dari kegelapan jahiliyah menuju cahaya iman yang lurus.",
            "QS. Al-Anbya: 107: \"Dan tiadalah Kami mengutus kamu, melainkan untuk (menjadi) rahmat bagi semesta alam.\" Disunnahkan mensyukuri kelahiran beliau dengan senantiasa membaca shalawat, meneladani akhlak mulia kepemimpinan beliau, dan mempelajari Sirah Nabawiyah."
        ),
        IslamicHoliday(
            "27-07", 
            "Isra' Mi'raj Nabi Muhammad SAW", 
            "Merenungkan perjalanan agung diterimanya perintah sholat 5 waktu.",
            "Perjalanan malam mukjizat luar biasa Rasulullah SAW dari Masjidil Haram (Makkah) ke Masjidil Aqsa (Yerusalem) kemudian naik melintasi langit ketujuh hingga Sidratul Muntaha dalam satu malam untuk menerima perintah ibadah shalat wajib lima waktu langsung dari Allah SWT.",
            "QS. Al-Isra': 1: \"Maha Suci Allah, yang telah memperjalankan hamba-Nya pada suatu malam dari Al Masjidil Haram ke Al Masjidil Aqsha...\" Shalat lima waktu adalah tiang agama dan bentuk komunikasi termulia antara seorang hamba dengan Sang Pencipta."
        ),
        IslamicHoliday(
            "15-08",
            "Nisfu Sya'ban",
            "Malam pertengahan bulan Sya'ban, menjelang Ramadhan. Sebagian umat Islam mengisi malam ini dengan memperbanyak dzikir, doa, dan introspeksi diri sebagai persiapan menyambut bulan suci Ramadhan. Terdapat perbedaan pandangan di kalangan ulama mengenai amalan khusus pada malam ini — silakan merujuk pada bimbingan ustadz/guru agama setempat."
        ),
        IslamicHoliday(
            "01-09", 
            "Awal Puasa Ramadhan", 
            "Memulai ibadah puasa wajib sebulan penuh, tarawih, tadarus Al-Qur'an dan zakat fitrah.",
            "Masuknya bulan suci Ramadhan yang dipenuhi limpahan rahmat, berkah, ampunan (maghfirah), serta pembebasan dari api neraka. Di bulan inilah seluruh umat Muslim diwajibkan melakukan ibadah puasa siang hari dan menghidupkan malam hari dengan shalat tarawih.",
            "QS. Al-Baqarah: 183: \"Wahai orang-orang yang beriman! Diwajibkan atas kamu berpuasa sebagaimana diwajibkan atas orang-orang sebelum kamu agar kamu bertakwa.\" Rasulullah SAW bersabda: \"Barangsiapa berpuasa Ramadhan karena iman dan mengharap pahala, maka dosanya yang telah lalu akan diampuni.\" (HR. Bukhari)"
        ),
        IslamicHoliday(
            "17-09", 
            "Nuzulul Qur'an (17 Ramadhan)", 
            "Memperingati malam turunnya mukjizat kitab suci Al-Qur'an pertama kali ke dunia.",
            "Peristiwa bersejarah turunnya ayat-ayat suci Al-Qur'an al-Karim pertama kali kepada Rasulullah SAW yaitu Surah Al-Alaq ayat 1-5 di Gua Hira, melalui perantara malaikat Jibril AS pada malam yang penuh dengan berkah spiritual.",
            "QS. Al-Baqarah: 185: \"Bulan Ramadhan, bulan yang di dalamnya diturunkan (permulaan) Al Qur'an sebagai petunjuk bagi manusia.\" Membaca, mempelajari, serta mengamalkan isi kandungan Al-Qur'an pada malam diturunkannya mukjizat ini memiliki fadhilah limpahan pahala yang sangat besar."
        ),
        IslamicHoliday(
            "01-10", 
            "Hari Raya Idul Fitri (1 Syawal)", 
            "Kemenangan suci setelah sebulan berpuasa. Disunnahkan shalat Idul Fitri dan silaturahmi maaf-memaafkan.",
            "Hari raya kemenangan besar umat Islam setelah berhasil menyelesaikan ujian ibadah puasa Ramadhan sebulan penuh. Hari di mana umat Islam kembali suci kepada fitrah kemanusiaan, diisi dengan takbiran mengagungkan Asma Allah dan saling memberi maaf.",
            "Rasulullah SAW bersabda: \"Sesungguhnya Allah memiliki dua hari raya untuk kalian di mana kalian bermain di dalamnya pada masa Jahiliyah, dan Allah telah menggantinya dengan yang lebih baik: Idul Fitri dan Idul Adha.\" (HR. An-Nasa'i). Disunnahkan mandi sebelum shalat Id, mengenakan pakaian terbaik, dan makan sebelum berangkat shalat."
        ),
        IslamicHoliday(
            "09-12", 
            "Hari Arafah (9 Dzulhijjah)", 
            "Disunnahkan berpuasa Arafah bagi yang tidak berhaji, menghapus dosa 2 tahun.",
            "Hari puncak agung ibadah haji di mana para jamaah haji dari seluruh penjuru dunia berkumpul di padang Arafah untuk melakukan Wukuf, merenung, bertasbih, dan berdoa memohon ampunan. Hari ini adalah hari paling utama di mana Allah membebaskan hamba dari api neraka.",
            "Rasulullah SAW bersabda: \"Puasa hari Arafah, aku berharap kepada Allah agar ia dapat menghapuskan dosa setahun yang lalu dan setahun yang akan datang.\" (HR. Muslim). Ini adalah puasa sunnah yang sangat dianjurkan bagi umat Islam yang sedang tidak berhaji."
        ),
        IslamicHoliday(
            "10-12", 
            "Hari Raya Idul Adha (10 Dzulhijjah)", 
            "Sholat Idul Adha dan berkurban bagi yang mampu sebagai keteladanan Nabi Ibrahim AS.",
            "Memperingati ketabahan dan kepatuhan luar biasa Nabi Ibrahim AS ketika diperintahkan Allah untuk menyembelih putra tercintanya, Nabi Ismail AS. Keikhlasan kedua nabi ini digantikan Allah dengan seekor kambing gibas besar dari surga, mengawali ketetapan ibadah kurban harian umat Islam sedunia.",
            "QS. Al-Kautsar: 2: \"Maka dirikanlah shalat karena Tuhanmu; dan berkurbanlah.\" Rasulullah SAW juga bersabda: \"Tidak ada amalan anak Adam pada Hari Raya Kurban yang lebih dicintai Allah daripada menyembelih dan mengalirkan darah hewan kurban.\" (HR. Tirmidzi)"
        )
    )

    /**
     * Mengambil daftar Hari Besar Islam lengkap untuk dicocokkan.
     */
    fun getIslamicHolidays(): List<IslamicHoliday> = holidays

    /**
     * Memeriksa apakah tanggal Hijriah tertentu ("hari" dan "bulan") bertepatan dengan Hari Besar Islam.
     * Mengembalikan objek `IslamicHoliday` atau `null` jika tidak ada.
     */
    fun checkHoliday(hijriDay: Int, hijriMonth: Int): IslamicHoliday? {
        val key = "%02d-%02d".format(hijriDay, hijriMonth)
        return holidays.find { it.hijriDate == key }
    }

    /**
     * Memeriksa apakah jendela waktu fitur musiman Ramadhan aktif (H-15 s/d H+15 Ramadhan).
     * Jendela aktif dari (1 Ramadhan - 15 hari) sampai dengan (1 Syawal + 15 hari) inklusif.
     */
    fun isRamadhanFeatureWindowActive(today: LocalDate = LocalDate.now()): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val currentHijrah = HijrahDate.from(today)
                val currentHijriYear = currentHijrah.get(ChronoField.YEAR)

                // Cek tahun berjalan dan tahun berikutnya untuk menangani edge case pergantian tahun
                val yearsToCheck = listOf(currentHijriYear, currentHijriYear + 1, currentHijriYear - 1)
                for (year in yearsToCheck) {
                    try {
                        val awalRamadhanHijrah = HijrahDate.of(year, 9, 1)
                        val awalSyawalHijrah = HijrahDate.of(year, 10, 1)

                        val awalRamadhanGregorian = LocalDate.ofEpochDay(awalRamadhanHijrah.toEpochDay())
                        val awalSyawalGregorian = LocalDate.ofEpochDay(awalSyawalHijrah.toEpochDay())

                        val windowStart = awalRamadhanGregorian.minusDays(15)
                        val windowEnd = awalSyawalGregorian.plusDays(15)

                        if (!today.isBefore(windowStart) && !today.isAfter(windowEnd)) {
                            return true
                        }
                    } catch (_: Throwable) {
                        // Lanjutkan ke kandidat tahun berikutnya
                    }
                }
                return false
            } catch (t: Throwable) {
                android.util.Log.e("HijriDateUtils", "Gagal memproses Java Time window Ramadhan: ${t.message}")
            }
        }

        // Fallback untuk perangkat API rendah
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.year)
            set(Calendar.MONTH, today.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
        }
        val hijriDate = convertToHijri(cal)
        return (hijriDate.month == 8 && hijriDate.day >= 14) ||
                (hijriDate.month == 9) ||
                (hijriDate.month == 10 && hijriDate.day <= 16)
    }

    /**
     * Memeriksa apakah hari ini persis tanggal 1 Ramadhan (hari pertama puasa).
     */
    fun isFirstDayOfRamadhan(today: LocalDate = LocalDate.now()): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val currentHijrah = HijrahDate.from(today)
                val currentHijriYear = currentHijrah.get(ChronoField.YEAR)
                val yearsToCheck = listOf(currentHijriYear, currentHijriYear + 1, currentHijriYear - 1)
                for (year in yearsToCheck) {
                    try {
                        val awalRamadhanHijrah = HijrahDate.of(year, 9, 1)
                        val awalRamadhanGregorian = LocalDate.ofEpochDay(awalRamadhanHijrah.toEpochDay())
                        if (today == awalRamadhanGregorian) {
                            return true
                        }
                    } catch (_: Throwable) {
                        // Coba tahun berikutnya jika gagal
                    }
                }
                return false
            } catch (t: Throwable) {
                android.util.Log.e("HijriDateUtils", "Gagal memproses isFirstDayOfRamadhan: ${t.message}")
            }
        }

        // Fallback untuk perangkat API rendah
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.year)
            set(Calendar.MONTH, today.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
        }
        val hijriDate = convertToHijri(cal)
        return hijriDate.month == 9 && hijriDate.day == 1
    }

    /**
     * Memeriksa apakah hari ini persis 5 hari sebelum 1 Syawal / Idul Fitri (H-5 Idul Fitri).
     */
    fun isFiveDaysBeforeEidFitr(today: LocalDate = LocalDate.now()): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val currentHijrah = HijrahDate.from(today)
                val currentHijriYear = currentHijrah.get(ChronoField.YEAR)
                val yearsToCheck = listOf(currentHijriYear, currentHijriYear + 1, currentHijriYear - 1)
                for (year in yearsToCheck) {
                    try {
                        val awalSyawalHijrah = HijrahDate.of(year, 10, 1)
                        val awalSyawalGregorian = LocalDate.ofEpochDay(awalSyawalHijrah.toEpochDay())
                        if (today == awalSyawalGregorian.minusDays(5)) {
                            return true
                        }
                    } catch (_: Throwable) {
                        // Coba tahun berikutnya jika gagal
                    }
                }
                return false
            } catch (t: Throwable) {
                android.util.Log.e("HijriDateUtils", "Gagal memproses isFiveDaysBeforeEidFitr: ${t.message}")
            }
        }

        // Fallback untuk perangkat API rendah
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.year)
            set(Calendar.MONTH, today.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
        }
        val hijriDate = convertToHijri(cal)
        return hijriDate.month == 9 && (hijriDate.day in 24..25)
    }

    /**
     * Mendapatkan tahun Hijriah saat ini secara aman.
     */
    fun getCurrentHijriYear(today: LocalDate = LocalDate.now()): Int {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                return HijrahDate.from(today).get(ChronoField.YEAR)
            } catch (_: Throwable) {
                // Gunakan fallback jika terjadi kesalahan
            }
        }
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.year)
            set(Calendar.MONTH, today.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
        }
        return convertToHijri(cal).year
    }

    /**
     * Konverter Hijriah Tabular Sederhana & Kokoh (Pure Math) sebagai Fallback Luring
     * Berjalan lancar di semua API Android tanpa butuh java.time atau desugaring.
     */
    private fun convertToHijriTabular(calendar: Calendar): HijriDate {
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Hitung Julian Day
        var y = gYear
        var m = gMonth
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        val jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + gDay + b - 1524.5

        val l = jd.toInt() - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val lOffset = l - 10631 * n + 354
        val j = (((10985 - lOffset) / 5316).toInt() * ((50 * lOffset + 46) / 17719).toInt() + 
                 ((lOffset / 5670).toInt() * ((43 * lOffset + 152) / 15238).toInt()))
        val lRemaining = lOffset - ((30 * j + 29) / 30).toInt() + 30
        
        val hMonthNum = ((24 * lRemaining - 17) / 709).toInt()
        val hDay = lRemaining - ((30 * hMonthNum + 29) / 30).toInt() + 29
        val hYear = 30 * n + j - 30

        val months = listOf(
            "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir", 
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban", 
            "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
        )
        val monthIdx = (hMonthNum - 1).coerceIn(0, 11)
        val monthName = months[monthIdx]
        val displayState = "$hDay $monthName $hYear H"

        return HijriDate(
            day = hDay,
            month = hMonthNum.coerceIn(1, 12),
            year = hYear,
            formatted = displayState,
            monthName = monthName
        )
    }

    /**
     * Mengkonversi Calendar Gregorian ke tanggal Hijriah lengkap dengan nama bulan Indonesia.
     * Menggunakan Java Time API untuk akurasi optimal dengan fallback tabular luring yang aman ter-skema.
     */
    fun convertToHijri(calendar: Calendar): HijriDate {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val gYear = calendar.get(Calendar.YEAR)
                val gMonth = calendar.get(Calendar.MONTH) + 1
                val gDay = calendar.get(Calendar.DAY_OF_MONTH)

                // Konversi ke LocalDate kemudian HijrahDate
                val localDate = LocalDate.of(gYear, gMonth, gDay)
                val hijrahDate = HijrahDate.from(localDate)

                val hDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)
                val hMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
                val hYear = hijrahDate.get(ChronoField.YEAR)

                val months = listOf(
                    "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir", 
                    "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban", 
                    "Ramadhan", "Syawal", "Dzulqa'dah", "Dzulhijjah"
                )
                val monthName = months[(hMonth - 1).coerceIn(0, 11)]
                val displayState = "$hDay $monthName $hYear H"

                return HijriDate(
                    day = hDay,
                    month = hMonth,
                    year = hYear,
                    formatted = displayState,
                    monthName = monthName
                )
            } catch (t: Throwable) {
                android.util.Log.e("HijriDateUtils", "Gagal memproses Java Time, beralih ke tabular fallback: ${t.message}")
            }
        }
        return convertToHijriTabular(calendar)
    }

    /**
     * Membangun representasi matriks grid kalender dari bulan Hijriah tertentu (Isi 1 bulan),
     * lengkap dengan hari-hari padanan Masehi untuk memudahkan penggambaran tabel kalender.
     */
    fun getHijriMonthGrid(hMonth: Int, hYear: Int): List<HijriDayGridItem> {
        val items = ArrayList<HijriDayGridItem>()
        
        // 1. Dapatkan perkiraan tanggal Masehi yang bertepatan dengan tanggal 1 bulan Hijriah ini.
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        var totalDays = 29
        var initialized = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val hijrahDate = HijrahDate.of(hYear, hMonth, 1)
                val epochDay = hijrahDate.toEpochDay()
                val localDate = LocalDate.ofEpochDay(epochDay)
                cal.set(Calendar.YEAR, localDate.year)
                cal.set(Calendar.MONTH, localDate.monthValue - 1)
                cal.set(Calendar.DAY_OF_MONTH, localDate.dayOfMonth)
                totalDays = hijrahDate.lengthOfMonth()
                initialized = true
            } catch (t: Throwable) {
                android.util.Log.e("HijriDateUtils", "Gagal memuat grid tanggal HijrahDate.of: ${t.message}")
            }
        }

        if (!initialized) {
            // fallback kasar jika terjadi error atau tahun/bulan di luar batas atau API < 26
            var foundDate = false
            val searchCal = Calendar.getInstance()
            searchCal.add(Calendar.MONTH, -6) // Cari dari 6 bulan ke belakang
            
            for (i in 1..400) {
                val hd = convertToHijri(searchCal)
                if (hd.month == hMonth && hd.year == hYear && hd.day == 1) {
                    cal.timeInMillis = searchCal.timeInMillis
                    foundDate = true
                    break
                }
                searchCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (!foundDate) {
                cal.set(Calendar.DAY_OF_MONTH, 1)
            }

            val isLeap = (11 * hYear + 14) % 30 < 11
            totalDays = when {
                hMonth == 12 -> if (isLeap) 30 else 29
                hMonth % 2 == 1 -> 30
                else -> 29
            }
        }

        // Cari tahu hari pertama di grid (Sunday = 1, Monday = 2, dst)
        val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 (Minggu) sampai 7 (Sabtu)

        // Tambahkan sel kosong sebagai padding grid di awalan bulan jika tidak dimulai hari Minggu
        for (pad in 1 until startDayOfWeek) {
            items.add(HijriDayGridItem(isPadding = true))
        }

        // Isi hari-hari aktif bulan Hijriah ini
        for (day in 1..totalDays) {
            val hDateStr = "%02d-%02d".format(day, hMonth)
            val holiday = holidays.find { it.hijriDate == hDateStr }
            val mDay = cal.get(Calendar.DAY_OF_MONTH)
            val mMonthIdx = cal.get(Calendar.MONTH)
            val mShortMonths = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agt", "Sep", "Okt", "Nov", "Des")
            val mMonthLabel = mShortMonths[mMonthIdx.coerceIn(0, 11)]
            val mDayLabelString = "$mDay $mMonthLabel"
            
            items.add(
                HijriDayGridItem(
                    isPadding = false,
                    hDay = day,
                    hMonth = hMonth,
                    hYear = hYear,
                    mDayLabel = mDayLabelString,
                    holidayName = holiday?.name,
                    holidayDescription = holiday?.description
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return items
    }
}

/**
 * Model Data representasi terstruktur tanggal Hijriah
 */
data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val formatted: String,
    val monthName: String
)

/**
 * Representasi item kotak grid dalam tampilan Kalender Hijriah
 */
data class HijriDayGridItem(
    val isPadding: Boolean,
    val hDay: Int = 0,
    val hMonth: Int = 0,
    val hYear: Int = 0,
    val mDayLabel: String = "",
    val holidayName: String? = null,
    val holidayDescription: String? = null
)
