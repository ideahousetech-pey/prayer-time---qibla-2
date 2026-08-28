package id.ideahousetech.prayertime_qibla.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.ideahousetech.prayertime_qibla.data.DailyInsightRepository
import id.ideahousetech.prayertime_qibla.model.DailyInsightItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DailyInsightUiState {
    object Loading : DailyInsightUiState()
    data class Success(val items: List<DailyInsightItem>) : DailyInsightUiState()
    data class Error(val message: String) : DailyInsightUiState()
}

/**
 * ViewModel untuk mengelola sistem penayangan harian (Daily Insight System) secara mandiri.
 * Menyediakan data reaktif terisolasi untuk menghemat daya baterai dan mendukung pemuatan instan.
 */
class DailyInsightViewModel(
    private val repository: DailyInsightRepository = DailyInsightRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DailyInsightUiState>(DailyInsightUiState.Loading)
    val uiState: StateFlow<DailyInsightUiState> = _uiState.asStateFlow()

    init {
        loadTodayInsights()
    }

    /**
     * Memuat daftar kurasi hikmah (Ayat, Hadits, Doa) sesuai kalender hari ini.
     */
    fun loadTodayInsights() {
        viewModelScope.launch {
            _uiState.value = DailyInsightUiState.Loading
            try {
                val insights = repository.getDailyInsightsForToday()
                _uiState.value = DailyInsightUiState.Success(insights)
            } catch (e: Exception) {
                _uiState.value = DailyInsightUiState.Error(e.message ?: "Gagal memuat insight harian")
            }
        }
    }

    /**
     * Menyalin konten kartu hikmah secara terperinci ke clipboard sistem.
     */
    fun copyInsightToClipboard(context: Context, item: DailyInsightItem) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val contentToCopy = buildString {
                append("🌟 DAILY INSIGHT ISLAMI 🌟\n")
                append("✨ ${item.title} [${item.category}]\n\n")
                append("${item.arabic}\n")
                if (!item.latin.isNullOrBlank()) {
                    append("(${item.latin})\n")
                }
                append("\nArtinya: \"${item.translation}\"\n\n")
                append("Rujukan: ${item.reference}\n")
                append("Disalin via Aplikasi Waktu Sholat & Kiblat")
            }
            val clip = ClipData.newPlainText("Daily Insight", contentToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Konten berhasil disalin ke papan klip!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal menyalin konten.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membagikan konten hikmah ke platform media sosial, messenger, atau aplikasi lain.
     */
    fun shareInsight(context: Context, item: DailyInsightItem) {
        try {
            val shareText = buildString {
                append("🌟 DAILY INSIGHT ISLAMI 🌟\n")
                append("✨ Kategori: ${item.category} - ${item.title}\n\n")
                append("${item.arabic}\n\n")
                if (!item.latin.isNullOrBlank()) {
                    append("(${item.latin})\n\n")
                }
                append("Artinya: \"${item.translation}\"\n\n")
                append("Rujukan: ${item.reference}\n")
                append("Aplikasi Waktu Sholat & Kiblat")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Firman & Hikmah via"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal meluncurkan intent berbagi.", Toast.LENGTH_SHORT).show()
        }
    }
}
