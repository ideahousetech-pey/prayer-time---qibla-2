package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Utilitas deteksi Root yang "Gentle".
 * Berfungsi mendeteksi apakah perangkat telah dimodifikasi (root), tanpa melakukan pemblokiran
 * paksa demi menjaga kenyamanan pengguna aplikasi ibadah.
 */
object RootDetector {

    /**
     * Memeriksa seluruh indikator root secara kumulatif.
     */
    fun isDeviceRooted(context: Context): Boolean {
        return checkBuildTags() || checkSuBinaryPaths() || checkSuExecution() || checkCommonRootFiles()
    }

    /**
     * 1. Memeriksa Build Tags untuk tanda "test-keys" yang biasanya ada di Custom ROM tidak resmi.
     */
    private fun checkBuildTags(): Boolean {
        val tags = Build.TAGS
        return tags != null && tags.contains("test-keys")
    }

    /**
     * 2. Memeriksa keberadaan file biner "su" di lokasi-lokasi sistem yang umum.
     */
    private fun checkSuBinaryPaths(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/usr/we-need-root/su",
            "/atb/su",
            "/system/bin/failsafe/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }

    /**
     * 3. Memeriksa apakah perintah "su" dapat dijalankan secara interaktif di runtime shell.
     */
    private fun checkSuExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            line != null
        } catch (t: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * 4. Memeriksa file/folder spesifik Magisk atau SuperSU di storage internal.
     */
    private fun checkCommonRootFiles(): Boolean {
        val rootDirectories = arrayOf(
            "/data/adb/magisk",
            "/dbdata/databases/com.noshufou.android.su",
            "/data/data/com.noshufou.android.su",
            "/data/data/com.topjohnwu.magisk",
            "/data/data/eu.chainfire.supersu"
        )
        for (dir in rootDirectories) {
            if (File(dir).exists()) {
                return true
            }
        }
        return false
    }
}
