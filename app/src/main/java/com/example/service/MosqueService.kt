package id.ideahousetech.prayertime_qibla.service

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.ideahousetech.prayertime_qibla.BuildConfig
import id.ideahousetech.prayertime_qibla.model.Mosque
import id.ideahousetech.prayertime_qibla.service.dto.NearbySearchResponse
import id.ideahousetech.prayertime_qibla.service.dto.OverpassResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.*

/**
 * Service untuk mengelola pencarian Masjid terdekat.
 * Sumber data utama menggunakan OpenStreetMap Overpass API (gratis tanpa billing/API key),
 * dengan multi-server fallback dan fallback ke Mock Data jika koneksi offline.
 * Kode Google Places API tetap dipertahankan sebagai opsi cadangan di masa depan.
 */
class MosqueService {

    // --- Overpass API Configuration ---
    private val overpassBaseUrls = listOf(
        "https://overpass-api.de/",
        "https://overpass.kumi.systems/",
        "https://overpass.openstreetmap.ru/"
    )

    private val overpassMoshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val overpassOkHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(SecurityInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private val overpassApiClients: Map<String, OverpassApi> = overpassBaseUrls.associateWith { baseUrl ->
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(overpassOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(overpassMoshi))
            .build()
            .create(OverpassApi::class.java)
    }

    // --- Google Places API Configuration (Legacy/Cadangan) ---
    private val googleApi: GooglePlacesApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(SecurityInterceptor())
            .addInterceptor(logging)
            .hostnameVerifier { hostname, session ->
                val defaultVerifier = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier()
                defaultVerifier.verify(hostname, session) && (hostname == "maps.googleapis.com" || hostname.endsWith(".googleapis.com"))
            }
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        googleApi = retrofit.create(GooglePlacesApi::class.java)
    }

    /**
     * Fungsi utama pencarian masjid terdekat di sekitar koordinat lat/lon.
     * Alur:
     * 1. Coba pencarian dengan OpenStreetMap Overpass API (gratis tanpa API key).
     * 2. Jika hasil kosong atau semua base URL gagal/timeout, fallback ke Mock Data.
     */
    suspend fun searchNearbyMosques(lat: Double, lon: Double, initialRadius: Int = 3000): List<Mosque> {
        if (!isValidCoordinate(lat, lon)) {
            Log.e("MosqueService", "Koordinat tidak valid: $lat, $lon")
            return emptyList()
        }

        try {
            // 1. Coba cari masjid menggunakan OpenStreetMap Overpass API
            val overpassResults = searchNearbyMosquesOverpass(lat, lon, initialRadius)
            if (overpassResults.isNotEmpty()) {
                return overpassResults
            }

            // Jika radius awal (3000m) tidak menghasilkan apa-apa di Overpass, coba auto-expand ke 6000m
            if (initialRadius < 6000) {
                Log.i("MosqueService", "Mencoba ekspansi radius Overpass ke 6000m")
                val expandedResults = searchNearbyMosquesOverpass(lat, lon, 6000)
                if (expandedResults.isNotEmpty()) {
                    return expandedResults
                }
            }
        } catch (e: Exception) {
            Log.e("MosqueService", "Exception saat eksekusi Overpass API: ${e.message}", e)
        }

        // 2. Jika Overpass kosong atau gagal pada semua server, aktifkan Fallback Mock Data
        Log.i("MosqueService", "Overpass kosong atau gagal di semua server, mengaktifkan Fallback Mock Data")
        return generateMockMosques(lat, lon)
    }

