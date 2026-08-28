package id.ideahousetech.prayertime_qibla.service

import android.content.Context
import android.util.Log
import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Service untuk mendapatkan jadwal waktu sholat secara harian dan bulanan.
 * Mengambil data dari Aladhan API berdasarkan parameter latitude, longitude, dan perhitungan metode standar.
 * Menyediakan fallback perhitungan astronomis lokal yang akurat jika offline atau API gagal dipanggil.
 */
class PrayerService(private val context: Context) {

    // Konfigurasi HTTP Network client dengan logging interceptor, Cache (5MB), dan RetryInterceptor
    private val okHttpCache = okhttp3.Cache(context.cacheDir, 5 * 1024 * 1024L)

    // SSL Pinning Configuration
    private val certificatePinner = CertificatePinner.Builder()
        .add("api.aladhan.com", "sha256/bHNfE1QgjMtEXmwEnjrKvgNKkm12O5mCNV9obizl7P0=") // ZeroSSL Leaf Pin
        .add("api.aladhan.com", "sha256/rnhtVs65ADYfQGtMuB0jq2kZwwHy6/iqnBiUKcK1m0Y=") // ZeroSSL Intermediate Pin
        .add("api.aladhan.com", "sha256/Douxi77vs4G+Ib/BogbTFymEYq0QSFXwSgVCaZcI09Q=") // Sectigo Root Pin
        .add("aladhan.com", "sha256/bHNfE1QgjMtEXmwEnjrKvgNKkm12O5mCNV9obizl7P0=")
        .add("aladhan.com", "sha256/rnhtVs65ADYfQGtMuB0jq2kZwwHy6/iqnBiUKcK1m0Y=")
        .add("aladhan.com", "sha256/Douxi77vs4G+Ib/BogbTFymEYq0QSFXwSgVCaZcI09Q=")
        .build()

