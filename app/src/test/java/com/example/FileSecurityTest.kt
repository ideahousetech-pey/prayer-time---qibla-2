package id.ideahousetech.prayertime_qibla

import id.ideahousetech.prayertime_qibla.utils.FileSecurityUtils
import id.ideahousetech.prayertime_qibla.utils.TrustedAdzanDomains
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileSecurityTest {

    @Test
    fun testSanitizeFileNamePathTraversal() {
        // Path traversal attempts
        val malicious1 = "../../../etc/passwd"
        val malicious2 = "..\\..\\..\\system_file.so"
        val malicious3 = "normal_file/../../../dangerous.mp3"
        val malicious4 = "safe_name.mp3"
        val malicious5 = "  spaces_and_dots..mp3  "

        assertEquals("passwd", FileSecurityUtils.sanitizeFileName(malicious1))
        assertEquals("system_file.so", FileSecurityUtils.sanitizeFileName(malicious2))
        assertEquals("dangerous.mp3", FileSecurityUtils.sanitizeFileName(malicious3))
        assertEquals("safe_name.mp3", FileSecurityUtils.sanitizeFileName(malicious4))
        assertEquals("spaces_and_dots.mp3", FileSecurityUtils.sanitizeFileName(malicious5))
    }

    @Test
    fun testTrustedAdzanDomainsWhitelist() {
        // Trusted domains
        assertTrue(TrustedAdzanDomains.isUrlTrusted("https://raw.githubusercontent.com/sidandv/My-Azan/master/Azan.mp3"))
        assertTrue(TrustedAdzanDomains.isUrlTrusted("https://www.islamcan.com/audio/adhans/adhan10.mp3"))
        assertTrue(TrustedAdzanDomains.isUrlTrusted("https://islamcan.com/audio/adhan.mp3"))
        assertTrue(TrustedAdzanDomains.isUrlTrusted("https://mp3quran.net/eng/reciters/1.mp3"))

        // Untrusted/malicious domains
        assertFalse(TrustedAdzanDomains.isUrlTrusted("https://malicious-site.com/Azan.mp3"))
        assertFalse(TrustedAdzanDomains.isUrlTrusted("http://raw.githubusercontent.com.attacker.com/My-Azan/master/Azan.mp3"))
        assertFalse(TrustedAdzanDomains.isUrlTrusted("ftp://www.islamcan.com/audio/adhan.mp3")) // protocol check is handled gracefully
    }
}
