package id.ideahousetech.prayertime_qibla.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Service untuk menangkap titik lokasi GPS (latitude, longitude) secara realtime.
 * Menggunakan FusedLocationProviderClient dari Play Services Location dengan standard suspend coroutine.
 * Melakukan reverse-geocoding untuk mengubah koordinat menjadi nama Kelurahan/Kecamatan & Kota.
 */
class LocationService(private val context: Context) {

    private val appContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Mengambil lokasi GPS aktif terakhir atau meminta pembaharuan lokasi secara realtime.
     * Menggunakan suspendCancellableCoroutine agar stabil dan aman dari thread leak.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            Log.d("LocationService", "Izin lokasi belum diberikan, membatalkan request GPS.")
            return null
        }

        return try {
            // BUG 3 Fix: FusedLocationProviderClient bisa hang jika GPS mati, tambahkan timeout maksimal 10 detik
            withTimeoutOrNull(10000L) {
                suspendCancellableCoroutine<Location?> { continuation ->
                    // BUG 2 Fix: Gunakan AtomicBoolean untuk menghindari race condition double resume
                    val isResumed = AtomicBoolean(false)
                    fun safeResume(loc: Location?) {
                        if (continuation.isActive && isResumed.compareAndSet(false, true)) {
                            continuation.resume(loc)
                        }
                    }

                    val cancellationTokenSource = CancellationTokenSource()
                    continuation.invokeOnCancellation {
                        try {
                            cancellationTokenSource.cancel()
                        } catch (e: Exception) {
                            Log.e("LocationService", "Gagal membatalkan CancellationTokenSource", e)
                        }
                    }

                    try {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                            cancellationTokenSource.token
                        ).addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                safeResume(task.result)
                            } else {
                                try {
                                    fusedLocationClient.lastLocation.addOnCompleteListener { lastTask ->
                                        if (lastTask.isSuccessful && lastTask.result != null) {
                                            safeResume(lastTask.result)
                                        } else {
                                            safeResume(null)
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("LocationService", "Gagal mengambil lastLocation: ${e.message}")
                                    safeResume(null)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LocationService", "Gagal mengambil lokasi GPS langsung: ${e.message}")
                        safeResume(null)
                    }
                }
            } ?: run {
                Log.w("LocationService", "getCurrentLocation timed out after 10s, falling back to lastLocation")
                getLastLocationFallback()
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Gagal mengambil lokasi: ${e.message}")
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocationFallback(): Location? = suspendCancellableCoroutine { continuation ->
        val isResumed = AtomicBoolean(false)
        fun safeResume(loc: Location?) {
            if (continuation.isActive && isResumed.compareAndSet(false, true)) {
                continuation.resume(loc)
            }
        }
        try {
            fusedLocationClient.lastLocation.addOnCompleteListener { lastTask ->
                if (lastTask.isSuccessful && lastTask.result != null) {
                    safeResume(lastTask.result)
                } else {
                    safeResume(null)
                }
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Gagal mengambil lastLocation fallback: ${e.message}")
            safeResume(null)
        }
    }

    /**
     * Memancarkan data lokasi baru secara realtime menggunakan callbackFlow dan FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission")
    fun locationUpdatesFlow(
        intervalMs: Long = 15000L,
        fastestIntervalMs: Long = 10000L,
        minDistanceMeters: Float = 50f
    ): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            Log.d("LocationService", "Izin lokasi belum diberikan untuk aliran update GPS.")
            channel.close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(fastestIntervalMs)
            .setMinUpdateDistanceMeters(minDistanceMeters)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                Log.d("LocationService", "Status ketersediaan GPS: ${availability.isLocationAvailable}")
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                android.os.Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e("LocationService", "Gagal melakukan request update lokasi: ${e.message}")
            close(e)
        }

        awaitClose {
            Log.d("LocationService", "Menghentikan update lokasi (awaitClose)")
            try {
                fusedLocationClient.removeLocationUpdates(callback)
            } catch (e: Exception) {
                Log.e("LocationService", "Gagal menghentikan update lokasi", e)
            }
        }
    }

    /**
     * Mengubah koordinat menjadi nama alamat administratif lengkap ("Kecamatan X, Kota Y").
     * Fallback apabila offline atau Geocoder gagal: koordinat GPS saat ini.
     */
    @Suppress("DEPRECATION")
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return try {
            // BUG 1 Fix: Batasi pengerjaan Geocoder maksimal 5 detik
            withTimeoutOrNull(5000L) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = suspendCancellableCoroutine<List<android.location.Address>> { continuation ->
                        val isResumed = AtomicBoolean(false)
                        val safeResume = { result: List<android.location.Address> ->
                            if (continuation.isActive && isResumed.compareAndSet(false, true)) {
                                continuation.resume(result)
                            }
                        }
                        try {
                            val geocoder = Geocoder(appContext, Locale("id", "ID"))
                            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                                safeResume(addresses)
                            }
                        } catch (e: Exception) {
                            safeResume(emptyList())
                        }
                    }
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val district = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: ""
                        val city = addr.subAdminArea ?: addr.adminArea ?: ""
                        formatAddressString(district, city)
                    } else {
                        "Lokasi tidak diketahui"
                    }
                } else {
                    // BUG 1 Fix: Panggil Geocoder di API < 33 menggunakan Dispatchers.IO untuk mencegah ANR
                    withContext(Dispatchers.IO) {
                        val geocoder = Geocoder(appContext, Locale("id", "ID"))
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val district = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: ""
                            val city = addr.subAdminArea ?: addr.adminArea ?: ""
                            formatAddressString(district, city)
                        } else {
                            "Lokasi tidak diketahui"
                        }
                    }
                }
            } ?: "Lat: %.4f, Lon: %.4f".format(Locale.US, latitude, longitude)
        } catch (e: Exception) {
            Log.e("LocationService", "Gagal memproses Geocoder: ${e.message}")
            "Lat: %.4f, Lon: %.4f".format(Locale.US, latitude, longitude)
        }
    }

    private fun formatAddressString(district: String, city: String): String {
        val clearDistrict = district.replace("Kecamatan ", "").replace("Kelurahan ", "").trim()
        val clearCity = city.replace("Kota ", "").replace("Kabupaten ", "").trim()
        
        return when {
            clearDistrict.isNotEmpty() && clearCity.isNotEmpty() -> "$clearDistrict, $clearCity"
            clearDistrict.isNotEmpty() -> clearDistrict
            clearCity.isNotEmpty() -> clearCity
            else -> "Lokasi tidak diketahui"
        }
    }
}
