package id.ideahousetech.prayertime_qibla

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrayerTimeValidationTest {

    private lateinit var viewModel: PrayerViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        viewModel = PrayerViewModel(context)
    }

    @Test
    fun `test isValidTimeString with valid inputs`() {
        // Standard formats
        assertTrue(viewModel.isValidTimeString("12:00"))
        assertTrue(viewModel.isValidTimeString("05:30"))
        assertTrue(viewModel.isValidTimeString("23:59"))
        assertTrue(viewModel.isValidTimeString("00:00"))

        // Single digit hour but double digit minute is accepted (standard in some APIs)
        assertTrue(viewModel.isValidTimeString("5:30"))
        assertTrue(viewModel.isValidTimeString("0:05"))

        // Suffixes and Timezones from APIs
        assertTrue(viewModel.isValidTimeString("12:00 WIB"))
        assertTrue(viewModel.isValidTimeString("12:00 (WIB)"))
        assertTrue(viewModel.isValidTimeString("12:00WIB"))
        assertTrue(viewModel.isValidTimeString(" 05:30 GMT+7 "))
    }

    @Test
    fun `test isValidTimeString with invalid inputs`() {
        // Null or blank
        assertFalse(viewModel.isValidTimeString(null))
        assertFalse(viewModel.isValidTimeString(""))
        assertFalse(viewModel.isValidTimeString("   "))

        // Missing colon or wrong format
        assertFalse(viewModel.isValidTimeString("invalid"))
        assertFalse(viewModel.isValidTimeString("12-00"))
        assertFalse(viewModel.isValidTimeString("1200"))

        // Non-standard minute format (minute has only 1 digit)
        assertFalse(viewModel.isValidTimeString("12:0"))
        assertFalse(viewModel.isValidTimeString("5:5"))

        // Range check failures
        assertFalse(viewModel.isValidTimeString("24:00")) // hour 24 is out of 0..23
        assertFalse(viewModel.isValidTimeString("12:60")) // minute 60 is out of 0..59
        assertFalse(viewModel.isValidTimeString("25:99"))
        assertFalse(viewModel.isValidTimeString("-1:30"))
        assertFalse(viewModel.isValidTimeString("12:-05"))
    }

    @Test
    fun `test parseTimeStringToCalendar with valid inputs`() {
        // 1. Standard today conversion
        val calToday = viewModel.parseTimeStringToCalendar("14:45", isTomorrow = false)
        assertNotNull(calToday)
        assertEquals(14, calToday!!.get(Calendar.HOUR_OF_DAY))
        assertEquals(45, calToday.get(Calendar.MINUTE))
        assertEquals(0, calToday.get(Calendar.SECOND))
        assertEquals(0, calToday.get(Calendar.MILLISECOND))

        // 2. Tomorrow conversion
        val calTomorrow = viewModel.parseTimeStringToCalendar("04:15 WIB", isTomorrow = true)
        assertNotNull(calTomorrow)
        assertEquals(4, calTomorrow!!.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, calTomorrow.get(Calendar.MINUTE))
        
        // Assert date is indeed tomorrow (difference is in the future and day of month is advanced)
        val now = Calendar.getInstance()
        val diffMs = calTomorrow.timeInMillis - now.timeInMillis
        assertTrue(diffMs > 0)
        assertEquals(1, (calTomorrow.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR) + 365) % 365)
    }

    @Test
    fun `test parseTimeStringToCalendar returns null for invalid inputs`() {
        // Invalid inputs must cleanly return null instead of crashing or falling back to "now" Calendar
        assertNull(viewModel.parseTimeStringToCalendar(null, false))
        assertNull(viewModel.parseTimeStringToCalendar("", false))
        assertNull(viewModel.parseTimeStringToCalendar("invalid", false))
        assertNull(viewModel.parseTimeStringToCalendar("12:0", false))
        assertNull(viewModel.parseTimeStringToCalendar("25:99", false))
    }
}
