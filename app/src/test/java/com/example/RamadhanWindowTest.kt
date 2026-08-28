package id.ideahousetech.prayertime_qibla

import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.chrono.HijrahDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RamadhanWindowTest {

    @Test
    fun `test isRamadhanFeatureWindowActive with various dates`() {
        // Ambil referensi tahun 1447 H / 1448 H
        val year = 1447
        val awalRamadhanHijrah = HijrahDate.of(year, 9, 1)
        val awalSyawalHijrah = HijrahDate.of(year, 10, 1)

        val awalRamadhanGregorian = LocalDate.ofEpochDay(awalRamadhanHijrah.toEpochDay())
        val awalSyawalGregorian = LocalDate.ofEpochDay(awalSyawalHijrah.toEpochDay())

        // 1. Jauh sebelum Ramadhan (misal 30 hari sebelum 1 Ramadhan) -> FALSE
        val jauhSebelum = awalRamadhanGregorian.minusDays(30)
        assertFalse("Tanggal jauh sebelum Ramadhan harus bernilai false", 
            HijriDateUtils.isRamadhanFeatureWindowActive(jauhSebelum))

        // 2. H-10 tepat sebelum 1 Ramadhan -> TRUE
        val hMinus10 = awalRamadhanGregorian.minusDays(10)
        assertTrue("Tanggal H-10 sebelum Ramadhan harus bernilai true", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hMinus10))

        // 3. Tengah Ramadhan (15 Ramadhan) -> TRUE
        val tengahRamadhanHijrah = HijrahDate.of(year, 9, 15)
        val tengahRamadhan = LocalDate.ofEpochDay(tengahRamadhanHijrah.toEpochDay())
        assertTrue("Tengah Ramadhan harus bernilai true", 
            HijriDateUtils.isRamadhanFeatureWindowActive(tengahRamadhan))

        // 4. H+10 setelah 1 Syawal -> TRUE
        val hPlus10 = awalSyawalGregorian.plusDays(10)
        assertTrue("H+10 setelah 1 Syawal harus bernilai true", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hPlus10))

        // 5. H+11 setelah 1 Syawal -> FALSE
        val hPlus11 = awalSyawalGregorian.plusDays(11)
        assertFalse("H+11 setelah 1 Syawal harus bernilai false", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hPlus11))
    }
}
