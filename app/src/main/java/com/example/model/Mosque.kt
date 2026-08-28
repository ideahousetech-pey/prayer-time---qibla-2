package id.ideahousetech.prayertime_qibla.model

import java.util.Locale

/**
 * Representasi data Masjid / Musholla terdekat hasil pencarian real Google Places API.
 */
data class Mosque(
    val placeId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double,
    val rating: Double?,
    val isOpen: Boolean?
) {
    /**
     * Memformat jarak agar ramah pengguna (contoh: "450 m" atau "1.2 km").
     */
    val formattedDistance: String
        get() = if (distanceMeters >= 1000) {
            String.format(Locale.US, "%.1f km", distanceMeters / 1000.0)
        } else {
            "${distanceMeters.toInt()} m"
        }
}

/**
 * Sealed class untuk melacak state pencarian masjid terdekat di UI secara reaktif.
 */
sealed class MosqueUiState {
    object Idle : MosqueUiState()
    object Loading : MosqueUiState()
    data class Success(val mosques: List<Mosque>) : MosqueUiState()
    data class Error(val message: String) : MosqueUiState()
    object Empty : MosqueUiState()
    object NoInternet : MosqueUiState()
    object NoPermission : MosqueUiState()
}
