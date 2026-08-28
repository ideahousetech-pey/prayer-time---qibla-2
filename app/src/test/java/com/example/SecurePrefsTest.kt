package id.ideahousetech.prayertime_qibla

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.utils.DataSensitivity
import id.ideahousetech.prayertime_qibla.utils.KeyStoreDiagnostics
import id.ideahousetech.prayertime_qibla.utils.PrefsKeys
import id.ideahousetech.prayertime_qibla.utils.PrefsMigration
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import id.ideahousetech.prayertime_qibla.utils.getSensitivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecurePrefsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        SecurePrefs.resetInstance()
    }

    @Test
    fun testDataSensitivityCategorization() {
        // Verify PrefsKeys sensitivity mapping
        assertEquals(DataSensitivity.CRITICAL, PrefsKeys.getSensitivity(PrefsKeys.CACHED_LAT))
        assertEquals(DataSensitivity.CRITICAL, PrefsKeys.getSensitivity(PrefsKeys.CACHED_LON))
        assertEquals(DataSensitivity.CRITICAL, PrefsKeys.getSensitivity(PrefsKeys.CACHED_ADDRESS))

        assertEquals(DataSensitivity.SENSITIVE, PrefsKeys.getSensitivity(PrefsKeys.ENABLE_ADZAN_ALARM))
        assertEquals(DataSensitivity.SENSITIVE, PrefsKeys.getSensitivity(PrefsKeys.PRAYER_TIME_OFFSET))

        assertEquals(DataSensitivity.NON_SENSITIVE, PrefsKeys.getSensitivity(PrefsKeys.APP_THEME_MODE))
        assertEquals(DataSensitivity.NON_SENSITIVE, PrefsKeys.getSensitivity(PrefsKeys.IS_ONBOARDING_COMPLETED))
    }

    @Test
    fun testPlainFallbackSensitivityProtection() {
        // Force reset instance and get preferences
        val prefs = SecurePrefs.get(context)

        // Put values of different sensitivity levels
        prefs.edit()
            .putString(PrefsKeys.APP_THEME_MODE, "dark") // Non-sensitive
            .putBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true) // Sensitive
            .putString(PrefsKeys.CACHED_ADDRESS, "Jakarta, Indonesia") // Critical
            .apply()

        // 1. Verify we can read the critical value from our in-memory cache
        assertEquals("Jakarta, Indonesia", prefs.getString(PrefsKeys.CACHED_ADDRESS, null))

        // 2. Open the raw backup plain shared preferences directly to see what actually touched disk
        val rawPlainPrefs = context.getSharedPreferences("adzan_secure_prefs_plain_fallback", Context.MODE_PRIVATE)
        
        // Critical data MUST NOT exist on the plain text storage disk!
        assertFalse(rawPlainPrefs.contains(PrefsKeys.CACHED_ADDRESS))
        assertNull(rawPlainPrefs.getString(PrefsKeys.CACHED_ADDRESS, null))

        // Non-sensitive & Sensitive data ARE allowed to be saved to plain fallback disk
        assertTrue(rawPlainPrefs.contains(PrefsKeys.APP_THEME_MODE))
        assertEquals("dark", rawPlainPrefs.getString(PrefsKeys.APP_THEME_MODE, null))
        assertTrue(rawPlainPrefs.contains(PrefsKeys.ENABLE_ADZAN_ALARM))
        assertTrue(rawPlainPrefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, false))
    }

    @Test
    fun testConcurrentThreadSafety() {
        val threadCount = 10
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val instances = java.util.Collections.synchronizedList(mutableListOf<android.content.SharedPreferences>())

        for (i in 0 until threadCount) {
            executor.submit {
                try {
                    val prefs = SecurePrefs.get(context)
                    instances.add(prefs)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        // Assert all threads retrieved the EXACT same singleton instance
        val firstInstance = instances[0]
        for (instance in instances) {
            assertSame(firstInstance, instance)
        }
    }

    @Test
    fun testKeyStoreDiagnosticsExecution() {
        val result = KeyStoreDiagnostics.performDiagnostics()
        // Standard Robolectric environments run virtual JVM keystores, let's verify diagnostics run cleanly
        assertNotNull(result)
        assertTrue(result.errorMessage.isNotEmpty())
        assertTrue(result.suggestion.isNotEmpty())
    }

    @Test
    fun testMigrationPlainToEncrypted() {
        // Pre-populate some old plain keys
        val oldPlainPrefs = context.getSharedPreferences("adzan_secure_prefs_plain_fallback", Context.MODE_PRIVATE)
        oldPlainPrefs.edit()
            .putString(PrefsKeys.APP_THEME_MODE, "light")
            .putBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, false)
            .apply()

        // Run plain-to-secure migration helper
        PrefsMigration.migrateFromPlainToEncrypted(context)

        // Retrieve current SecurePrefs (could be either encrypted or wrap-fallback depending on KeyStore available in run environment)
        val securePrefs = SecurePrefs.get(context)

        if (SecurePrefs.isEncryptionActive(context)) {
            // Verify values migrated successfully
            assertEquals("light", securePrefs.getString(PrefsKeys.APP_THEME_MODE, null))
            assertFalse(securePrefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true))

            // Old plain keys should have been cleaned up after migration
            assertFalse(oldPlainPrefs.contains(PrefsKeys.APP_THEME_MODE))
        } else {
            // If encryption is not active, values should remain in plain prefs to avoid losing non-sensitive user configs
            assertEquals("light", oldPlainPrefs.getString(PrefsKeys.APP_THEME_MODE, null))
            assertFalse(oldPlainPrefs.getBoolean(PrefsKeys.ENABLE_ADZAN_ALARM, true))
        }
    }
}
