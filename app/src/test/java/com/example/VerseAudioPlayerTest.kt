package id.ideahousetech.prayertime_qibla

import id.ideahousetech.prayertime_qibla.utils.VerseAudioPlayerManager
import id.ideahousetech.prayertime_qibla.utils.VersePlaybackState
import id.ideahousetech.prayertime_qibla.utils.getVerseAudioUrl
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VerseAudioPlayerTest {

    @Test
    fun `test getVerseAudioUrl generates correct EveryAyah pattern`() {
        // Surah 1 Ayat 1 (Al-Fatihah)
        val url1 = getVerseAudioUrl(1, 1)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/001001.mp3", url1)

        // Surah 114 Ayat 6 (An-Nas)
        val url2 = getVerseAudioUrl(114, 6)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/114006.mp3", url2)

        // Surah 2 Ayat 255 (Ayat Kursi)
        val url3 = getVerseAudioUrl(2, 255)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/002255.mp3", url3)

        // Surah 36 Ayat 1 (Yasin)
        val url4 = getVerseAudioUrl(36, 1)
        assertEquals("https://everyayah.com/data/Alafasy_128kbps/036001.mp3", url4)
    }

    @Test
    fun `test VerseAudioPlayerManager stop and release resets state`() {
        val manager = VerseAudioPlayerManager()
        assertNull(manager.currentSurah)
        assertNull(manager.currentVerse)

        manager.stop()
        assertNull(manager.currentSurah)
        assertNull(manager.currentVerse)

        manager.release()
        assertNull(manager.currentSurah)
        assertNull(manager.currentVerse)
    }
}
