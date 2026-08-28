package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetManager
import android.content.Context

/**
 * BroadcastReceiver Provider untuk widget Waktu Sholat ukuran Large (4x4).
 */
class PrayerLargeWidgetProvider : BaseSecureWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        PrayerWidgetHelper.updateLargeWidgets(context, appWidgetManager, appWidgetIds)
    }
}
