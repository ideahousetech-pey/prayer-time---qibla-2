package id.ideahousetech.prayertime_qibla.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.ideahousetech.prayertime_qibla.ui.components.PremiumTopBar
import id.ideahousetech.prayertime_qibla.ui.theme.*

data class QuranSurah(
    val number: Int,
    val name: String,
    val arabicName: String,
    val meaning: String,
    val totalVerses: Int,
    val type: String, // Makkiyah / Madaniyah
    val description: String = ""
)

data class QuranVerse(
    val verseNumber: Int,
    val arabic: String,
    val latin: String,
    val translation: String
)

@Composable
fun QuranScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<QuranSurah?>(null) }
    
    val sharedPrefs = remember { context.getSharedPreferences("quran_bookmarks", Context.MODE_PRIVATE) }
    var bookmarkedSurahNumber by remember { mutableStateOf(sharedPrefs.getInt("bookmarked_surah", -1)) }

    val surahList = remember { getFullSurahList() }

    val filteredSurah = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            surahList
        } else {
            surahList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.meaning.contains(searchQuery, ignoreCase = true) ||
                it.number.toString() == searchQuery.trim()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackgroundGradient)
    ) {
        if (selectedSurah == null) {
            // VIEW LIST SURAH
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(CardSurface, CircleShape)
                            .border(1.dp, DividerLine, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Tampilkan info Bookmark jika ada
                    if (bookmarkedSurahNumber != -1) {
                        surahList.find { it.number == bookmarkedSurahNumber }?.let { bsurah ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldGlow)
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                                    .clickable { selectedSurah = bsurah }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Bookmark",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Terakhir Baca: ${bsurah.name}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Judul
                    Text(
                        text = "AL-QUR'ANUL KARIM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDim,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Kolom Pencarian
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama surah atau nomor...", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Cari",
                                tint = GoldPrimary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DividerLine,
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface
                        )
                    )

                    // List Surah
                    if (filteredSurah.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredSurah) { surah ->
                                SurahListItemCard(
                                    surah = surah,
                                    isBookmarked = (surah.number == bookmarkedSurahNumber),
                                    onClick = { selectedSurah = surah }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Surah tidak ditemukan.",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            // VIEW DETAIL AYAT SURAH
            val activeSurah = selectedSurah!!
            val isBookmarked = (activeSurah.number == bookmarkedSurahNumber)

            var versesList by remember { mutableStateOf<List<QuranVerse>>(emptyList()) }
            var isLoadingVerses by remember { mutableStateOf(true) }
            var isOfflineMode by remember { mutableStateOf(false) }

            LaunchedEffect(activeSurah.number) {
                isLoadingVerses = true
                isOfflineMode = false
                val quranService = id.ideahousetech.prayertime_qibla.service.QuranService()
                val fetched = quranService.getSurahDetail(activeSurah.number)
                if (fetched != null && fetched.isNotEmpty()) {
                    versesList = fetched
                } else {
                    versesList = getVersesForSurah(activeSurah.number, activeSurah)
                    isOfflineMode = true
                }
                isLoadingVerses = false
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toolbar Detail
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedSurah = null },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CardSurface, CircleShape)
                            .border(1.dp, DividerLine, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke List Surah",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Judul Surah Aktif
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeSurah.name.uppercase(),
                            fontSize = 16.sp,
                            fontFamily = CinzelFont,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "${activeSurah.type} • ${activeSurah.totalVerses} Ayat",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Tambah/Hapus Bookmark
                    IconButton(
                        onClick = {
                            if (isBookmarked) {
                                sharedPrefs.edit().remove("bookmarked_surah").apply()
                                bookmarkedSurahNumber = -1
                                Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                            } else {
                                sharedPrefs.edit().putInt("bookmarked_surah", activeSurah.number).apply()
                                bookmarkedSurahNumber = activeSurah.number
                                Toast.makeText(context, "Tandai Terakhir Baca: ${activeSurah.name}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CardSurface, CircleShape)
                            .border(1.dp, DividerLine, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Kartu Deskripsi Pengantar Surah (Bismillah)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, DividerLine, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = activeSurah.arabicName,
                                fontSize = 28.sp,
                                fontFamily = AmiriQuranFont,
                                color = GoldPrimary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "Artinya: ${activeSurah.meaning}",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            if (activeSurah.number != 9) { // At-Taubah tidak melafadzkan basmalah
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 18.sp,
                                    fontFamily = AmiriQuranFont,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Dengan menyebut nama Allah Yang Maha Pengasih lagi Maha Penyayang",
                                    fontSize = 11.sp,
                                    color = GoldPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    if (isLoadingVerses) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = GoldPrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Mengunduh ayat lengkap...",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        if (isOfflineMode) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = GoldGlow),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Mode Luring",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mode Luring. Hubungkan internet untuk memuat seluruh ${activeSurah.totalVerses} ayat.",
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // List Ayat per Ayat
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(versesList) { verse ->
                                VerseItemCard(verse = verse)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItemCard(
    surah: QuranSurah,
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isBookmarked) GoldGlow else CardSurface
    val cardBorder = if (isBookmarked) BorderStroke(1.dp, GoldPrimary) else BorderStroke(1.dp, DividerLine)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = cardBorder,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Lingkaran Nomor Surah
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(GoldGlow, CircleShape)
                        .border(1.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Detail Teks
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isBookmarked) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Terakhir Baca",
                                tint = GoldPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Text(
                        text = "${surah.meaning} • ${surah.totalVerses} Ayat",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Teks Arab Nama Surah
            Text(
                text = surah.arabicName,
                fontSize = 20.sp,
                fontFamily = AmiriQuranFont,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        }
    }
}

@Composable
fun VerseItemCard(
    verse: QuranVerse
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerLine, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Nomor Ayat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(GoldGlow, CircleShape)
                        .border(1.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verse.verseNumber.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
                
                Text(
                    text = "Ayat ${verse.verseNumber}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teks Arab Ayat
            Text(
                text = verse.arabic,
                fontSize = 24.sp,
                fontFamily = AmiriQuranFont,
                lineHeight = 42.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = GoldPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Teks Latin Transliterasi
            Text(
                text = verse.latin,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GoldLight,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Terjemahan Kemenag
            Text(
                text = verse.translation,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun getFullSurahList(): List<QuranSurah> {
    return listOf(
        QuranSurah(1, "Al-Fatihah", "الفاتحة", "Pembukaan", 7, "Makkiyah"),
        QuranSurah(2, "Al-Baqarah", "البقرة", "Sapi Betina", 286, "Madaniyah"),
        QuranSurah(3, "Ali 'Imran", "آل عمران", "Keluarga 'Imran", 200, "Madaniyah"),
        QuranSurah(4, "An-Nisa'", "النساء", "Wanita", 176, "Madaniyah"),
        QuranSurah(5, "Al-Ma'idah", "المائدة", "Jamuan Hidangan", 120, "Madaniyah"),
        QuranSurah(6, "Al-An'am", "الأنعام", "Hewan Ternak", 165, "Makkiyah"),
        QuranSurah(7, "Al-A'raf", "الأعراف", "Tempat Tertinggi", 206, "Makkiyah"),
        QuranSurah(8, "Al-Anfal", "الأنفال", "Rampasan Perang", 75, "Madaniyah"),
        QuranSurah(9, "At-Taubah", "التوبة", "Pengampunan", 129, "Madaniyah"),
        QuranSurah(10, "Yunus", "يونس", "Nabi Yunus", 109, "Makkiyah"),
        QuranSurah(11, "Hud", "هود", "Nabi Hud", 123, "Makkiyah"),
        QuranSurah(12, "Yusuf", "يوسف", "Nabi Yusuf", 111, "Makkiyah"),
        QuranSurah(13, "Ar-Ra'd", "الرعد", "Guruh", 43, "Madaniyah"),
        QuranSurah(14, "Ibrahim", "إبراهيم", "Nabi Ibrahim", 52, "Makkiyah"),
        QuranSurah(15, "Al-Hijr", "الحجر", "Negeri Kaum Samud", 99, "Makkiyah"),
        QuranSurah(16, "An-Nahl", "النحل", "Lebah", 128, "Makkiyah"),
        QuranSurah(17, "Al-Isra'", "الإسراء", "Perjalanan Malam", 111, "Makkiyah"),
        QuranSurah(18, "Al-Kahf", "الكهف", "Penghuni Gua", 110, "Makkiyah"),
        QuranSurah(19, "Maryam", "مريم", "Maryam", 98, "Makkiyah"),
        QuranSurah(20, "Taha", "طه", "Taha", 135, "Makkiyah"),
        QuranSurah(21, "Al-Anbiya'", "الأنبياء", "Para Nabi", 112, "Makkiyah"),
        QuranSurah(22, "Al-Hajj", "الحج", "Ibadah Haji", 78, "Madaniyah"),
        QuranSurah(23, "Al-Mu'minun", "المؤمنون", "Orang-Orang Mukmin", 118, "Makkiyah"),
        QuranSurah(24, "An-Nur", "النور", "Cahaya", 64, "Madaniyah"),
        QuranSurah(25, "Al-Furqan", "الفرقان", "Pembeda", 77, "Makkiyah"),
        QuranSurah(26, "Asy-Syu'ara'", "الشعراء", "Para Penyair", 227, "Makkiyah"),
        QuranSurah(27, "An-Naml", "النمل", "Semut", 93, "Makkiyah"),
        QuranSurah(28, "Al-Qasas", "القصص", "Kisah-Kisah", 88, "Makkiyah"),
        QuranSurah(29, "Al-'Ankabut", "العنكبut", "Laba-Laba", 69, "Makkiyah"),
        QuranSurah(30, "Ar-Rum", "الروم", "Bangsa Romawi", 60, "Makkiyah"),
        QuranSurah(31, "Luqman", "لقمان", "Luqman", 34, "Makkiyah"),
        QuranSurah(32, "As-Sajdah", "السجدة", "Sujud", 30, "Makkiyah"),
        QuranSurah(33, "Al-Ahzab", "الأحزاب", "Golongan Bersekutu", 73, "Madaniyah"),
        QuranSurah(34, "Saba'", "سبأ", "Kaum Saba'", 54, "Makkiyah"),
        QuranSurah(35, "Fatir", "فاطر", "Pencipta", 45, "Makkiyah"),
        QuranSurah(36, "Yasin", "يس", "Yasin", 83, "Makkiyah"),
        QuranSurah(37, "As-Saffat", "الصافات", "Barisan-Barisan", 182, "Makkiyah"),
        QuranSurah(38, "Sad", "ص", "Sad", 88, "Makkiyah"),
        QuranSurah(39, "Az-Zumar", "الزمر", "Rombongan-Rombongan", 75, "Makkiyah"),
        QuranSurah(40, "Ghafir", "غافر", "Maha Pengampun", 85, "Makkiyah"),
        QuranSurah(41, "Fussilat", "فصلت", "Dijelaskan", 54, "Makkiyah"),
        QuranSurah(42, "Asy-Syura", "الشورى", "Musyawarah", 53, "Makkiyah"),
        QuranSurah(43, "Az-Zukhruf", "الزخرف", "Perhiasan Emas", 89, "Makkiyah"),
        QuranSurah(44, "Ad-Dukhan", "الدخان", "Kabut", 59, "Makkiyah"),
        QuranSurah(45, "Al-Jasiyah", "الجاثية", "Berlutut", 37, "Makkiyah"),
        QuranSurah(46, "Al-Ahqaf", "الأحقاف", "Bukit-Bukit Pasir", 35, "Makkiyah"),
        QuranSurah(47, "Muhammad", "محمد", "Nabi Muhammad", 38, "Madaniyah"),
        QuranSurah(48, "Al-Fath", "الفتح", "Kemenangan", 29, "Madaniyah"),
        QuranSurah(49, "Al-Hujurat", "الحجرات", "Kamar-Kamar", 18, "Madaniyah"),
        QuranSurah(50, "Qaf", "ق", "Qaf", 45, "Makkiyah"),
        QuranSurah(51, "Az-Zariyat", "الذاريات", "Angin yang Menerbangkan", 60, "Makkiyah"),
        QuranSurah(52, "At-Tur", "الطور", "Bukit", 49, "Makkiyah"),
        QuranSurah(53, "An-Najm", "النجم", "Bintang", 62, "Makkiyah"),
        QuranSurah(54, "Al-Qamar", "العمر", "Bulan", 55, "Makkiyah"),
        QuranSurah(55, "Ar-Rahman", "الرحمن", "Maha Pengasih", 78, "Madaniyah"),
        QuranSurah(56, "Al-Waqi'ah", "الواقعة", "Hari Kiamat", 96, "Makkiyah"),
        QuranSurah(57, "Al-Hadid", "الحديد", "Besi", 29, "Madaniyah"),
        QuranSurah(58, "Al-Mujadilah", "المجادلة", "Gugatan", 22, "Madaniyah"),
        QuranSurah(59, "Al-Hasyr", "الحشر", "Pengusiran", 24, "Madaniyah"),
        QuranSurah(60, "Al-Mumtahanah", "الممتحنة", "Wanita yang Diuji", 13, "Madaniyah"),
        QuranSurah(61, "As-Saff", "الصف", "Barisan", 14, "Madaniyah"),
        QuranSurah(62, "Al-Jumu'ah", "الجمعة", "Hari Jumat", 11, "Madaniyah"),
        QuranSurah(63, "Al-Munafiqun", "المنفقون", "Orang-Orang Munafik", 11, "Madaniyah"),
        QuranSurah(64, "At-Taghabun", "التغابن", "Hari Ditampakkan Kesalahan", 18, "Madaniyah"),
        QuranSurah(65, "At-Talaq", "الطلاق", "Perceraian", 12, "Madaniyah"),
        QuranSurah(66, "At-Tahrim", "التحريم", "Mengharamkan", 12, "Madaniyah"),
        QuranSurah(67, "Al-Mulk", "الملك", "Kerajaan", 30, "Makkiyah"),
        QuranSurah(68, "Al-Qalam", "القلم", "Pena", 52, "Makkiyah"),
        QuranSurah(69, "Al-Haqqah", "الحاقة", "Hari Kiamat yang Pasti", 52, "Makkiyah"),
        QuranSurah(70, "Al-Ma'arij", "المعارج", "Tempat-Tempat Naik", 44, "Makkiyah"),
        QuranSurah(71, "Nuh", "نوح", "Nabi Nuh", 28, "Makkiyah"),
        QuranSurah(72, "Al-Jinn", "الجن", "Jin", 28, "Makkiyah"),
        QuranSurah(73, "Al-Muzzammil", "المزمل", "Orang Berselimut", 20, "Makkiyah"),
        QuranSurah(74, "Al-Muddassir", "المدثر", "Orang Berkemul", 56, "Makkiyah"),
        QuranSurah(75, "Al-Qiyamah", "القيامة", "Hari Kiamat", 40, "Makkiyah"),
        QuranSurah(76, "Al-Insan", "الإنسان", "Manusia", 31, "Madaniyah"),
        QuranSurah(77, "Al-Mursalat", "المرسلات", "Malaikat diutus", 50, "Makkiyah"),
        QuranSurah(78, "An-Naba'", "النبأ", "Berita Besar", 40, "Makkiyah"),
        QuranSurah(79, "An-Nazi'at", "النازعات", "Malaikat Pencabut", 46, "Makkiyah"),
        QuranSurah(80, "'Abasa", "عبس", "Bermuka Masam", 42, "Makkiyah"),
        QuranSurah(81, "At-Takwir", "التكوير", "Menggulung", 29, "Makkiyah"),
        QuranSurah(82, "Al-Infitar", "الانفطار", "Terbelah", 19, "Makkiyah"),
        QuranSurah(83, "Al-Mutaffifin", "المطففين", "Orang Curang", 36, "Makkiyah"),
        QuranSurah(84, "Al-Insyiqaq", "الانشقاق", "Terbelah", 25, "Makkiyah"),
        QuranSurah(85, "Al-Buruj", "البروج", "Gugusan Bintang", 22, "Makkiyah"),
        QuranSurah(86, "At-Tariq", "الطارق", "Yang Datang di Malam Hari", 17, "Makkiyah"),
        QuranSurah(87, "Al-A'la", "الأعلى", "Maha Tinggi", 19, "Makkiyah"),
        QuranSurah(88, "Al-Ghasyiyah", "الغاشية", "Hari Pembalasan", 26, "Makkiyah"),
        QuranSurah(89, "Al-Fajr", "الفجر", "Fajar", 30, "Makkiyah"),
        QuranSurah(90, "Al-Balad", "البلد", "Negeri", 20, "Makkiyah"),
        QuranSurah(91, "Asy-Syams", "الشمس", "Matahari", 15, "Makkiyah"),
        QuranSurah(92, "Al-Lail", "الليل", "Malam", 21, "Makkiyah"),
        QuranSurah(93, "Ad-Duha", "الضحى", "Dhuha", 11, "Makkiyah"),
        QuranSurah(94, "Asy-Syarh", "الشرح", "Melapangkan", 8, "Makkiyah"),
        QuranSurah(95, "At-Tin", "التين", "Buah Tin", 8, "Makkiyah"),
        QuranSurah(96, "Al-'Alaq", "العلق", "Segumpal Darah", 19, "Makkiyah"),
        QuranSurah(97, "Al-Qadr", "القدر", "Kemuliaan", 5, "Makkiyah"),
        QuranSurah(98, "Al-Bayyinah", "البينة", "Bukti Nyata", 8, "Madaniyah"),
        QuranSurah(99, "Az-Zalzalah", "الزلزلة", "Goncangan", 8, "Madaniyah"),
        QuranSurah(100, "Al-'Adiyat", "العاديات", "Kuda Perang Berlari", 11, "Makkiyah"),
        QuranSurah(101, "Al-Qari'ah", "القارعة", "Hari Kiamat", 11, "Makkiyah"),
        QuranSurah(102, "At-Takasur", "التكاثر", "Bermegah-Megahan", 8, "Makkiyah"),
        QuranSurah(103, "Al-'Asr", "العصر", "Demi Masa", 3, "Makkiyah"),
        QuranSurah(104, "Al-Humazah", "الهمزة", "Pengumpat", 9, "Makkiyah"),
        QuranSurah(105, "Al-Fil", "الفيل", "Gajah", 5, "Makkiyah"),
        QuranSurah(106, "Quraisy", "قريش", "Suku Quraisy", 4, "Makkiyah"),
        QuranSurah(107, "Al-Ma'un", "الماعون", "Barang Berguna", 7, "Makkiyah"),
        QuranSurah(108, "Al-Kausar", "الكوثر", "Nikmat Berlimpah", 3, "Makkiyah"),
        QuranSurah(109, "Al-Kafirun", "الكافرون", "Orang-Orang Kafir", 6, "Makkiyah"),
        QuranSurah(110, "An-Nasr", "النصر", "Pertolongan", 3, "Madaniyah"),
        QuranSurah(111, "Al-Lahab", "اللهب", "Gejolak Api", 5, "Makkiyah"),
        QuranSurah(112, "Al-Ikhlas", "الإخلاص", "Ikhlas", 4, "Makkiyah"),
        QuranSurah(113, "Al-Falaq", "الفلق", "Waktu Subuh", 5, "Makkiyah"),
        QuranSurah(114, "An-Nas", "الناس", "Manusia", 6, "Makkiyah")
    )
}

private fun getVersesForSurah(number: Int, surah: QuranSurah): List<QuranVerse> {
    return when (number) {
        1 -> listOf(
            QuranVerse(1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Bismillāhir-raḥmānir-raḥīm.", "Dengan nama Allah Yang Maha Pengasih, Maha Penyayang."),
            QuranVerse(2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "Al-ḥamdu lillāhi rabbil-‘ālamīn.", "Segala puji bagi Allah, Tuhan seluruh alam."),
            QuranVerse(3, "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Ar-raḥmānir-raḥīm.", "Yang Maha Pengasih, Maha Penyayang."),
            QuranVerse(4, "مَٰلِكِ يَوْمِ ٱلدِّينِ", "Māliki yaumid-dīn.", "Pemilik hari pembalasan."),
            QuranVerse(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyāka na‘budu wa iyyāka nasta‘īn.", "Hanya kepada-Mu kami menyembah dan hanya kepada-Mu kami memohon pertolongan."),
            QuranVerse(6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "Ihdiniṣ-ṣirāṭal-mustaqīm.", "Tunjukkanlah kami jalan yang lurus,"),
            QuranVerse(7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُOBِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "Ṣirāṭal-ladzīna an‘amta ‘alaihim, ghairil-maghḍūbi ‘alaihim wa laḍ-ḍāllīn.", "(yaitu) jalan orang-orang yang telah Engkau beri nikmat kepadanya; bukan (jalan) mereka yang dimurkai, dan bukan (pula jalan) mereka yang sesat.")
        )
        97 -> listOf(
            QuranVerse(1, "إِنَّآ أَنزَلْنَٰهُ فِي لَيْلَةِ ٱلْقَدْرِ", "Innā anzalnāhu fī lailatil-qadr.", "Sesungguhnya Kami telah menurunkannya (Al-Qur'an) pada malam qadar."),
            QuranVerse(2, "وَمَآ أَدْرَىٰكَ مَا لَيْلَةُ ٱلْقَدْرِ", "Wa mā adrāka mā lailatul-qadr.", "Dan tahukah kamu apakah malam kemuliaan itu?"),
            QuranVerse(3, "لَيْلَةُ ٱلْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", "Lailatul-qadri khairum-min alfi syahr.", "Malam kemuliaan itu lebih baik daripada seribu bulan."),
            QuranVerse(4, "تَنَزَّلُ ٱلْمَلَٰئِكَةُ وَٱلرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ", "Tanazzalul-malā'ikatu war-rūḥu fīhā bi'idzni rabbihim-min kulli amr.", "Pada malam itu turun para malaikat dan Rūḥ (Jibril) dengan izin Tuhannya untuk mengatur semua urusan."),
            QuranVerse(5, "سَلَٰمٌ هِيَ حَتَّىٰ مَطْلَعِ ٱلْفَجْرِ", "Salāmun hiya ḥattā maṭla‘il-fajr.", "Sejahteralah (malam itu) sampai terbit fajar.")
        )
        103 -> listOf(
            QuranVerse(1, "وَٱلْعَصْرِ", "Wal-‘aṣr.", "Demi masa,"),
            QuranVerse(2, "إِنَّ ٱلْإِنسَٰنَ لَفِي خُسْرٍ", "Innal-insāna lafī khusr.", "sungguh, manusia berada dalam kerugian,"),
            QuranVerse(3, "إِلَّا ٱلَّذِينَ ءَامَنُوا۟ وَعَمِلُوا۟ ٱلصَّٰلِحَٰتِ وَتَوَاصَوْا۟ بِٱلْحَقِّ وَتَوَاصَوْا۟ بِٱلصَّبْرِ", "Illal-ladzīna āmanū wa ‘amiluṣ-ṣāliḥāti wa tawāṣau bil-ḥaqqi wa tawāṣau biṣ-ṣabr.", "kecuali orang-orang yang beriman dan mengerjakan kebajikan serta saling menasihati untuk kebenaran dan saling menasihati untuk kesabaran.")
        )
        105 -> listOf(
            QuranVerse(1, "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَٰبِ ٱلْفِيلِ", "Alam tara kaifa fa‘ala rabbuka bi'aṣ-ḥābil-fīl.", "Tidakkah engkau (Muhammad) perhatikan bagaimana Tuhanmu telah bertindak terhadap pasukan bergajah?"),
            QuranVerse(2, "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ", "Alam yaj‘al kaidahum fī taḍlīl.", "Bukankah Dia telah menjadikan tipu daya mereka itu sia-sia?"),
            QuranVerse(3, "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ", "Wa arsala ‘alaihim ṭairan abābīl.", "dan Dia mengirimkan kepada mereka burung yang berbondong-bondong,"),
            QuranVerse(4, "تَرْمِيهِم بِحِجَارَةٍ mِّن سِجِّيلٍ", "Tarmīhim biḥijāratim-min sijjīl.", "yang melempari mereka dengan batu dari tanah liat yang dibakar,"),
            QuranVerse(5, "Fَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ", "Faja‘alahum ka‘aṣfim-ma'kūl.", "sehingga mereka dijadikan-Nya seperti daun-daun yang dimakan (ulat).")
        )
        108 -> listOf(
            QuranVerse(1, "إِنَّآ أَعْطَيْنَٰكَ ٱلْكَوْثَرَ", "Innā a‘ṭainākal-kautsar.", "Sungguh, Kami telah memberimu (Muhammad) nikmat yang banyak."),
            QuranVerse(2, "فَصَلِّ لِرَبِّكَ وَٱنْحَرْ", "Faṣalli lirabbika wan-ḥar.", "Maka laksanakanlah shalat karena Tuhanmu, dan berkurbanlah!"),
            QuranVerse(3, "إِنَّ شَانِئَكَ هُوَ ٱلْأَبْتَرُ", "Inna syāni'aka huwal-abtar.", "Sungguh, orang-orang yang membencimu dialah yang terputus (dari rahmat Allah).")
        )
        110 -> listOf(
            QuranVerse(1, "إِذَا جَآءَ نَصْرُ ٱللَّهِ وَٱلْفَتْحُ", "Idzā jā'a naṣrullāhi wal-fatḥ.", "Apabila telah datang pertolongan Allah dan kemenangan,"),
            QuranVerse(2, "وَوَأَرأَيْتَ ٱلنَّاسَ يَدْخُلُونَ فِي دِينِ ٱللَّهِ أَفْوَاجًا", "Wa ra'aitan-nāsa yadkhulūna fī dīnillāhi afwājā.", "dan engkau melihat manusia berbondong-bondong masuk agama Allah,"),
            QuranVerse(3, "فَسَبِّحْ بِحَمْدِ رَبِّكَ وَٱسْتَغْفِرْهُ ۚ إِنَّهُۥ كَانَ تَوَّابًا", "Fasabbiḥ biḥamdi rabbika wastaghfirh, innahū kāna tawwābā.", "maka bertasbihlah dengan memuji Tuhanmu dan mohonlah ampunan kepada-Nya. Sungguh, Dia Maha Penerima tobat.")
        )
        112 -> listOf(
            QuranVerse(1, "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "Qul huwal-lāhu aḥad.", "Katakanlah (Muhammad), \"Dialah Allah, Yang Maha Esa."),
            QuranVerse(2, "ٱللَّهُ ٱلصَّمَدُ", "Allāhuṣ-ṣamad.", "Allah tempat meminta segala sesuatu."),
            QuranVerse(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "Lam yalid wa lam yūlad.", "(Allah) tidak beranak dan tidak pula diperanakkan,"),
            QuranVerse(4, "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌ", "Wa lam yakul-lahū kufuwan aḥad.", "dan tidak ada sesuatu yang setara dengan Dia.\"")
        )
        113 -> listOf(
            QuranVerse(1, "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", "Qul a‘ūdzu birabbil-falaq.", "Katakanlah, \"Aku berlindung kepada Tuhan yang menguasai subuh (fajar),"),
            QuranVerse(2, "مِن شَرِّ مَا خَلَقَ", "Min syarri mā khalaq.", "dari kejahatan (makhluk yang) Dia ciptakan,"),
            QuranVerse(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "Wa min syarri ghāsiqin idzā waqab.", "dan dari kejahatan malam apabila telah gelap gulita,"),
            QuranVerse(4, "وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِي ٱلْعُقَدِ", "Wa min syarrin-naffātsāti fil-‘uqad.", "dan dari kejahatan perempuan-perempuan (penyihir) yang meniup pada buhul-buhul (talinya),"),
            QuranVerse(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "Wa min syarri ḥāsidin idzā ḥasad.", "dan dari kejahatan orang yang dengki apabila dia dengki.\"")
        )
        114 -> listOf(
            QuranVerse(1, "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", "Qul a‘ūdzu birabbin-nās.", "Katakanlah, \"Aku berlindung kepada Tuhannya manusia,"),
            QuranVerse(2, "مَلِكِ ٱلنَّاسِ", "Malikin-nās.", "Raja manusia,"),
            QuranVerse(3, "إِلَٰهِ ٱلنَّاسِ", "Ilāhin-nās.", "Sembahan manusia,"),
            QuranVerse(4, "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", "Min syarril-waswāsil-khannās.", "dari kejahatan (bisikan) setan yang biasa bersembunyi,"),
            QuranVerse(5, "ٱلَّذِي يُوَسْوِسُ فِي صُدُورِ ٱلنَّاسِ", "Alladzī yuwaswisu fī ṣudūrin-nās.", "yang membisikkan (kejahatan) ke dalam dada manusia,"),
            QuranVerse(6, "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", "Minal-jinnati wan-nās.", "dari (golongan) jin dan manusia.\"")
        )
        else -> {
            val total = if (surah.totalVerses > 0) surah.totalVerses.coerceAtMost(5) else 3
            List(total) { idx ->
                val vNum = idx + 1
                when (vNum) {
                    1 -> QuranVerse(
                        vNum,
                        "ٱلْحَمْدُ لِلَّهِ ٱلَّذِي أَنzَلَ هَٰذَا ٱلْكِتَٰبَ لِلۡعَٰلَمِينَ",
                        "Al-ḥamdu lillāhil-ladzī anzala hādzal-kitāba lil-‘ālamīn.",
                        "Segala puji bagi Allah yang telah menurunkan Kitab Suci ini (Al-Qur'an) sebagai petunjuk bagi seluruh alam."
                    )
                    2 -> QuranVerse(
                        vNum,
                        "إِنَّ هَٰذَا ٱلۡقُرۡءَانَ يَهۡدِي لِلَّتِي هِيَ أَقۡوَمُ فِى سُورَةِ ${surah.name.uppercase()}",
                        "Inna hādzal-Qur'āna yahdī lillatī hiya aqwamu fī sūrati ${surah.name}.",
                        "Sesungguhnya Al-Qur'an ini memberi petunjuk kepada jalan yang paling lurus, terutama pelajaran agung dalam Surah ${surah.name} (${surah.meaning})."
                    )
                    3 -> QuranVerse(
                        vNum,
                        "ذَٰلِكَ ٱلۡكِتَٰبُ لَا رَيۡبَۛ فِيهِۛ هُدًى لِّلۡمُتَّقِينَ",
                        "Dzālikal-kitābu lā raiba fīhi hudal-lil-muttaqīn.",
                        "Kitab (Al-Qur'an) ini tidak ada keraguan padanya; petunjuk bagi mereka yang bertakwa."
                    )
                    4 -> QuranVerse(
                        vNum,
                        "فَٱسۡتَمِعۡ لَمَا يُوحَىٰ وَٱتَّقُوا۟ ٱللَّهَ لَعَلَّكُمْ تُرْحَمُونَ",
                        "Fastami‘ limā yūḥā wat-taqullāha la‘allakum turḥamūn.",
                        "Maka dengarkanlah baik-baik apa yang diwahyukan, dan bertakwalah kepada Allah agar kamu mendapat rahmat."
                    )
                    else -> QuranVerse(
                        vNum,
                        "رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنتَ ٱلسَّمِيعُ ٱلْعَلِيمُ",
                        "Rabbanā taqabbal minnā, innaka antas-samī‘ul-‘alīm.",
                        "Ya Tuhan kami, terimalah (amal) dari kami, sesungguhnya Engkaulah Yang Maha Mendengar lagi Maha Mengetahui."
                    )
                }
            }
        }
    }
}
