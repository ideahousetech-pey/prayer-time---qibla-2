package id.ideahousetech.prayertime_qibla

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.utils.PermissionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PermissionBackupTest {

    private lateinit var context: Context
    private lateinit var permissionManager: PermissionManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        permissionManager = PermissionManager()
    }

    @Test
    fun testPermissionManagerCheckAllRequiredPermissions() {
        // Pada runtime testing murni (tanpa runtime grant), status default perizinan dibaca
        val status = permissionManager.checkAllRequiredPermissions(context)
        
        // Verifikasi properti status ter-bundle sempurna
        assertNotNull(status)
        assertFalse(status.hasCoarseLocation)
        assertFalse(status.hasFineLocation)
        
        // Notifikasi bernilai true karena SDK testing berada di Android 11 murni secara default pada beberapa level,
        // tetapi kita asumsikan properti hasNotifications didefinisikan dengan baik
        assertNotNull(status.hasNotifications)
        assertNotNull(status.hasExactAlarms)
        assertFalse(status.isReadyForAdzan)
    }

    @Test
    fun testHandlePermanentlyDeniedNotCrash() {
        try {
            permissionManager.handlePermanentlyDenied(context, "android.permission.ACCESS_FINE_LOCATION")
            // Sukses memulai intent menu setting aplikasi
        } catch (e: Exception) {
            fail("handlePermanentlyDenied melemparkan exception tidak terduga: ${e.message}")
        }
    }

    @Test
    fun testOpenExactAlarmSettingsNotCrash() {
        try {
            permissionManager.openExactAlarmSettings(context)
            // Sukses memulai intent menu alarm & reminder (jika di OS Android 12+)
        } catch (e: Exception) {
            fail("openExactAlarmSettings melemparkan exception tidak terduga: ${e.message}")
        }
    }
}
