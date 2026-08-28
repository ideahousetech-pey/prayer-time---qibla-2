package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.model.Mosque
import id.ideahousetech.prayertime_qibla.model.MosqueUiState
import id.ideahousetech.prayertime_qibla.service.MosqueService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk mengelola daftar masjid terdekat, melacak status pencarian,
 * serta menangani optimasi batasan API (cooldown 15s & threshold jarak 100m).
 */
class ExploreViewModel(
    private val context: Context,
    private val mosqueService: MosqueService = MosqueService()
) : ViewModel() {

    private val _mosqueState = MutableStateFlow<MosqueUiState>(MosqueUiState.Idle)
    val mosqueState: StateFlow<MosqueUiState> = _mosqueState.asStateFlow()

    private val _searchRadius = MutableStateFlow(3000) // Default radius 3000 meter
    val searchRadius: StateFlow<Int> = _searchRadius.asStateFlow()

    private var lastSearchTime = 0L
    private var lastSearchLocation: Pair<Double, Double>? = null

    /**
     * Memulai proses pencarian masjid berdasarkan koordinat GPS saat ini.
     * Mengimplementasikan optimasi cooldown 15 detik & threshold pergerakan 100 meter.
     */
    fun searchMosques(lat: Double, lon: Double, forceRefresh: Boolean = false) {
        if (!mosqueService.isValidCoordinate(lat, lon)) {
            _mosqueState.value = MosqueUiState.Idle
            return
        }

        val currentTime = System.currentTimeMillis()

        // 1. Cek Internet
        if (!isNetworkAvailable()) {
            _mosqueState.value = MosqueUiState.NoInternet
            return
        }

        // 2. Optimasi Cooldown (15 detik) jika tidak dipaksa refresh
        if (!forceRefresh && (currentTime - lastSearchTime < 15_000)) {
            Log.i("ExploreViewModel", "Pencarian diabaikan: Cooldown 15 detik aktif. Sisa waktu: ${(15_000 - (currentTime - lastSearchTime)) / 1000}s")
            return
        }

        // 3. Optimasi Jarak Threshold (100 meter) jika tidak dipaksa refresh
        val lastLoc = lastSearchLocation
        if (!forceRefresh && lastLoc != null) {
            val distanceMoved = mosqueService.haversineDistance(lastLoc.first, lastLoc.second, lat, lon)
            Log.d("ExploreViewModel", "Pengguna bergeser sejauh: $distanceMoved meter dari pencarian sebelumnya.")
            if (distanceMoved < 100.0 && (_mosqueState.value is MosqueUiState.Success || _mosqueState.value is MosqueUiState.Empty)) {
                Log.i("ExploreViewModel", "Pencarian diabaikan: User bergeser kurang dari 100m ($distanceMoved m).")
                return
            }
        }

        // Jalankan pencarian
        viewModelScope.launch {
            _mosqueState.value = MosqueUiState.Loading
            try {
                Log.d("ExploreViewModel", "Mengeksekusi pencarian masjid terdekat pada koordinat ($lat, $lon) radius ${_searchRadius.value} m...")
                val list = mosqueService.searchNearbyMosques(lat, lon, _searchRadius.value)
                
                // FIX BUG: lastSearchTime dan lastSearchLocation HARUS diupdate baik saat Success MAUPUN Empty
                lastSearchLocation = Pair(lat, lon)
                lastSearchTime = System.currentTimeMillis()
                
                if (list.isEmpty()) {
                    Log.d("ExploreViewModel", "Hasil pencarian kosong.")
                    _mosqueState.value = MosqueUiState.Empty
                } else {
                    Log.d("ExploreViewModel", "Berhasil menemukan ${list.size} masjid.")
                    _mosqueState.value = MosqueUiState.Success(list)
                }
            } catch (e: Exception) {
                Log.e("ExploreViewModel", "Error fetching mosques", e)
                _mosqueState.value = MosqueUiState.Error(e.localizedMessage ?: "Terjadi kesalahan tidak dikenal")
            }
        }
    }

    /**
     * Memaksimalkan radius pencarian secara manual dan memicu pencarian ulang.
     */
    fun expandSearchRadius(lat: Double, lon: Double) {
        val currentRadius = _searchRadius.value
        if (currentRadius < 10000) {
            _searchRadius.value = currentRadius + 2000
            Log.i("ExploreViewModel", "Mengekspansi radius ke ${_searchRadius.value} meter")
            searchMosques(lat, lon, forceRefresh = true)
        }
    }

    /**
     * Mengulang pencarian terakhir secara paksa (force refresh).
     */
    fun retry(lat: Double, lon: Double) {
        _searchRadius.value = 3000 // Reset ke default
        searchMosques(lat, lon, forceRefresh = true)
    }

    /**
     * Set State ke NoPermission jika izin GPS ditolak oleh pengguna.
     */
    fun setNoPermissionState() {
        _mosqueState.value = MosqueUiState.NoPermission
    }

    /**
     * Helper terintegrasi untuk mengecek jaringan internet perangkat.
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}

/**
 * Factory untuk instansiasi ExploreViewModel secara aman tanpa kebocoran memori Context.
 */
class ExploreViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
            return ExploreViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
