package id.ideahousetech.prayertime_qibla.model

/**
 * Representasi tipe Insight: Ayat, Hadits, atau Doa.
 */
enum class InsightType {
    AYAT,
    HADITS,
    DOA
}

/**
 * Model data representasi konten Daily Insight Islami yang dirancang untuk offline-first.
 * 
 * @property type Tipe konten (AYAT, HADITS, atau DOA)
 * @property title Judul atau deskripsi singkat insight harian
 * @property arabic Teks bahasa Arab asli dengan harakat lengkap
 * @property latin Transliterasi latin atau ejaan bantu
 * @property translation Terjemahan atau makna dalam Bahasa Indonesia
 * @property reference Sumber otentik rujukan (QS/Hadits/Kutipan Kitab)
 * @property category Label kategori (misalnya: "Sabar", "Syukur", "Adab")
 */
data class DailyInsightItem(
    val type: InsightType,
    val title: String,
    val arabic: String,
    val latin: String? = null,
    val translation: String,
    val reference: String,
    val category: String
)