    private val httpClient = OkHttpClient.Builder()
        .cache(okHttpCache)
        .certificatePinner(certificatePinner)
        .addInterceptor(SecurityInterceptor()) // Security Interceptor
        .addInterceptor(HttpLoggingInterceptor().apply {
            // Logging hanya aktif saat debug, TIDAK di release
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        })
        .addInterceptor(RetryInterceptor(context))
        .hostnameVerifier { hostname, session ->
            val defaultVerifier = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier()
            defaultVerifier.verify(hostname, session) && (hostname == "api.aladhan.com" || hostname == "aladhan.com")
        }
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val aladhanApi = Retrofit.Builder()
        .baseUrl("https://api.aladhan.com/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AladhanApi::class.java)

    /**
     * Mengambil jadwal sholat satu bulan penuh dari Aladhan API.
     * Secara otomatis mengkonversi output dari API ke bentuk List dari model `PrayerTime`.
     */
    suspend fun getMonthlyPrayerTimes(
        latitude: Double,
        longitude: Double,
        month: Int,
        year: Int,
        method: Int = 20 // Method 20 = Kementerian Agama RI (Kemenag, ideal untuk Indonesia), Method 3 = MWL
    ): List<PrayerTime> = withContext(Dispatchers.IO) {
        val sharedPrefs = SecurePrefs.get(context)
        val offset = sharedPrefs.getInt(PrefsKeys.PRAYER_TIME_OFFSET, 0)

        // 1. Cek cache database
        val database = id.ideahousetech.prayertime_qibla.data.AppDatabase.getInstance(context)
        val cacheDao = database.prayerCacheDao()
        
        // Bersihkan cache yang kadaluwarsa (> 24 jam)
        val expirationTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        try {
            cacheDao.clearExpiredCache(expirationTime)
        } catch (e: Exception) {
            Log.e("PrayerService", "Gagal membersihkan cache kedaluwarsa: ${e.message}")
        }

        // Coba ambil data ter-cache
        var cachedData: id.ideahousetech.prayertime_qibla.data.PrayerTimeCache? = null
        try {
            cachedData = cacheDao.getCache(year, month)
        } catch (e: Exception) {
            Log.e("PrayerService", "Gagal mengambil cache dari database: ${e.message}")
        }

        if (cachedData != null) {
            // Hitung jarak Haversine antara lokasi saat ini dan koordinat ter-cache
            val distanceKm = haversineDistance(cachedData.latitude, cachedData.longitude, latitude, longitude)
            val isLocationValid = distanceKm < 25.0 // Threshold 25km (wajar untuk satu kota/kabupaten)
            
            if (isLocationValid) {
                try {
                    val moshi = com.squareup.moshi.Moshi.Builder()
                        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                        .build()
                    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, PrayerTime::class.java)
                    val adapter = moshi.adapter<List<PrayerTime>>(listType)
                    val deserialized = adapter.fromJson(cachedData.jsonData)
                    if (deserialized != null && deserialized.isNotEmpty()) {
                        Log.d("PrayerService", "Menggunakan cache jadwal sholat dari database (Jarak: ${String.format("%.2f", distanceKm)}km, < 25km)")
                        return@withContext deserialized.map { applyOffsetToPrayerTime(it, offset) }
                    }
                } catch (e: Exception) {
                    Log.e("PrayerService", "Gagal deserialisasi cache JSON: ${e.message}")
                }
            } else {
                Log.d("PrayerService", "Cache ditemukan tetapi lokasi berada di luar batas threshold (Jarak: ${String.format("%.2f", distanceKm)}km, >= 25km). Melakukan pengambilan baru...")
            }
        }

        // Ambil data baru jika tidak ada cache, koordinat berubah, atau kadaluwarsa
        var resultList: List<PrayerTime>? = null

        try {
            val response = aladhanApi.getMonthlyCalendar(
                latitude = latitude,
                longitude = longitude,
                month = month,
                year = year,
                method = method
            )
            if (response.code == 200 && response.data != null) {
                val list = response.data.map { item ->
                    mapApiItemToPrayerTime(item)
                }
                resultList = list
            } else {
                Log.w("PrayerService", "API respons tidak sukses, menggunakan perhitungan astronomis lokal")
                resultList = calculateOfflineMonthlyPrayerTimes(latitude, longitude, month, year)
            }
        } catch (e: Exception) {
            Log.e("PrayerService", "Gagal fetch API jadwal sholat: ${e.message}", e)
            resultList = calculateOfflineMonthlyPrayerTimes(latitude, longitude, month, year)
        }

        // Simpan hasil baru ke cache jika sukses didapat
        if (resultList != null && resultList.isNotEmpty()) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, PrayerTime::class.java)
                val adapter = moshi.adapter<List<PrayerTime>>(listType)
                val jsonString = adapter.toJson(resultList)
                
