package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.service.LocationService
import id.ideahousetech.prayertime_qibla.utils.getDouble
import id.ideahousetech.prayertime_qibla.utils.putDouble
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola titik integrasi GPS perangkat.
 * Mengotomatisasi izin baca koordinat, caching SharedPreferences,
 * serta memperbarui state nama lokasi ("Kecamatan X, Kota Y") secara realtime.
 */
class LocationViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val locationService = LocationService(appContext)
    private val prefs: SharedPreferences = appContext.getSharedPreferences("user_location_cache", Context.MODE_PRIVATE)

    // State lokasi koordinat
    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    // State nama wilayah administrative (format: "Menteng, Jakarta Pusat")
    private val _locationName = MutableStateFlow("Menunggu GPS...")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    // State loading status lokasi
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State untuk lokasi manual
    private val _isManualLocation = MutableStateFlow(false)
    val isManualLocation: StateFlow<Boolean> = _isManualLocation.asStateFlow()

    // State keaktifan tracking realtime
    private val _isTrackingActive = MutableStateFlow(false)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    private var trackingJob: kotlinx.coroutines.Job? = null
    private var lastTrackedLocation: Location? = null

    init {
        // Ambil status apakah terakhir kali diset mode manual
        _isManualLocation.value = prefs.getBoolean(PrefsKeys.IS_MANUAL_LOCATION, false)
        // Memuat lokasi cadangan terakhir dari shared_preference agar UI langsung terisi tanpa menunggu GPS lambat
        loadCachedLocation()
        // Mulai tracking otomatis jika tidak dalam mode manual
        if (!_isManualLocation.value) {
            startLocationTracking()
        }
    }

    private var refreshJob: kotlinx.coroutines.Job? = null
    private var lastRefreshTime = 0L

    /**
     * Memperoleh lokasi perangkat terkini secara realtime menggunakan FusedLocationProviderClient.
     * Setelah koordinat didapat, memicu reverse-geocoding untuk memperbarui nama lokasi,
     * lalu menyimpan properti baru tersebut di SharedPreferences.
     * Menggunakan cooldown 5 detik dan pembatalan job lama jika ada request baru berturut-turut.
     */
    fun refreshLocation() {
        if (_isManualLocation.value) {
            return
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < 5_000) {
            // Cooldown aktif 5 detik (dikurangi dari 30 detik untuk lebih responsif)
            return
        }

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val loc = locationService.getCurrentLocation()
                if (loc != null) {
                    processNewLocation(loc)
                    lastRefreshTime = System.currentTimeMillis()
                } else {
                    // Gunakan cache jika pembacaan GPS gagal
                    loadCachedLocation()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Memulai pemantauan lokasi secara realtime / berkelanjutan.
     */
    fun startLocationTracking() {
        if (_isManualLocation.value) {
            Log.d("LocationViewModel", "Batal memulai tracking: Mode manual aktif.")
            return
        }
        if (!locationService.hasLocationPermission()) {
            Log.d("LocationViewModel", "Izin lokasi belum diberikan, menunda tracking GPS.")
            return
        }
        if (_isTrackingActive.value || trackingJob?.isActive == true) {
            Log.d("LocationViewModel", "Tracking lokasi sudah berjalan aktif.")
            return
        }

        Log.d("LocationViewModel", "Memulai tracking lokasi GPS realtime...")
        trackingJob = viewModelScope.launch {
            _isTrackingActive.value = true
            locationService.locationUpdatesFlow()
                .catch { e ->
                    Log.w("LocationViewModel", "Pemberitahuan saat melacak lokasi: ${e.message}")
                    _isTrackingActive.value = false
                }
                .collect { newLocation ->
                    processNewLocation(newLocation)
                }
        }
    }

    /**
     * Menghentikan pelacakan lokasi berkelanjutan untuk menghemat baterai.
     */
    fun stopLocationTracking() {
        Log.d("LocationViewModel", "Menghentikan tracking lokasi GPS...")
        trackingJob?.cancel()
        trackingJob = null
        _isTrackingActive.value = false
    }

    /**
     * Memproses data lokasi baru yang masuk dari pelacak realtime.
     * Melakukan reverse geocoding hanya jika jarak pergeseran >= 200 meter untuk menghemat kuota.
     */
    suspend fun processNewLocation(newLocation: Location) {
        _userLocation.value = newLocation
        
        val lastLoc = lastTrackedLocation
        val distance = if (lastLoc != null) lastLoc.distanceTo(newLocation) else Float.MAX_VALUE
        
        // GEOCODE_THRESHOLD = 200 meter
        if (lastLoc == null || distance >= 200f) {
            Log.i("LocationViewModel", "Pindah lokasi signifikan ($distance m >= 200m). Melakukan reverse-geocoding.")
            val address = locationService.getAddressFromLocation(newLocation.latitude, newLocation.longitude)
            _locationName.value = address
            saveLocationToCache(newLocation.latitude, newLocation.longitude, address)
            lastTrackedLocation = newLocation
        } else {
            Log.i("LocationViewModel", "Pindah lokasi kecil ($distance m < 200m). Hanya menyimpan koordinat cache.")
            saveCoordinatesToCache(newLocation.latitude, newLocation.longitude)
        }
        
        _updateWidgetIfNeeded()
    }

    /**
     * Set lokasi kustom secara manual
     */
    fun setManualLocation(cityName: String, lat: Double, lon: Double) {
        stopLocationTracking()
        _isManualLocation.value = true
        _locationName.value = cityName
        val manualLoc = Location("manual").apply {
            latitude = lat
            longitude = lon
        }
        _userLocation.value = manualLoc
        
        prefs.edit().apply {
            putBoolean(PrefsKeys.IS_MANUAL_LOCATION, true)
            putDouble(PrefsKeys.CACHED_LAT, lat)
            putDouble(PrefsKeys.CACHED_LON, lon)
            putString(PrefsKeys.CACHED_ADDRESS, cityName)
            apply()
        }
        // Pastikan seluruh widget sinkron dengan data koordinat kustom yang baru diset manual
        _updateWidgetIfNeeded()
    }

    /**
     * Kembali ke mode GPS Otomatis
     */
    fun setAutoLocation() {
        _isManualLocation.value = false
        prefs.edit().putBoolean(PrefsKeys.IS_MANUAL_LOCATION, false).apply()
        startLocationTracking()
        refreshLocation()
    }

    /**
     * Menyimpan koordinat terakhir dan alamat teks ke Shared Preferences.
     */
    private fun saveLocationToCache(lat: Double, lon: Double, address: String) {
        prefs.edit().apply {
            putDouble(PrefsKeys.CACHED_LAT, lat)
            putDouble(PrefsKeys.CACHED_LON, lon)
            putString(PrefsKeys.CACHED_ADDRESS, address)
            apply()
        }
    }

    /**
     * Menyimpan koordinat saja ke Shared Preferences.
     */
    private fun saveCoordinatesToCache(lat: Double, lon: Double) {
        prefs.edit().apply {
            putDouble(PrefsKeys.CACHED_LAT, lat)
            putDouble(PrefsKeys.CACHED_LON, lon)
            apply()
        }
    }

    /**
     * Memperbarui seluruh widget dari satu titik operasi (Single Point Update)
     */
    private fun _updateWidgetIfNeeded() {
        id.ideahousetech.prayertime_qibla.widget.PrayerWidgetHelper.updateAllWidgets(appContext)
    }

    /**
     * Memuat koordinat cadangan dari Shared Preferences.
     * Default fallback ke Koordinat Jakarta Pusat jika cache kosong.
     */
    private fun loadCachedLocation() {
        val lat = prefs.getDouble(PrefsKeys.CACHED_LAT, -6.175115) // Monas Jakarta
        val lon = prefs.getDouble(PrefsKeys.CACHED_LON, 106.827157)
        val address = prefs.getString(PrefsKeys.CACHED_ADDRESS, "Menteng, Jakarta Pusat") ?: "Menteng, Jakarta Pusat"

        val mockLoc = Location("cached").apply {
            latitude = lat
            longitude = lon
        }
        _userLocation.value = mockLoc
        _locationName.value = address
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}

class LocationViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