    /**
     * Mencari masjid menggunakan OpenStreetMap Overpass API dengan dukungan failover antar-server.
     */
    suspend fun searchNearbyMosquesOverpass(lat: Double, lon: Double, radiusMeters: Int): List<Mosque> {
        // Overpass QL query: mencari node dan way dengan amenity=place_of_worship & religion=muslim
        val query = String.format(
            Locale.US,
            "[out:json][timeout:20];(node[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:%d,%.6f,%.6f);way[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](around:%d,%.6f,%.6f););out center;",
            radiusMeters, lat, lon,
            radiusMeters, lat, lon
        )

        for (baseUrl in overpassBaseUrls) {
            try {
                Log.d("MosqueService", "Mencoba query Overpass API ke: $baseUrl")
                val api = overpassApiClients[baseUrl] ?: continue
                val response = api.query(query)
                val elements = response.elements ?: emptyList()

                val mosques = elements.mapNotNull { element ->
                    val name = element.tags?.name?.trim()
                    if (name.isNullOrEmpty()) return@mapNotNull null

                    val (elementLat, elementLon) = when (element.type) {
                        "node" -> {
                            val nLat = element.lat
                            val nLon = element.lon
                            if (nLat != null && nLon != null) Pair(nLat, nLon) else null
                        }
                        "way" -> {
                            val center = element.center
                            if (center != null) Pair(center.lat, center.lon) else null
                        }
                        else -> null
                    } ?: return@mapNotNull null

                    val address = element.tags.addrFull?.ifBlank { null }
                        ?: element.tags.addrStreet?.ifBlank { null }
                        ?: "Alamat tidak tersedia"

                    val distance = haversineDistance(lat, lon, elementLat, elementLon)

                    Mosque(
                        placeId = "osm_${element.id}",
                        name = name,
                        address = address,
                        lat = elementLat,
                        lon = elementLon,
                        distanceMeters = distance,
                        rating = null,
                        isOpen = null,
                        isMockData = false
                    )
                }.sortedBy { it.distanceMeters }

                if (mosques.isNotEmpty()) {
                    Log.i("MosqueService", "Berhasil mendapatkan ${mosques.size} masjid dari Overpass ($baseUrl)")
                    return mosques
                } else {
                    Log.i("MosqueService", "Overpass ($baseUrl) mengembalikan 0 hasil masjid ber-nama")
                }
            } catch (e: Exception) {
                Log.w("MosqueService", "Gagal menghubungi Overpass API di $baseUrl: ${e.message}, mencoba server berikutnya...")
            }
        }

        Log.w("MosqueService", "Semua server Overpass API gagal diakses atau tidak ada hasil")
        return emptyList()
    }

    /**
     * Pencarian masjid menggunakan Google Places API (Legacy / Cadangan).
     * Disimpan untuk kemungkinan dipakai kembali di masa depan jika API Key Google Cloud diaktifkan.
     */
    suspend fun searchNearbyMosquesGoogle(lat: Double, lon: Double, initialRadius: Int = 3000): List<Mosque> {
        if (!isValidCoordinate(lat, lon)) {
            Log.e("MosqueService", "Koordinat tidak valid: $lat, $lon")
            return emptyList()
        }

        val apiKey = BuildConfig.GOOGLE_MAPS_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GOOGLE_MAPS_API_KEY" || apiKey.contains("AIzaSyBTvsd7ACUNZJwJaqJZDiBrr3FJ11xekAI")) {
            Log.i("MosqueService", "Google API Key tidak aktif, fallback ke Mock Data")
            return generateMockMosques(lat, lon)
        }

        var radius = initialRadius
        val locationQuery = "$lat,$lon"

