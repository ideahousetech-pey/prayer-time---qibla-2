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

        // 2. H-16 sebelum 1 Ramadhan -> FALSE
        val hMinus16 = awalRamadhanGregorian.minusDays(16)
        assertFalse("Tanggal H-16 sebelum Ramadhan harus bernilai false", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hMinus16))

        // 3. H-15 tepat sebelum 1 Ramadhan -> TRUE
        val hMinus15 = awalRamadhanGregorian.minusDays(15)
        assertTrue("Tanggal H-15 sebelum Ramadhan harus bernilai true", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hMinus15))

        // 4. Tengah Ramadhan (15 Ramadhan) -> TRUE
        val tengahRamadhanHijrah = HijrahDate.of(year, 9, 15)
        val tengahRamadhan = LocalDate.ofEpochDay(tengahRamadhanHijrah.toEpochDay())
        assertTrue("Tengah Ramadhan harus bernilai true", 
            HijriDateUtils.isRamadhanFeatureWindowActive(tengahRamadhan))

        // 5. H+15 setelah 1 Syawal -> TRUE
        val hPlus15 = awalSyawalGregorian.plusDays(15)
        assertTrue("H+15 setelah 1 Syawal harus bernilai true", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hPlus15))

        // 6. H+16 setelah 1 Syawal -> FALSE
        val hPlus16 = awalSyawalGregorian.plusDays(16)
        assertFalse("H+16 setelah 1 Syawal harus bernilai false", 
            HijriDateUtils.isRamadhanFeatureWindowActive(hPlus16))
    }

    @Test
    fun `test isFirstDayOfRamadhan and isFiveDaysBeforeEidFitr`() {
        val year = 1447
        val awalRamadhanHijrah = HijrahDate.of(year, 9, 1)
        val awalSyawalHijrah = HijrahDate.of(year, 10, 1)

        val awalRamadhanGregorian = LocalDate.ofEpochDay(awalRamadhanHijrah.toEpochDay())
        val awalSyawalGregorian = LocalDate.ofEpochDay(awalSyawalHijrah.toEpochDay())

        // 1 Ramadhan tepat -> isFirstDayOfRamadhan TRUE
        assertTrue("1 Ramadhan harus bernilai true", HijriDateUtils.isFirstDayOfRamadhan(awalRamadhanGregorian))
        assertFalse("Sehari sebelum 1 Ramadhan harus false", HijriDateUtils.isFirstDayOfRamadhan(awalRamadhanGregorian.minusDays(1)))
        assertFalse("Hari kedua Ramadhan harus false", HijriDateUtils.isFirstDayOfRamadhan(awalRamadhanGregorian.plusDays(1)))

        // H-5 Idul Fitri (1 Syawal - 5 hari) -> isFiveDaysBeforeEidFitr TRUE
        val hMinus5Eid = awalSyawalGregorian.minusDays(5)
        assertTrue("H-5 Idul Fitri harus bernilai true", HijriDateUtils.isFiveDaysBeforeEidFitr(hMinus5Eid))
        assertFalse("H-6 Idul Fitri harus bernilai false", HijriDateUtils.isFiveDaysBeforeEidFitr(hMinus5Eid.minusDays(1)))
        assertFalse("H-4 Idul Fitri harus bernilai false", HijriDateUtils.isFiveDaysBeforeEidFitr(hMinus5Eid.plusDays(1)))
    }
}
