package id.ideahousetech.prayertime_qibla.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Service untuk mengukur azimuth arah perangkat (kompas) & menghitung bearing arah Ka'bah (Kiblat).
 * Menggunakan sensor Magnetometer dan Accelerometer untuk menghitung posisi sudut rotasi (azimuth).
 * Dilengkapi dengan rumus trigonometri bola koordinat Ka'bah di Makkah (21.4225 N, 39.8262 E).
 */
class QiblaService(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Menyimpan pembacaan sensor
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // State penyaringan Low-Pass Filter berbasis vektor 2D
    private var smoothedCos = 0.0
    private var smoothedSin = 0.0
    private var hasSmoothed = false
    private val ALPHA = 0.15f // Koefisien penyaringan (makin kecil makin halus, rekomendasi: 0.1 - 0.3)

    // State flow untuk memancarkan arah hadap perangkat (azimuth) secara realtime ke UI Compose
    private val _azimuthFlow = MutableStateFlow(0f)
    val azimuthFlow: StateFlow<Float> = _azimuthFlow

    // StateFlow akurasi magnetometer (Default: SENSOR_STATUS_ACCURACY_HIGH = 3)
    private val _sensorAccuracy = MutableStateFlow(3)
    val sensorAccuracy: StateFlow<Int> = _sensorAccuracy

    /**
     * Koordinat Ka'bah di Masjidil Haram, Makkah
     */
    companion object {
        const val KABAH_LATITUDE = 21.422487
        const val KABAH_LONGITUDE = 39.826206
    }

    /**
     * Mulai mendengarkan pembacaan sensor orientasi
     */
    fun startListening() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Berhenti mendengarkan sensor untuk menghemat pemakaian baterai perangkat
     */
    fun stopListening() {
        sensorManager.unregisterListener(this)
        hasGravity = false
        hasGeomagnetic = false
        hasSmoothed = false
    }

    /**
     * Rumus Matematika Bola Navigasi Geometri:
     * Menghitung sudut arah (bearing) menuju Ka'bah dari titik koordinat GPS saat ini.
     * Mengembalikan sudut dalam derajat (0 - 360) terhadap arah Utara geografis.
     */
    fun calculateQiblaDirection(userLat: Double, userLon: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLonRad = Math.toRadians(userLon)
        val kabahLatRad = Math.toRadians(KABAH_LATITUDE)
        val kabahLonRad = Math.toRadians(KABAH_LONGITUDE)

        val deltaLon = kabahLonRad - userLonRad

        val y = sin(deltaLon)
        val x = cos(userLatRad) * tan(kabahLatRad) - sin(userLatRad) * cos(deltaLon)

        val qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)

        // Ubah kisaran -180...180 menjadi format arah kompas 0...360 derajat
        qiblaDeg = (qiblaDeg + 360.0) % 360.0
        return qiblaDeg
    }

    // Callbacks SensorEventListener
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        try {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val copySize = minOf(event.values.size, gravity.size)
                System.arraycopy(event.values, 0, gravity, 0, copySize)
                hasGravity = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                val copySize = minOf(event.values.size, geomagnetic.size)
                System.arraycopy(event.values, 0, geomagnetic, 0, copySize)
                hasGeomagnetic = true
            }

            if (hasGravity && hasGeomagnetic) {
                val rMatrix = FloatArray(9)
                val iMatrix = FloatArray(9)
                if (SensorManager.getRotationMatrix(rMatrix, iMatrix, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rMatrix, orientation)
                    
                    // Menggunakan Low-Pass Filter berbasis vektor 2D untuk memuluskan pembacaan azimuth kompas
                    // tanpa efek patah (discontinuity) saat melewati batas sudut 0/360 derajat.
                    val rad = orientation[0].toDouble()
                    val currentCos = cos(rad)
                    val currentSin = sin(rad)

                    if (!hasSmoothed) {
                        smoothedCos = currentCos
                        smoothedSin = currentSin
                        hasSmoothed = true
                    } else {
                        smoothedCos = ALPHA * currentCos + (1.0 - ALPHA) * smoothedCos
                        smoothedSin = ALPHA * currentSin + (1.0 - ALPHA) * smoothedSin
                    }

                    val smoothedRad = atan2(smoothedSin, smoothedCos)
                    var azimuthDeg = Math.toDegrees(smoothedRad).toFloat()
                    azimuthDeg = (azimuthDeg + 360f) % 360f
                    
                    // Dead zone filter: jika perubahan sudut sangat kecil (< 1 derajat), abaikan update untuk meredam noise sensor
                    val prevAzimuth = _azimuthFlow.value
                    var diff = kotlin.math.abs(azimuthDeg - prevAzimuth)
                    if (diff > 180f) {
                        diff = 360f - diff
                    }
                    if (diff >= 1f) {
                        _azimuthFlow.value = azimuthDeg
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("QiblaService", "Gagal melakukan asimilasi data sensor kompas: ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _sensorAccuracy.value = accuracy
        }
    }
}