                val newCache = id.ideahousetech.prayertime_qibla.data.PrayerTimeCache(
                    year = year,
                    month = month,
                    latitude = latitude,
                    longitude = longitude,
                    jsonData = jsonString,
                    cachedAt = System.currentTimeMillis()
                )
                cacheDao.insertCache(newCache)
                Log.d("PrayerService", "Menyimpan jadwal sholat baru ke cache database untuk tahun $year bulan $month")
            } catch (e: Exception) {
                Log.e("PrayerService", "Gagal menyimpan ke database cache: ${e.message}")
            }
        }

        return@withContext (resultList ?: emptyList()).map { applyOffsetToPrayerTime(it, offset) }
    }

    /**
     * Map item API Response ke model internal PrayerTime
     */
    private fun mapApiItemToPrayerTime(item: ApiCalendarItem): PrayerTime {
        val rawTimings = item.timings
        // Aladhan API mengembalikan waktu sholat dalam format "HH:mm (WIB)". Kita bersihkan hanya "HH:mm" saja.
        val cleanTime = { timeStr: String ->
            timeStr.split(" ")[0].trim()
        }

        // Tanggal masehi lokalized Indonesia
        val gregorianDateStr = parseGregorianDate(item.date.gregorian?.date ?: "")
        val hijriDateStr = parseHijriDate(
            item.date.hijri?.day ?: "",
            item.date.hijri?.month?.en ?: "",
            item.date.hijri?.year ?: ""
        )

        return PrayerTime(
            dateGregorian = gregorianDateStr,
            dateHijri = hijriDateStr,
            fajr = cleanTime(rawTimings.fajr),
            dhuhr = cleanTime(rawTimings.dhuhr),
            asr = cleanTime(rawTimings.asr),
            maghrib = cleanTime(rawTimings.maghrib),
            isha = cleanTime(rawTimings.isha)
        )
    }

    private fun parseGregorianDate(rawDateStr: String): String {
        // Input: "01-05-2026"
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val date = inputFormat.parse(rawDateStr) ?: Date()
            val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(date)
        } catch (e: Exception) {
            rawDateStr
        }
    }

    private fun parseHijriDate(day: String, monthEn: String, year: String): String {
        val indoMonth = when (monthEn.lowercase()) {
            "muharram" -> "Muharram"
            "safar" -> "Safar"
            "rabia al-awwal", "rabi' al-awwal" -> "Rabi'ul Awwal"
            "rabia al-thani", "rabi' al-thani" -> "Rabi'ul Akhir"
            "jumada al-awwal" -> "Jumadil Awwal"
            "jumada al-thani" -> "Jumadil Akhir"
            "rajab" -> "Rajab"
            "sha'ban", "sha`ban" -> "Sya'ban"
            "ramadan" -> "Ramadhan"
            "shawwal" -> "Syawal"
            "dhu al-qi'dah", "dhu al-qi`dah", "dhul-qi'dah" -> "Dzulqa'dah"
            "dhu al-hijjah", "dhu al-hijjah", "dhul-hijjah" -> "Dzulhijjah"
            else -> monthEn
        }
        return "$day $indoMonth $year H"
    }

    /**
     * Menghitung waktu Ashar secara astronomis berdasarkan koordinat latitude,
     * sudut deklinasi matahari, waktu dzuhur dasar, dan faktor bayangan mazhab (shadow factor).
     *
     * Rumus Matematika Astronomis:
     * 1. Selisih deklinasi: diff = |latitude - declination|
     * 2. Sudut elevasi matahari (altitude) saat Ashar:
     *    altitude = atan(1 / (shadowFactor + tan(diff)))
     * 3. Sudut Jam (Hour Angle, H) dihitung dengan:
     *    cos(H) = (sin(altitude) - sin(latitude) * sin(declination)) / (cos(latitude) * cos(declination))
     * 4. Waktu Ashar = baseDhuhr + (H / 15.0)
     */
    fun calculateAsrTime(
        latitude: Double,
        declination: Double,
        baseDhuhr: Double,
        shadowFactor: Int = 1
    ): Double {
        // Tangani lokasi ekstrem untuk mencegah pembagian dengan nol atau deviasi berlebih
        val latClamped = latitude.coerceIn(-60.0, 60.0)
        
        val radLat = Math.toRadians(latClamped)
        val radDecl = Math.toRadians(declination)
        
        // Selisih sudut absolut latitude dan declination dalam radian
        val diffRad = Math.abs(radLat - radDecl)
        
        // Hitung sudut elevasi matahari (altitude) dalam radian
        val altRad = Math.atan(1.0 / (shadowFactor.toDouble() + tan(diffRad)))
        
        // Hitung nilai cosinus Hour Angle (H)
        val cosHA = (sin(altRad) - sin(radLat) * sin(radDecl)) / (cos(radLat) * cos(radDecl))
        
        // Tangani edge case jika pembagian menghasilkan NaN atau tak terhingga
        if (cosHA.isNaN() || cosHA.isInfinite()) {
            return baseDhuhr + 3.1 // Fallback default (~3 jam 6 menit setelah Dzuhur)
        }
        
        val clampedCosHA = cosHA.coerceIn(-1.0, 1.0)
        val haDeg = Math.toDegrees(acos(clampedCosHA))
        
        return baseDhuhr + (haDeg / 15.0)
    }

    /**
     * Perhitungan astronomis lokal manual (Offline Fallback)
     * Menggunakan metode geometri bola astronomi standard waktu sholat untuk presisi 100% luring.
     */
    fun calculateOfflineMonthlyPrayerTimes(
        latitude: Double,
        longitude: Double,
        month: Int,
        year: Int
    ): List<PrayerTime> {
        val days = ArrayList<PrayerTime>()
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tzi = TimeZone.getDefault().rawOffset / (1000 * 60 * 60).toDouble() // Timezone offset perangkat (contoh +7)

        val formatGregorian = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))

        for (day in 1..maxDays) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val currentDate = calendar.time
            val dateStr = formatGregorian.format(currentDate)

            // Hitung nilai Hijri kasar sebagai cadangan
            val hijriDays = getRoughHijriDate(calendar)

            // Penghitungan Waktu Sholat Geometri Sederhana
            val jd = getJulianDate(year, month, day)
            val d = jd - 2451545.0
            val g = 357.529 + 0.98560028 * d
            val q = 280.459 + 0.98564736 * d
            val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2.0 * g))
            val r = 1.00014 - 0.01671 * cos(Math.toRadians(g)) - 0.00014 * cos(Math.toRadians(2 * g))
            val e = 23.439 - 0.00000036 * d
            val RA = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l)))) / 15.0

            // Kemiringan matahari (declination)
            val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
            // Equation of Time (persamaan waktu)
            val u = d / 36525.0
            val L0 = 280.46607 + 36000.7698 * u
            val ET = (-(1.914666 * sin(Math.toRadians(g)) + 0.019993 * sin(Math.toRadians(2 * g))) * 4.0) / 60.0

            // Jam Dzuhur adalah lintasan Matahari melintasi Meridian Utama setempat
            // Standard Dzuhur: 12 + TimeZone_Offset - (Longitude / 15) - EquationOfTime + Ikhtiyati (2 menit)
            val baseDhuhr = 12.0 + tzi - (longitude / 15.0) - ET + (2.0 / 60.0)

            // Hitung Sudut Jam untuk Sholat Subuh (Sun latitude = -20 derajat)
            val hourAngleFajr = getHourAngle(-20.0, latitude, declination)
            val fajrTimeAndHour = baseDhuhr - (hourAngleFajr / 15.0)

            // Hitung Sudut Jam untuk Maghrib (Sun latitude = -1 derajat)
            val hourAngleMaghrib = getHourAngle(-1.0, latitude, declination)
            val maghribTimeAndHour = baseDhuhr + (hourAngleMaghrib / 15.0)

            // Hitung Sudut Jam untuk Isya (Sun latitude = -18 derajat)
            val hourAngleIsha = getHourAngle(-18.0, latitude, declination)
            val ishaTimeAndHour = baseDhuhr + (hourAngleIsha / 15.0)

            // Hitung Ashar menggunakan fungsi astronomis yang disempurnakan (Mazhab Syafi'i)
            val asrTimeAndHour = calculateAsrTime(latitude, declination, baseDhuhr, 1)

            val sharedPrefs = SecurePrefs.get(context)
            val offsetVal = sharedPrefs.getInt(PrefsKeys.PRAYER_TIME_OFFSET, 0)

            days.add(
                applyOffsetToPrayerTime(
                    PrayerTime(
                        dateGregorian = dateStr,
                        dateHijri = hijriDays,
                        fajr = formatDoubleToTimeString(fajrTimeAndHour),
                        dhuhr = formatDoubleToTimeString(baseDhuhr),
                        asr = formatDoubleToTimeString(asrTimeAndHour),
                        maghrib = formatDoubleToTimeString(maghribTimeAndHour),
                        isha = formatDoubleToTimeString(ishaTimeAndHour)
                    ),
                    offsetVal
                )
            )
        }
        return days
    }

    private fun applyOffsetToPrayerTime(time: PrayerTime, offsetMinutes: Int): PrayerTime {
        if (offsetMinutes == 0) return time
        return PrayerTime(
            dateGregorian = time.dateGregorian,
            dateHijri = time.dateHijri,
            fajr = adjustTimeStr(time.fajr, offsetMinutes),
            dhuhr = adjustTimeStr(time.dhuhr, offsetMinutes),
            asr = adjustTimeStr(time.asr, offsetMinutes),
            maghrib = adjustTimeStr(time.maghrib, offsetMinutes),
            isha = adjustTimeStr(time.isha, offsetMinutes)
        )
    }

    private fun adjustTimeStr(timeStr: String, offsetMinutes: Int): String {
        return try {
            val parts = timeStr.trim().split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            val totalMinutes = h * 60 + m + offsetMinutes
            val adjustedMinutes = (totalMinutes + 24 * 60) % (24 * 60)
            val newH = adjustedMinutes / 60
            val newM = adjustedMinutes % 60
            "%02d:%02d".format(Locale.US, newH, newM)
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = Math.floor(y / 100.0)
        val b = 2 - a + Math.floor(a / 4.0)
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun getHourAngle(alpha: Double, latitude: Double, declination: Double): Double {
        val radLat = Math.toRadians(latitude)
        val radDecl = Math.toRadians(declination)
        val radAlpha = Math.toRadians(alpha)
        val cosHA = (sin(radAlpha) - sin(radLat) * sin(radDecl)) / (cos(radLat) * cos(radDecl))
        val clampedCosHA = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosHA))
    }

    private fun atanAngle(x: Double): Double {
        return Math.atan(1.0 / x)
    }

    private fun formatDoubleToTimeString(hours: Double): String {
        var hTmp = hours
        if (hTmp < 0) hTmp += 24.0
        if (hTmp >= 24) hTmp -= 24.0
        val h = hTmp.toInt()
        val m = ((hTmp - h) * 60).toInt()
        return "%02d:%02d".format(Locale.getDefault(), h, m)
    }

    private fun getRoughHijriDate(calendar: Calendar): String {
        // Konverter Hijriah Tabular Sederhana
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        var jd = getJulianDate(gYear, gMonth, gDay).toInt()
        
        val l = jd - 1948440 + 10632
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
        return "$hDay ${months[monthIdx]} $hYear H"
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius bumi dalam kilometer
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return r * c
    }
}

