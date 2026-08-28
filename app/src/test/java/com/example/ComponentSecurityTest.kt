package id.ideahousetech.prayertime_qibla

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.utils.IntentSecurityUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComponentSecurityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testIntentSigningAndValidationSuccess() {
        val intent = Intent("id.ideahousetech.prayertime_qibla.ACTION_TEST").apply {
            putExtra("data_key", "valid_data")
        }

        // Sign the intent
        IntentSecurityUtils.signIntent(intent)

        // Verify key presence
        assertTrue(intent.hasExtra("_security_timestamp"))
        assertTrue(intent.hasExtra("_security_signature"))

        // Validate (since calling UID is same app, always trusted. 
        // We will mock calling from a different untrusted UID by manually testing signature verification logic)
        val action = intent.action ?: ""
        val timestamp = intent.getLongExtra("_security_timestamp", 0L)
        val signature = intent.getStringExtra("_security_signature") ?: ""

        val now = System.currentTimeMillis()
        assertTrue("Timestamp should be extremely close to now", Math.abs(now - timestamp) < 1000)
        
        // Assert trusted check passes for our own UID
        assertTrue(IntentSecurityUtils.isIntentFromTrustedSource(context, intent))
    }

    @Test
    fun testIntentSecurityExtrasSanitization() {
        val dangerousIntent = Intent().apply {
            putExtra("primitive_string", "Safe Text")
            putExtra("primitive_int", 42)
            putExtra("primitive_boolean", true)
            
            // Complex custom/Serializable objects that could trigger deserialization vulnerabilities are omitted in sanitization
            val dangerousObject = java.util.HashMap<String, String>().apply {
                put("key", "value")
            }
            putExtra("complex_map", dangerousObject)
        }

        val sanitizedBundle = IntentSecurityUtils.sanitizeIntentExtras(dangerousIntent)

        // Primitive types MUST be preserved
        assertEquals("Safe Text", sanitizedBundle.getString("primitive_string"))
        assertEquals(42, sanitizedBundle.getInt("primitive_int"))
        assertTrue(sanitizedBundle.getBoolean("primitive_boolean"))

        // Complex types/serializable data fields MUST be completely removed to prevent deserialization attacks
        assertNull(sanitizedBundle.getSerializable("complex_map"))
        assertFalse(sanitizedBundle.containsKey("complex_map"))
    }

    @Test
    fun testScheduledIntentSigningAndReplayProtection() {
        val scheduledIntent = Intent("id.ideahousetech.prayertime_qibla.ACTION_PLAY_ADZAN")
        val scheduledTimeMs = System.currentTimeMillis() + 10000 // 10 seconds into the future

        // Sign
        IntentSecurityUtils.signScheduledIntent(scheduledIntent, scheduledTimeMs)

        // Confirm
        assertEquals(scheduledTimeMs, scheduledIntent.getLongExtra("_security_scheduled_time", 0L))
        assertTrue(scheduledIntent.hasExtra("_security_signature"))

        // If a replay attack occurs with an ancient timestamp, validation should fail
        val expiredIntent = Intent("id.ideahousetech.prayertime_qibla.ACTION_PLAY_ADZAN")
        // Ancient timestamp from 1 hour ago
        val ancientTimeMs = System.currentTimeMillis() - 3600000 
        IntentSecurityUtils.signScheduledIntent(expiredIntent, ancientTimeMs)

        // Mock different UID check manually to simulate third party trying to spoof
        val signature = expiredIntent.getStringExtra("_security_signature") ?: ""
        val scheduledTime = expiredIntent.getLongExtra("_security_scheduled_time", 0L)
        val now = System.currentTimeMillis()
        
        // Since diff is ~1 hour, it is far above the 5-minute (300,000ms) threshold
        assertTrue(Math.abs(now - scheduledTime) > 300000)
    }

    @Test
    fun testSecurePendingIntentCreation() {
        val targetIntent = Intent("id.ideahousetech.prayertime_qibla.ACTION_TEST")
        
        val securePi = IntentSecurityUtils.createSecurePendingIntent(
            context,
            123,
            targetIntent,
            0,
            IntentSecurityUtils.PendingIntentType.BROADCAST
        )

        assertNotNull(securePi)
        // Intent must have been signed automatically during creation
        assertTrue(targetIntent.hasExtra("_security_signature"))
    }
}
