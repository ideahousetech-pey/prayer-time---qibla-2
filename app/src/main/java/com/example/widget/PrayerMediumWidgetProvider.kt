package id.ideahousetech.prayertime_qibla.widget

import android.appwidget.AppWidgetManager
import android.content.Context

/**
 * BroadcastReceiver Provider untuk widget Waktu Sholat ukuran Medium (4x2).
 */
class PrayerMediumWidgetProvider : BaseSecureWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        PrayerWidgetHelper.updateMediumWidgets(context, appWidgetManager, appWidgetIds)
    }
}
