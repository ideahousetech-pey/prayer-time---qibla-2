package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetManager
import android.content.Context

/**
 * BroadcastReceiver Provider untuk widget Waktu Sholat ukuran Small (2x2).
 */
class PrayerSmallWidgetProvider : BaseSecureWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Lakukan pembaharuan visual render widget luring
        PrayerWidgetHelper.updateSmallWidgets(context, appWidgetManager, appWidgetIds)
    }
}
