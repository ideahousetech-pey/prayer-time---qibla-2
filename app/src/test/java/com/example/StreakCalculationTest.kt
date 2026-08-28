package id.ideahousetech.prayertime_qibla

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import id.ideahousetech.prayertime_qibla.data.PrayerTracker
import id.ideahousetech.prayertime_qibla.model.PrayerStatus
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerTrackerViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StreakCalculationTest {

    private lateinit var viewModel: PrayerTrackerViewModel
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        viewModel = PrayerTrackerViewModel(context)
    }

    private fun getRelativeDateString(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return sdf.format(cal.time)
    }

    private fun createTracker(date: String, isFull: Boolean): PrayerTracker {
        val status = if (isFull) PrayerStatus.JAMAAH else PrayerStatus.NONE
        return PrayerTracker(
            date = date,
            subuhStatus = status,
            dhuhrStatus = status,
            asrStatus = status,
            maghribStatus = status,
            isyaStatus = status
        )
    }

    @Test
    fun `test toJulianDayNumber conversion accuracy`() {
        // Test a specific known date
        // 2024-01-01 -> JDN 2460311
        assertEquals(2460311, viewModel.toJulianDayNumber("2024-01-01"))
        // 2024-01-02 -> JDN 2460312
        assertEquals(2460312, viewModel.toJulianDayNumber("2024-01-02"))
        // Invalid date string
        assertEquals(0, viewModel.toJulianDayNumber("invalid-date"))
    }

    @Test
    fun `test empty list returns zero streaks`() {
        val emptyList = emptyList<PrayerTracker>()
        assertEquals(0, viewModel.calculateActiveStreak(emptyList, useStrict = true))
        assertEquals(0, viewModel.calculateActiveStreak(emptyList, useStrict = false))
        assertEquals(0, viewModel.calculateBestStreak(emptyList))
    }

    @Test
    fun `test all days full consecutive`() {
        // 7 consecutive days completed up to yesterday
        val trackers = (1..7).map { i ->
            createTracker(getRelativeDateString(i), isFull = true)
        }

        // Both strict and lenient should return 7
        assertEquals(7, viewModel.calculateActiveStreak(trackers, useStrict = true))
        assertEquals(7, viewModel.calculateActiveStreak(trackers, useStrict = false))
        assertEquals(7, viewModel.calculateBestStreak(trackers))
    }

    @Test
    fun `test gap in the middle`() {
        // Monday, Sunday = Full
        // Saturday = Gap (No Record)
        // Friday, Thursday = Full
        val trackers = listOf(
            createTracker(getRelativeDateString(0), isFull = true), // Today (0 days ago)
            createTracker(getRelativeDateString(1), isFull = true), // Yesterday (1 day ago)
            // 2 days ago (Saturday) is omitted (No record)
            createTracker(getRelativeDateString(3), isFull = true), // 3 days ago
            createTracker(getRelativeDateString(4), isFull = true)  // 4 days ago
        )

        // Strict: Omitted day breaks the streak. 2 consecutive days (Today & Yesterday).
        assertEquals(2, viewModel.calculateActiveStreak(trackers, useStrict = true))

        // Lenient: Omitted day is skipped. 4 consecutive days.
        assertEquals(4, viewModel.calculateActiveStreak(trackers, useStrict = false))

        // Best streak is calculated purely sequentially.
        // Today, Yesterday are Julian Day sequential. JDN gap of 2 between 1 day ago and 3 days ago.
        // So best streak should be:
        // Group 1: 3 days ago & 4 days ago -> 2 days streak
        // Group 2: Today & Yesterday -> 2 days streak
        // Max best streak -> 2
        assertEquals(2, viewModel.calculateBestStreak(trackers))
    }

    @Test
    fun `test gap at start and end`() {
        // We have trackers at days:
        // Yesterday (1 day ago) -> Full
        // 2 days ago -> Full
        // 3 days ago -> Not Full (None status)
        // 4 days ago -> Full
        // 5 days ago -> Full
        val trackers = listOf(
            createTracker(getRelativeDateString(1), isFull = true),
            createTracker(getRelativeDateString(2), isFull = true),
            createTracker(getRelativeDateString(3), isFull = false), // Gap in status (broken record)
            createTracker(getRelativeDateString(4), isFull = true),
            createTracker(getRelativeDateString(5), isFull = true)
        )

        // Both strict and lenient must break at day 3 (status is "None" / not full)
        assertEquals(2, viewModel.calculateActiveStreak(trackers, useStrict = true))
        assertEquals(2, viewModel.calculateActiveStreak(trackers, useStrict = false))

        // Best streak: Group 1 (Yesterday & 2 days ago) -> 2 days; Group 2 (4 days ago & 5 days ago) -> 2 days
        assertEquals(2, viewModel.calculateBestStreak(trackers))
    }

    @Test
    fun `test only one day`() {
        val trackers = listOf(
            createTracker(getRelativeDateString(1), isFull = true)
        )
        assertEquals(1, viewModel.calculateActiveStreak(trackers, useStrict = true))
        assertEquals(1, viewModel.calculateActiveStreak(trackers, useStrict = false))
        assertEquals(1, viewModel.calculateBestStreak(trackers))
    }

    @Test
    fun `test 365 days consecutively`() {
        val trackers = (1..365).map { i ->
            createTracker(getRelativeDateString(i), isFull = true)
        }
        assertEquals(365, viewModel.calculateActiveStreak(trackers, useStrict = true))
        assertEquals(365, viewModel.calculateActiveStreak(trackers, useStrict = false))
        assertEquals(365, viewModel.calculateBestStreak(trackers))
    }
}
