package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import id.ideahousetech.prayertime_qibla.service.QiblaService
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel untuk mengelola logika sensor kompas Kiblat secara aman dari leak memori.
 * Berperan sebagai jembatan penampung siklus hidup agar sensor tidak terus berjalan saat screen hancur / rotasi.
 */
class QiblaViewModel(context: Context) : ViewModel() {

    private val qiblaService = QiblaService(context.applicationContext)

    // Alirkan data azimuth dan akurasi sensor secara realtime
    val azimuthFlow: StateFlow<Float> = qiblaService.azimuthFlow
    val sensorAccuracy: StateFlow<Int> = qiblaService.sensorAccuracy

    /**
     * Memulai pendengaran sensor dari service kompas
     */
    fun startListening() {
        qiblaService.startListening()
    }

    /**
     * Berhenti mendengarkan sensor untuk menghemat daya
     */
    fun stopListening() {
        qiblaService.stopListening()
    }

    /**
     * Menghitung sudut kompas (bearing) menuju Ka'bah
     */
    fun calculateQiblaDirection(userLat: Double, userLon: Double): Double {
        return qiblaService.calculateQiblaDirection(userLat, userLon)
    }

    /**
     * Berhenti mendengarkan secara otomatis saat ViewModel dihancurkan
     */
    override fun onCleared() {
        super.onCleared()
        qiblaService.stopListening()
    }
}

class QiblaViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QiblaViewModel::class.java)) {
            return QiblaViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