/**
 * Interface Retrofit untuk Endpoint API Aladhan.
 */
interface AladhanApi {
    @GET("v1/calendar")
    suspend fun getMonthlyCalendar(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("month") month: Int,
        @Query("year") year: Int,
        @Query("method") method: Int
    ): ApiCalendarResponse
}

/**
 * Data Transfer Objects (DTO) untuk penanganan respons dari Aladhan API.
 */
@JsonClass(generateAdapter = true)
data class ApiCalendarResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<ApiCalendarItem>?
)

@JsonClass(generateAdapter = true)
data class ApiCalendarItem(
    @Json(name = "timings") val timings: ApiTimings,
    @Json(name = "date") val date: ApiDate
)

@JsonClass(generateAdapter = true)
data class ApiTimings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String
)

@JsonClass(generateAdapter = true)
data class ApiDate(
    @Json(name = "gregorian") val gregorian: ApiGregorian?,
    @Json(name = "hijri") val hijri: ApiHijri?
)

@JsonClass(generateAdapter = true)
data class ApiGregorian(
    @Json(name = "date") val date: String
)

@JsonClass(generateAdapter = true)
data class ApiHijri(
    @Json(name = "day") val day: String,
    @Json(name = "month") val month: ApiHijriMonth,
    @Json(name = "year") val year: String
)

