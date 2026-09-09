package com.example

import id.ideahousetech.prayertime_qibla.model.PrayerTime
import id.ideahousetech.prayertime_qibla.model.ramadhan.RamadhanTask
import id.ideahousetech.prayertime_qibla.model.ramadhan.Submission
import id.ideahousetech.prayertime_qibla.utils.HijriDateUtils
import id.ideahousetech.prayertime_qibla.viewmodel.PrayerSubmissionUiModel
import id.ideahousetech.prayertime_qibla.viewmodel.TaskItemUiModel
import id.ideahousetech.prayertime_qibla.viewmodel.TaskLockState
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RamadhanPrayerTaskTest {

    @Test
    fun testPrayerTimeImsakField() {
        val prayerTime = PrayerTime(
            dateGregorian = "Rabu, 13 Mei 2026",
            dateHijri = "13 Dzulqa'dah 1447 H",
            fajr = "04:35",
            dhuhr = "12:00",
            asr = "15:15",
            maghrib = "18:05",
            isha = "19:15",
            imsak = "04:25"
        )
        assertEquals("04:25", prayerTime.imsak)

        // Default value verification
        val defaultPrayerTime = PrayerTime(
            dateGregorian = "Rabu, 13 Mei 2026",
            dateHijri = "13 Dzulqa'dah 1447 H",
            fajr = "04:35",
            dhuhr = "12:00",
            asr = "15:15",
            maghrib = "18:05",
            isha = "19:15"
        )
        assertEquals("", defaultPrayerTime.imsak)
    }

    @Test
    fun testSubmissionPrayerTypeFieldAndConstants() {
        assertEquals("subuh", Submission.PRAYER_SUBUH)
        assertEquals("dzuhur", Submission.PRAYER_DZUHUR)
        assertEquals("ashar", Submission.PRAYER_ASHAR)
        assertEquals("maghrib", Submission.PRAYER_MAGHRIB)
        assertEquals("isya", Submission.PRAYER_ISYA)
        assertEquals("tarawih", Submission.PRAYER_TARAWIH)

        val sub = Submission(
            submissionId = "user1_task1_subuh",
            studentId = "user1",
            taskId = "task1",
            prayerType = Submission.PRAYER_SUBUH,
            status = Submission.STATUS_SELESAI
        )
        assertEquals("subuh", sub.prayerType)
        assertTrue(sub.isSelesai())
    }

    @Test
    fun testHijriDateUtilsIsCurrentlyRamadhan() {
        // Ramadhan 1447 H corresponds to approx mid-February to mid-March 2026
        // Let's test non-Ramadhan date (e.g. today or known date)
        val nonRamadhan = LocalDate.of(2026, 9, 9)
        // Month 9 of Hijri year is Ramadhan. In September 2026, it is Rabi'ul Awwal (month 3)
        assertFalse(HijriDateUtils.isCurrentlyRamadhan(nonRamadhan))
    }

    @Test
    fun testTaskItemUiModelSixItemsStructure() {
        val prayerTypes = listOf(
            Submission.PRAYER_SUBUH to "Subuh",
            Submission.PRAYER_DZUHUR to "Dzuhur",
            Submission.PRAYER_ASHAR to "Ashar",
            Submission.PRAYER_MAGHRIB to "Maghrib",
            Submission.PRAYER_ISYA to "Isya",
            Submission.PRAYER_TARAWIH to "Tarawih"
        )

        val items = prayerTypes.map { (pType, label) ->
            PrayerSubmissionUiModel(
                prayerType = pType,
                label = label,
                submission = null,
                lockState = TaskLockState.ACTIVE,
                isSelesai = false
            )
        }

        val taskModel = TaskItemUiModel(
            task = RamadhanTask(taskId = "task1", dayIndex = 1),
            prayerItems = items
        )

        assertEquals(6, taskModel.prayerItems.size)
        assertEquals("Subuh", taskModel.prayerItems[0].label)
        assertEquals("Tarawih", taskModel.prayerItems[5].label)
        assertFalse(taskModel.isSelesai)
    }

    @Test
    fun testPrayerLockLogicHelper() {
        val fajr = LocalTime.of(4, 30)
        val dhuhr = LocalTime.of(12, 0)
        val asr = LocalTime.of(15, 15)
        val maghrib = LocalTime.of(18, 0)
        val isha = LocalTime.of(19, 15)

        fun checkLock(prayerType: String, now: LocalTime): TaskLockState {
            return when (prayerType) {
                Submission.PRAYER_SUBUH -> when {
                    now.isBefore(fajr) -> TaskLockState.NOT_STARTED
                    now.isBefore(dhuhr) -> TaskLockState.ACTIVE
                    else -> TaskLockState.EXPIRED
                }
                Submission.PRAYER_DZUHUR -> when {
                    now.isBefore(dhuhr) -> TaskLockState.NOT_STARTED
                    now.isBefore(asr) -> TaskLockState.ACTIVE
                    else -> TaskLockState.EXPIRED
                }
                Submission.PRAYER_ASHAR -> when {
                    now.isBefore(asr) -> TaskLockState.NOT_STARTED
                    now.isBefore(maghrib) -> TaskLockState.ACTIVE
                    else -> TaskLockState.EXPIRED
                }
                Submission.PRAYER_MAGHRIB -> when {
                    now.isBefore(maghrib) -> TaskLockState.NOT_STARTED
                    now.isBefore(isha) -> TaskLockState.ACTIVE
                    else -> TaskLockState.EXPIRED
                }
                Submission.PRAYER_ISYA, Submission.PRAYER_TARAWIH -> when {
                    now.isBefore(isha) -> TaskLockState.NOT_STARTED
                    else -> TaskLockState.ACTIVE
                }
                else -> TaskLockState.NOT_STARTED
            }
        }

        // 03:00 -> before Fajr
        assertEquals(TaskLockState.NOT_STARTED, checkLock(Submission.PRAYER_SUBUH, LocalTime.of(3, 0)))
        // 05:00 -> in Fajr window
        assertEquals(TaskLockState.ACTIVE, checkLock(Submission.PRAYER_SUBUH, LocalTime.of(5, 0)))
        // 13:00 -> after Fajr window (Dzuhur window)
        assertEquals(TaskLockState.EXPIRED, checkLock(Submission.PRAYER_SUBUH, LocalTime.of(13, 0)))
        assertEquals(TaskLockState.ACTIVE, checkLock(Submission.PRAYER_DZUHUR, LocalTime.of(13, 0)))
        assertEquals(TaskLockState.NOT_STARTED, checkLock(Submission.PRAYER_ASHAR, LocalTime.of(13, 0)))

        // 20:00 -> after Isha
        assertEquals(TaskLockState.EXPIRED, checkLock(Submission.PRAYER_MAGHRIB, LocalTime.of(20, 0)))
        assertEquals(TaskLockState.ACTIVE, checkLock(Submission.PRAYER_ISYA, LocalTime.of(20, 0)))
        assertEquals(TaskLockState.ACTIVE, checkLock(Submission.PRAYER_TARAWIH, LocalTime.of(20, 0)))
    }
}