        try {
            var response = googleApi.searchNearbyMosques(
                location = locationQuery,
                radius = radius,
                key = apiKey
            )

            if (response.status == "ZERO_RESULTS" && radius < 10000) {
                radius = 6000
                Log.i("MosqueService", "ZERO_RESULTS ditemukan di Google, mencoba ekspansi radius ke $radius m")
                response = googleApi.searchNearbyMosques(
                    location = locationQuery,
                    radius = radius,
                    key = apiKey
                )
            }

            if (response.status == "OK" && response.results != null) {
                return parseAndFilterResults(response, lat, lon)
            } else {
                Log.w("MosqueService", "Google API Status: ${response.status}. Msg: ${response.errorMessage}")
                return generateMockMosques(lat, lon)
            }
        } catch (e: Exception) {
            Log.e("MosqueService", "Exception Google Places API: ${e.message}", e)
            return generateMockMosques(lat, lon)
        }
    }

    /**
     * Memfilter hasil pencarian Google Places agar benar-benar merupakan Masjid/Musholla
     * dan menghitung jarak presisi client-side menggunakan rumus Haversine.
     */
    private fun parseAndFilterResults(response: NearbySearchResponse, userLat: Double, userLon: Double): List<Mosque> {
        val rawResults = response.results ?: return emptyList()

        return rawResults.filter { place ->
            val lowerName = place.name.lowercase()
            val isMosqueType = place.types?.contains("mosque") == true || place.types?.contains("place_of_worship") == true
            val hasMuslimKeywords = lowerName.contains("masjid") || 
                                    lowerName.contains("musholla") || 
                                    lowerName.contains("mosque") || 
                                    lowerName.contains("langgar") || 
                                    lowerName.contains("bait")
            
            isMosqueType || hasMuslimKeywords
        }.map { place ->
            val placeLat = place.geometry?.location?.lat ?: userLat
            val placeLon = place.geometry?.location?.lng ?: userLon
            val distance = haversineDistance(userLat, userLon, placeLat, placeLon)

            Mosque(
                placeId = place.placeId,
                name = place.name,
                address = place.vicinity ?: "Alamat tidak tersedia",
                lat = placeLat,
                lon = placeLon,
                distanceMeters = distance,
                rating = place.rating,
                isOpen = place.openingHours?.openNow,
                isMockData = false
            )
        }.sortedBy { it.distanceMeters }
    }

    /**
     * Rumus Haversine yang akurat untuk mengkalkulasi jarak 2 titik koordinat di Bumi (dalam satuan Meter).
     */
    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Jari-jari bumi dalam meter
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Validasi dasar koordinat GPS.
     */
    fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0 && (lat != 0.0 || lon != 0.0)
    }

    /**
     * Helper untuk membuat URL photo tempat berdasarkan photo reference dari API Google.
     */
    fun getPhotoUrl(photoReference: String, maxWidth: Int = 400): String {
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$maxWidth&photoreference=$photoReference&key=${BuildConfig.GOOGLE_MAPS_API_KEY}"
    }

    /**
     * Generator Mock Data realistis di sekitar koordinat user untuk testing saat offline / tanpa respon server.
     */
    private fun generateMockMosques(userLat: Double, userLon: Double): List<Mosque> {
        val mockTemplates = listOf(
            Triple("Masjid Jami' Al-Ikhlas", "Jl. Nurul Amal No. 12", Pair(0.0022, -0.0018)),
            Triple("Musholla Babussalam", "Kawasan Residensial Harmoni Blok C", Pair(-0.0015, 0.0031)),
            Triple("Masjid Agung Baiturrahman", "Jl. Protokol Raya Utama No. 45", Pair(0.0055, 0.0042)),
            Triple("Masjid Al-Muhajirin", "Perumahan Indah Mulia Sektor 3", Pair(-0.0062, -0.0035)),
            Triple("Musholla At-Taqwa", "Gg. Barakah, RT 04/RW 02", Pair(0.0009, 0.0012)),
            Triple("Masjid Raya Darussalam", "Pusat Niaga Terpadu Kav 8-10", Pair(0.0112, -0.0085))
        )

        return mockTemplates.mapIndexed { index, (name, address, offset) ->
            val placeLat = userLat + offset.first
            val placeLon = userLon + offset.second
            val distance = haversineDistance(userLat, userLon, placeLat, placeLon)

            Mosque(
                placeId = "mock_place_id_$index",
                name = name,
                address = address,
                lat = placeLat,
                lon = placeLon,
                distanceMeters = distance,
                rating = 4.2 + (index % 8) * 0.1, // Rating dinamis 4.2 s/d 4.9
                isOpen = index % 2 == 0, // Selang-seling buka / tutup
                isMockData = true
            )
        }.sortedBy { it.distanceMeters }
    }
}