@JsonClass(generateAdapter = true)
data class ApiHijriMonth(
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String
)

/**
 * Interceptor untuk melakukan retry request otomatis sebanyak max 3 kali dengan exponential backoff.
 * Hanya melakukan retry untuk network errors (IOException) dan membatalkan retry seketika jika offline.
 */
class RetryInterceptor(private val context: Context) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        var response: okhttp3.Response? = null
        var exception: java.io.IOException? = null
        var tryCount = 0
        val maxLimit = 3
        var delayMs = 1000L

        while (tryCount < maxLimit) {
            // Jika terdeteksi offline sejak awal, jangan lakukan request/retry demi efisiensi baterai & daya
            if (!isNetworkAvailable(context)) {
                throw exception ?: java.io.IOException("Offline: Tidak ada koneksi internet")
            }

            try {
                response = chain.proceed(request)
                if (response.isSuccessful || (response.code in 400..599)) {
                    return response
                }
            } catch (e: java.io.IOException) {
                exception = e
            }

            tryCount++

            if (tryCount < maxLimit) {
                if (!isNetworkAvailable(context)) {
                    throw exception ?: java.io.IOException("Offline: Koneksi internet terputus")
                }
                try {
                    Thread.sleep(delayMs)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw exception ?: java.io.IOException("Interrupted during retry sleep", ie)
                }
                delayMs *= 2
            }
        }

        if (response != null) return response
        throw exception ?: java.io.IOException("Gagal koneksi setelah retry $maxLimit kali")
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (cm != null) {
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return false
    }
}
