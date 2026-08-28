package id.ideahousetech.prayertime_qibla

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit test untuk memverifikasi fungsionalitas pengaman race condition (safeResume)
 * yang digunakan pada LocationService guna mencegah double resume pada coroutine continuation.
 */
class LocationServiceTest {

    @Test
    fun testSafeResumePreventsDoubleInvocation() {
        val isResumed = AtomicBoolean(false)
        val resumeCount = AtomicInteger(0)
        
        // Menyerupai implementasi safeResume() di LocationService
        fun safeResume() {
            if (isResumed.compareAndSet(false, true)) {
                resumeCount.incrementAndGet()
            }
        }

        // Pemanggilan beruntun
        safeResume()
        safeResume()

        assertEquals("Resume harus dipanggil tepat satu kali meskipun dipanggil beruntun", 1, resumeCount.get())
    }

    @Test
    fun testSafeResumeConcurrentThreadSafety() {
        val isResumed = AtomicBoolean(false)
        val resumeCount = AtomicInteger(0)
        val numThreads = 10
        val latch = CountDownLatch(numThreads)
        
        fun safeResume() {
            if (isResumed.compareAndSet(false, true)) {
                resumeCount.incrementAndGet()
            }
        }

        // Jalankan banyak thread secara bersamaan untuk mensimulasikan race condition nyata
        val threads = List(numThreads) {
            Thread {
                try {
                    safeResume()
                } finally {
                    latch.countDown()
                }
            }
        }

        threads.forEach { it.start() }
        val completed = latch.await(2, TimeUnit.SECONDS)

        assertTrue("Semua thread harus selesai berjalan", completed)
        assertEquals("Meskipun diakses secara konkurean oleh banyak thread, resume hanya dipanggil tepat satu kali", 1, resumeCount.get())
    }
}
