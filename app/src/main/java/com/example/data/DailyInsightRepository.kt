package id.ideahousetech.prayertime_qibla.data

import id.ideahousetech.prayertime_qibla.model.DailyInsightItem
import id.ideahousetech.prayertime_qibla.model.InsightType
import java.util.Calendar

/**
 * Repositori penyedia konten Daily Insight Islami secara Offline-First.
 * Menggunakan kalkulasi deterministik berbasis tanggal kalender harian (dayOfYear)
 * untuk memilih satu set Ayat, Hadits, dan Doa unik setiap hari dari bank data kurasi otentik.
 */
class DailyInsightRepository {

    // Bank data kurasi Ayat Hari Ini
    private val ayatBank = listOf(
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Kemudahan Setelah Kesulitan",
            arabic = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا • إِنَّ مَعَ الْعُSْرِ يُسْرًا",
            latin = "Fa inna ma'al 'usri yusraa, inna ma'al 'usri yusraa.",
            translation = "Karena sesungguhnya sesudah kesulitan itu ada kemudahan, sesungguhnya sesudah kesulitan itu ada kemudahan.",
            reference = "QS. Al-Insyirah: 5-6",
            category = "Harapan & Sabar"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Kedekatan Allah dengan Hamba-Nya",
            arabic = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            latin = "Wa idzaa sa'alaka 'ibaadii 'annii fa innii qariib, ujiibu da'watad-daa'i idzaa da'aan.",
            translation = "Dan apabila hamba-hamba-Ku bertanya kepadamu tentang Aku, maka (jawablah), bahwasanya Aku adalah dekat. Aku mengabulkan permohonan orang yang berdoa apabila ia memohon kepada-Ku.",
            reference = "QS. Al-Baqarah: 186",
            category = "Doa & Kedekatan"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Kewajiban Bersyukur",
            arabic = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            latin = "Fadzkuruunii adzkurkum wasykuruu lii wa laa takfuruun.",
            translation = "Ingatlah kamu kepada-Ku niscaya Aku ingat kepadamu, dan bersyukurlah kepada-Ku, dan janganlah kamu mengingkari (nikmat)-Ku.",
            reference = "QS. Al-Baqarah: 152",
            category = "Syukur"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Sabar dan Kemenangan",
            arabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ",
            latin = "Yaa ayyuhal-ladziina aamanus-ta'iinuu bish-shabri wash-shalaah, innallaaha ma'ash-shaabiriin.",
            translation = "Wahai orang-orang yang beriman! Mohonlah pertolongan (kepada Allah) dengan sabar dan sholat. Sungguh, Allah beserta orang-orang yang sabar.",
            reference = "QS. Al-Baqarah: 153",
            category = "Sabar & Sholat"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Ujian Hidup Sesuai Kemampuan",
            arabic = "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            latin = "Laa yukallifullaahu nafsan illaa wus'ahaa.",
            translation = "Allah tidak membebani seseorang melainkan sesuai dengan kesanggupannya.",
            reference = "QS. Al-Baqarah: 286",
            category = "Kekuatan Jiwa"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Ketenangan Melalui Dzikir",
            arabic = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُمْ بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            latin = "Alladziina aamanuu wa tathma-innu quluubuhum bidzikrillahi, alaa bidzikrillaahi tathma-innul quluub.",
            translation = "Yaitu orang-orang yang beriman dan hati mereka merasa tentram dengan mengingat Allah. Ingatlah, hanya dengan mengingati Allah-lah hati menjadi tentram.",
            reference = "QS. Ar-Ra'd: 28",
            category = "Ketenangan Hati"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Kebaikan yang Sekecil Zarah",
            arabic = "فَمَنْ يَعْمَلْ مِثْقَالَ ذَرَّةٍ خَيْرًا يَرَهُ • وَمَنْ يَعْمَلْ مِثْقَالَ ذَرَّةٍ شَرًّا يَرَهُ",
            latin = "Faman ya'mal mithqaala dzarratin khayray yarah, wa man ya'mal mithqaala dzarratin syarray yarah.",
            translation = "Barangsiapa yang mengerjakan kebaikan seberat dzarrahpun, niscaya dia akan melihat (balasan)nya. Dan barangsiapa yang mengerjakan kejahatan sebesar dzarrahpun, niscaya dia akan melihat (balasan)nya.",
            reference = "QS. Az-Zalzalah: 7-8",
            category = "Keadilan & Beramal"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Kelebihan Orang Bertakwa",
            arabic = "وَمَنْ يَتَّقِ اللَّهَ يَجْعَلْ لَهُ مَخْرَجًا • وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
            latin = "Wa man yattaqillaaha yaj'al lahuu makhrajaa, wa yarzuqhu min haytsu laa yahtasib.",
            translation = "Barangsiapa bertakwa kepada Allah niscaya Dia akan mengadakan baginya jalan keluar. Dan memberinya rezeki dari arah yang tiada disangka-sangkanya.",
            reference = "QS. At-Thalaq: 2-3",
            category = "Tawakal & Rezeki"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Balasan Berbuat Baik",
            arabic = "هَلْ جَزَاءُ الْإِحْسَانِ إِلَّا الْإِحْسَانُ",
            latin = "Hal jazaau-il ihsaani illal ihsaan.",
            translation = "Tidak ada balasan untuk kebaikan selain kebaikan (pula).",
            reference = "QS. Ar-Rahman: 60",
            category = "Kebajikan"
        ),
        DailyInsightItem(
            type = InsightType.AYAT,
            title = "Pintu Ampunan Allah yang Luas",
            arabic = "۞ قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
            latin = "Qul yaa 'ibaadiyal-ladziina asrafuu 'alaa anfusihim laa taqnathuu mir rahmatillaahi, innallaaha yaghfirudz-dzunuuba jamii'aa.",
            translation = "Katakanlah: 'Wahai hamba-hamba-Ku yang malampaui batas terhadap diri mereka sendiri, janganlah kamu berputus asa dari rahmat Allah. Sesungguhnya Allah mengampuni dosa-dosa semuanya.'",
            reference = "QS. Az-Zumar: 53",
            category = "Ampunan & Harapan"
        )
    )

    // Bank data kurasi Hadits Hari Ini
    private val haditsBank = listOf(
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Pentingnya Menjaga Lisan",
            arabic = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ",
            translation = "Barangsiapa yang beriman kepada Allah dan hari akhir, hendaklah ia berkata yang baik atau diam.",
            reference = "HR. Bukhari & Muslim",
            category = "Adab & Bicara"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Keindahan Memiliki Akhlak Mulia",
            arabic = "إِنَّ مِنْ أَحَبِّكُمْ إِلَيَّ وَأَقْرَبِكُمْ مِنِّي مَجْلِسًا يَوْمَ الْقِيَامَةِ أَحَاسِنَكُمْ أَخْلاَقًا",
            translation = "Sesungguhnya yang paling aku cintai di antara kalian dan yang paling dekat majelisnya denganku pada hari kiamat adalah yang paling baik akhlaknya.",
            reference = "HR. Tirmidzi",
            category = "Akhlak"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Segala Amal Bergantung Niat",
            arabic = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            translation = "Sesungguhnya setiap amalan itu bergantung kepada niatnya, dan sesungguhnya setiap orang akan mendapatkan apa yang ia niatkan.",
            reference = "HR. Bukhari",
            category = "Niat & Ikhlas"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Cinta Kasih Kepada Saudara",
            arabic = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ",
            translation = "Tidak sempurna iman salah seorang di antara kalian sampai ia mencintai untuk saudaranya apa yang ia cintai untuk dirinya sendiri.",
            reference = "HR. Bukhari & Muslim",
            category = "Persaudaraan"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Keutamaan Menuntut Ilmu",
            arabic = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ",
            translation = "Barangsiapa menempuh suatu jalan untuk mencari ilmu, maka Allah akan memudahkan baginya jalan menuju surga.",
            reference = "HR. Muslim",
            category = "Ilmu"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Larangan Marah berlebihan",
            arabic = "لاَ تَغْضَبْ وَلَكَ الْجَنَّةُ",
            translation = "Janganlah kamu marah, dan bagimu adalah surga.",
            reference = "HR. Thabrani",
            category = "Sabar & Kontrol"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Tangan di Atas Lebih Baik",
            arabic = "الْيَدُ الْعُلْيَا خَيْرٌ مِنَ الْيَدِ السُّفْلَى",
            translation = "Tangan yang di atas (pemberi) lebih baik daripada tangan yang di bawah (penerima).",
            reference = "HR. Bukhari",
            category = "Sedekah"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Menghilangkan Kesusahan Orang Lain",
            arabic = "مَنْ نَفَّسَ عَنْ مُؤْمِنٍ كُرْبَةً مِنْ كُرَبِ الدُّنْيَا نَفَّسَ اللَّهُ عَنْهُ كُرْبَةً مِنْ كُرَبِ يَوْمِ الْقِيَامَةِ",
            translation = "Barangsiapa menghilangkan satu kesusahan dunia dari seorang mukmin, maka Allah akan menghilangkan darinya satu kesusahan di hari kiamat.",
            reference = "HR. Muslim",
            category = "Kepedulian Sosial"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Sifat Malu Adalah Bagian Iman",
            arabic = "الْحَيَاءُ شُعْبَةٌ مِنَ الإِيمَانِ",
            translation = "Sifat malu itu adalah salah satu cabang dari keimanan.",
            reference = "HR. Bukhari",
            category = "Adab & Iman"
        ),
        DailyInsightItem(
            type = InsightType.HADITS,
            title = "Senyum Adalah Sedekah",
            arabic = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ",
            translation = "Senyumanmu di hadapan saudaramu adalah sedekah bagimu.",
            reference = "HR. Tirmidzi",
            category = "Kasih Sayang"
        )
    )

    // Bank data kurasi Doa Hari Ini
    private val doaBank = listOf(
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Sapu Jagad (Segenap Kebaikan)",
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            latin = "Rabbanaa aatinaa fid-dun-yaa hasanataw wa fil-aakhirati hasanataw wa qinaa 'adzaaban-naar.",
            translation = "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat, serta peliharalah kami dari siksaan api neraka.",
            reference = "QS. Al-Baqarah: 201",
            category = "Perlindungan & Sukses"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Keteguhan Iman & Agama",
            arabic = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ",
            latin = "Ya muqallibal-quluubi tsabbit qalbii 'alaa diinik.",
            translation = "Wahai Dzat yang membolak-balikkan hati, tetapkanlah hatiku di atas agama-Mu.",
            reference = "HR. Tirmidzi",
            category = "Keteguhan Iman"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Penerang dan Kemudahan Urusan",
            arabic = "رَبِّ اشْرَحْ لِي صَدْرِي • وَيَسِّرْ لِي أَمْرِي • وَاحْلُلْ عُقْدَةً مِنْ لِسَانِي • يَفْقَهُوا قَوْلِي",
            latin = "Rabbisy-syrahlii shadrii, wa yassir lii amrii, wahlul 'uqdatam mil-lisaanii, yafqahuu qawlii.",
            translation = "Ya Rabbku, lapangkanlah dadaku, mudahkanlah urusanku, dan lepaskanlah kekakuan lidahku agar mereka mengerti perkataanku.",
            reference = "QS. Thaha: 25-28",
            category = "Kemudahan Hidup"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Memohon Ampunan Ibu Bapak",
            arabic = "رَبِّ اغْفِرْ لِي وَلِوَالِدَيَّ وَارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيرًا",
            latin = "Rabbighfir lii waliwaalidayya warhamhumaa kamaa rabbayaanii shaghiiraa.",
            translation = "Ya Tuhanku, ampunilah dosaku dan dosa kedua orang tuaku, serta sayangilah mereka sebagaimana mereka mendidikku di waktu kecil.",
            reference = "Doa Bakti",
            category = "Ibu Bapak"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Perlindungan Sihir & Keburukan",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            latin = "A'uudzu bikalimaatillaahit-taammaati min syarri maa khalaq.",
            translation = "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari kejahatan makhluk yang diciptakan-Nya.",
            reference = "HR. Muslim",
            category = "Perlindungan Harian"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Memohon Ilmu yang Berkah",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا وَرِزْقًا طَيِّبًا وَعَمَلًا مُتَقَبَّلًا",
            latin = "Allaahumma innii as-aluka 'ilman naafi'an, wa rizqan thayyiban, wa 'amalan mutaqabbalan.",
            translation = "Ya Allah, sesungguhnya aku memohon kepada-Mu ilmu yang bermanfaat, rezeki yang baik, dan amalan yang diterima.",
            reference = "HR. Ibnu Majah",
            category = "Ilmu & Rezeki"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Menolak Rasa Malas dan Cemas",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ وَالْعَجْزِ وَالْكَسَلِ",
            latin = "Allaahumma innii a'uudzu bika minal hammi wal hazani wal 'ajzi wal kasal.",
            translation = "Ya Allah, sesungguhnya aku berlindung kepada-Mu dari keluh kesah dan kesedihan, dari kelemahan dan kemalasan.",
            reference = "HR. Bukhari",
            category = "Kesehatan Mental"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Pasrah Bahagia (Tawakal)",
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            latin = "Bismillaahi tawakkaltu 'alallaahi laa hawla wa laa quwwata illaa billaah.",
            translation = "Dengan nama Allah, aku bertawakal kepada Allah. Tiada daya dan kekuatan kecuali dengan bantuan Allah.",
            reference = "HR. Abu Daud",
            category = "Tawakal"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Agar Bersyukur Berkelimpahan",
            arabic = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ",
            latin = "Rabbi aw zi'nii an asykura ni'matakal-latii an'amta 'alayya.",
            translation = "Ya Tuhanku, berilah aku ilham dan petunjuk untuk tetap mensyukuri nikmat-Mu yang telah Engkau anugerahkan kepadaku.",
            reference = "QS. An-Naml: 19",
            category = "Syukur & Ridho"
        ),
        DailyInsightItem(
            type = InsightType.DOA,
            title = "Doa Memohon Keselamatan Jiwa Raga",
            arabic = "اللَّهُمَّ عافِني في بَدَني، اللَّهُمَّ عافِني في سَمْعي، اللَّهُمَّ عافِني في بَصَري",
            latin = "Allaahumma 'aafinii fii badanii, allaahumma 'aafinii fii sam'ii, allaahumma 'aafinii fii basharii.",
            translation = "Ya Allah, sehatkanlah badanku, Ya Allah, sehatkanlah pendengaranku, Ya Allah, sehatkanlah penglihatanku.",
            reference = "HR. Abu Daud",
            category = "Kesehatan & Waras"
        )
    )

    /**
     * Memperoleh konten Ayat, Hadits, dan Doa untuk hari ini secara luring.
     * Menggunakan index hari dalam setahun (Calendar.DAY_OF_YEAR) untuk rotasi deterministik harian yang stabil.
     */
    fun getDailyInsightsForToday(): List<DailyInsightItem> {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Ambil elemen secara deterministik berdasarkan hari saat ini
        val ayatIndex = (dayOfYear + 3) % ayatBank.size
        val haditsIndex = (dayOfYear + 7) % haditsBank.size
        val doaIndex = (dayOfYear + 11) % doaBank.size

        return listOf(
            ayatBank[ayatIndex],
            haditsBank[haditsIndex],
            doaBank[doaIndex]
        )
    }
}
