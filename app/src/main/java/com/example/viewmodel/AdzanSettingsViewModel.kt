package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.ideahousetech.prayertime_qibla.service.NotificationService
import id.ideahousetech.prayertime_qibla.utils.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdzanSettingsViewModel(private val context: Context) : ViewModel() {
    private val prefs = SecurePrefs.get(context)
    private val notificationService = NotificationService.getInstance(context)

    private val _enableAdzanAlarm = MutableStateFlow(prefs.getBoolean("enable_adzan_alarm", true))
    val enableAdzanAlarm: StateFlow<Boolean> = _enableAdzanAlarm.asStateFlow()

    private val _adzanSubuhSound = MutableStateFlow(prefs.getString("adzan_subuh_sound", "madinah") ?: "madinah")
    val adzanSubuhSound: StateFlow<String> = _adzanSubuhSound.asStateFlow()

    private val _adzanDhuhrSound = MutableStateFlow(prefs.getString("adzan_dhuhr_sound", "makkah") ?: "makkah")
    val adzanDhuhrSound: StateFlow<String> = _adzanDhuhrSound.asStateFlow()

    private val _adzanAsrSound = MutableStateFlow(prefs.getString("adzan_asr_sound", "makkah") ?: "makkah")
    val adzanAsrSound: StateFlow<String> = _adzanAsrSound.asStateFlow()

    private val _adzanMaghribSound = MutableStateFlow(prefs.getString("adzan_maghrib_sound", "makkah") ?: "makkah")
    val adzanMaghribSound: StateFlow<String> = _adzanMaghribSound.asStateFlow()

    private val _adzanIshaSound = MutableStateFlow(prefs.getString("adzan_isha_sound", "makkah") ?: "makkah")
    val adzanIshaSound: StateFlow<String> = _adzanIshaSound.asStateFlow()

    private val _adzanVolume = MutableStateFlow(prefs.getInt("adzan_volume", 80))
    val adzanVolume: StateFlow<Int> = _adzanVolume.asStateFlow()

    private val _enablePreReminder = MutableStateFlow(prefs.getBoolean("enable_pre_reminder", false))
    val enablePreReminder: StateFlow<Boolean> = _enablePreReminder.asStateFlow()

    private val _preReminderMinutes = MutableStateFlow(prefs.getInt("pre_reminder_minutes", 15))
    val preReminderMinutes: StateFlow<Int> = _preReminderMinutes.asStateFlow()

    private val _activeTestPrayer = MutableStateFlow<String?>(null)
    val activeTestPrayer: StateFlow<String?> = _activeTestPrayer.asStateFlow()

    val previewState = notificationService.previewState

    fun updateEnableAdzanAlarm(value: Boolean) {
        _enableAdzanAlarm.value = value
        prefs.edit().putBoolean("enable_adzan_alarm", value).apply()
    }

    fun updateAdzanSubuhSound(value: String) {
        _adzanSubuhSound.value = value
        prefs.edit().putString("adzan_subuh_sound", value).apply()
    }

    fun updateAdzanDhuhrSound(value: String) {
        _adzanDhuhrSound.value = value
        prefs.edit().putString("adzan_dhuhr_sound", value).apply()
    }

    fun updateAdzanAsrSound(value: String) {
        _adzanAsrSound.value = value
        prefs.edit().putString("adzan_asr_sound", value).apply()
    }

    fun updateAdzanMaghribSound(value: String) {
        _adzanMaghribSound.value = value
        prefs.edit().putString("adzan_maghrib_sound", value).apply()
    }

    fun updateAdzanIshaSound(value: String) {
        _adzanIshaSound.value = value
        prefs.edit().putString("adzan_isha_sound", value).apply()
    }

    fun updateAdzanVolume(value: Int) {
        _adzanVolume.value = value
        prefs.edit().putInt("adzan_volume", value).apply()
    }

    fun updateEnablePreReminder(value: Boolean) {
        _enablePreReminder.value = value
        prefs.edit().putBoolean("enable_pre_reminder", value).apply()
    }

    fun updatePreReminderMinutes(value: Int) {
        _preReminderMinutes.value = value
        prefs.edit().putInt("pre_reminder_minutes", value).apply()
    }

    fun testAdzan(prayerName: String) {
        if (_activeTestPrayer.value == prayerName) {
            stopTest()
        } else {
            _activeTestPrayer.value = prayerName
            val isFajr = prayerName.lowercase() == "subuh"
            notificationService.previewAdzan(isFajr = isFajr, durationSeconds = 10, prayerName = prayerName)
        }
    }

    fun stopTest() {
        notificationService.stopPreviewAdzan()
        _activeTestPrayer.value = null
    }
}

class AdzanSettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdzanSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdzanSettingsViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
