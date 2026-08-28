package id.ideahousetech.prayertime_qibla.utils

import android.app.PendingIntent
import android.os.Build

/**
 * Extension/helper function untuk mendapatkan flags PendingIntent yang aman di semua API level Android.
 */
fun pendingIntentFlags(update: Boolean = true): Int {
    val baseFlag = if (update) PendingIntent.FLAG_UPDATE_CURRENT else 0
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        baseFlag or PendingIntent.FLAG_IMMUTABLE
    } else {
        baseFlag
    }
}
