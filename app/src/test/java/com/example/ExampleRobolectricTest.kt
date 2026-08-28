package id.ideahousetech.prayertime_qibla

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.utils.getDouble
import id.ideahousetech.prayertime_qibla.utils.putDouble
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Prayer Times & Qibla", appName)
  }

  @Test
  fun `test calculateAsrTime for Jakarta`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prayerService = id.ideahousetech.prayertime_qibla.service.PrayerService(context)
    
    // Koordinat Jakarta (-6.2, 106.8)
    val latitude = -6.2
    val declination = 0.0 // Ekuinoks
    val baseDhuhr = 12.0
    
    val asrTime = prayerService.calculateAsrTime(latitude, declination, baseDhuhr, shadowFactor = 1)
    
    // Hasil asrTime dalam jam desimal, misalnya 15.176 (sekitar 15:10 WIB)
    println("Hasil perhitungan waktu Ashar Jakarta: $asrTime")
    org.junit.Assert.assertTrue(
      "Waktu Ashar Jakarta ($asrTime) harus berkisar antara 15:00 (15.0) dan 15:30 (15.5) WIB",
      asrTime in 15.0..15.5
    )
  }

  @Test
  fun `test SharedPreferences putDouble and getDouble precision and backward compatibility`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)

    val inputLat = -6.175115191234567
    val inputLon = 106.82715712345678

    // Test saving as Double and retrieving
    prefs.edit()
      .putDouble("test_lat", inputLat)
      .putDouble("test_lon", inputLon)
      .apply()

    val outputLat = prefs.getDouble("test_lat", 0.0)
    val outputLon = prefs.getDouble("test_lon", 0.0)

    // Verify precision is 100% preserved
    assertEquals(inputLat, outputLat, 0.0)
    assertEquals(inputLon, outputLon, 0.0)

    // Test backward compatibility (migrating from old Float format)
    val oldFloatLat = -6.175115f
    prefs.edit().putFloat("old_lat", oldFloatLat).apply()

    // Retrieve as Double (should trigger backward compatibility and convert transparently)
    val migratedLat = prefs.getDouble("old_lat", 0.0)
    assertEquals(oldFloatLat.toDouble(), migratedLat, 1e-7)

    // Confirm it wrote the Long/Double back to SharedPreferences
    val longRep = prefs.getLong("old_lat", 0L)
    val doubleRepFromLong = java.lang.Double.longBitsToDouble(longRep)
    assertEquals(oldFloatLat.toDouble(), doubleRepFromLong, 0.0)
  }
}
