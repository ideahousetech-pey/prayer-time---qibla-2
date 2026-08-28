package id.ideahousetech.prayertime_qibla

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.utils.AppSecurityManager
import id.ideahousetech.prayertime_qibla.utils.AppSecurityManager.SecurityLevel
import id.ideahousetech.prayertime_qibla.utils.RootDetector
import id.ideahousetech.prayertime_qibla.utils.AppIntegrityChecker
import id.ideahousetech.prayertime_qibla.utils.SecurityEnvironmentChecker
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppSecurityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testAppSecurityManagerInitialization() {
        // Inisialisasi Security Manager
        AppSecurityManager.initialize(context)

        // Verifikasi properti default
        assertNotNull(AppSecurityManager.securityLevel)
        
        val report = AppSecurityManager.getSecurityReport(context)
        assertEquals(AppSecurityManager.isDeviceRooted, report.isRooted)
        assertEquals(AppSecurityManager.isDebugging, report.isDebug)
        assertEquals(AppSecurityManager.isRunningOnEmulator, report.isEmulator)
        assertEquals(AppSecurityManager.isAppIntegrityValid, report.isIntegrityValid)
    }

    @Test
    fun testAppSecurityManagerDecisionFlows() {
        // Inisialisasi
        AppSecurityManager.initialize(context)

        // Pastikan canAccessSensitiveData selaras dengan status integritas aplikasi
        if (AppSecurityManager.securityLevel == SecurityLevel.COMPROMISED) {
            assertFalse(AppSecurityManager.canAccessSensitiveData())
        } else {
            assertTrue(AppSecurityManager.canAccessSensitiveData())
        }

        // Memeriksa status shouldShowSecurityWarning
        val expectedWarning = AppSecurityManager.securityLevel == SecurityLevel.LOW || AppSecurityManager.securityLevel == SecurityLevel.COMPROMISED
        assertEquals(expectedWarning, AppSecurityManager.shouldShowSecurityWarning())
    }

    @Test
    fun testMemoryProtectionClearCharArray() {
        val secretData = charArrayOf('b', 'i', 's', 'm', 'i', 'l', 'l', 'a', 'h')
        
        // Bersihkan memori data sensitif
        AppSecurityManager.clearSensitiveData(secretData)

        // Verifikasi bahwa seluruh karakter telah di-nol-kan
        for (char in secretData) {
            assertEquals('\u0000', char)
        }
    }

    @Test
    fun testMemoryProtectionClearByteArray() {
        val secretBytes = byteArrayOf(1, 2, 3, 4, 5, 6)

        // Bersihkan data bytes sensitif
        AppSecurityManager.clearSensitiveBytes(secretBytes)

        // Verifikasi bahwa seluruh byte telah di-nol-kan
        for (byte in secretBytes) {
            assertEquals(0.toByte(), byte)
        }
    }
}
