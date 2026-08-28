package id.ideahousetech.prayertime_qibla.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Model representasi pencapaian ibadah spiritual luhur (Spiritual Milestone).
 * Mengaitkan progress dengan referensi Hadits shahih / Al-Qur'an demi menumbuhkan Ikhlas dan rekapitulasi luring yang bernilai ibadah.
 */
data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val requirementText: String,
    val isUnlocked: Boolean,
    val progress: Float, // Nilai 0.0 sampai 1.0
    val progressText: String,
    val hadithSource: String?, // Contoh: "HR. Muslim no. 656"
    val spiritualBenefit: String, // Keistimewaan spiritual
    val category: BadgeCategory
)

enum class BadgeCategory {
    STREAK,
    JAMAAH,
    SPECIFIC_PRAYER,
    COMPRESSIVE
}

/**
 * Laporan spiritual mingguan terautomasi yang memadukan visualisasi statistik dan nasihat religius (Tausiyah Khusus).
 */
data class WeeklySpiritualSummary(
    val startDateLabel: String,
    val endDateLabel: String,
    val completionPercentage: Float,
    val totalJamaah: Int,
    val totalMunfarid: Int,
    val totalMasbuq: Int,
    val totalHalangan: Int,
    val highestStreakThisWeek: Int,
    val prayerDistribution: Map<String, Int>, // Nama Sholat -> Jumlah terlaksana
    val adviceTherapy: String, // Nasihat personal
    val adviceSource: String // Sumber rujukan nasihat
)

/**
 * Laporan spiritual bulanan terautomasi untuk menatap ke arah peningkatan ibadah bulanan secara holistik.
 */
data class MonthlySpiritualSummary(
    val monthLabel: String,
    val completionPercentage: Float,
    val totalJamaah: Int,
    val totalMunfarid: Int,
    val totalMasbuq: Int,
    val activeDaysCount: Int,
    val unlockedBadgesCount: Int,
    val bestStreakThisMonth: Int,
    val primaryAchievedMilestone: String,
    val generalAssessment: String // Tinjauan spiritual komprehensif
)
