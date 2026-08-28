package id.ideahousetech.prayertime_qibla.service

import id.ideahousetech.prayertime_qibla.ui.QuranVerse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

class QuranService {

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(SecurityInterceptor())
        .hostnameVerifier { hostname, session ->
            val defaultVerifier = javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier()
            defaultVerifier.verify(hostname, session) && (hostname == "equran.id" || hostname.endsWith(".equran.id"))
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val equranApi = Retrofit.Builder()
        .baseUrl("https://equran.id/")
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(EQuranApi::class.java)

    suspend fun getSurahDetail(number: Int): List<QuranVerse>? = withContext(Dispatchers.IO) {
        try {
            val response = equranApi.getSurahDetail(number)
            if (response.code == 200 && response.data != null) {
                return@withContext response.data.ayat.map { item ->
                    QuranVerse(
                        verseNumber = item.nomorAyat,
                        arabic = item.teksArab,
                        latin = item.teksLatin,
                        translation = item.teksIndonesia
                    )
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

interface EQuranApi {
    @GET("api/v2/surat/{number}")
    suspend fun getSurahDetail(@Path("number") number: Int): EQuranResponse
}

@JsonClass(generateAdapter = true)
data class EQuranResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: EQuranSuratDetail?
)

@JsonClass(generateAdapter = true)
data class EQuranSuratDetail(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "ayat") val ayat: List<EQuranAyat>
)

@JsonClass(generateAdapter = true)
data class EQuranAyat(
    @Json(name = "nomorAyat") val nomorAyat: Int,
    @Json(name = "teksArab") val teksArab: String,
    @Json(name = "teksLatin") val teksLatin: String,
    @Json(name = "teksIndonesia") val teksIndonesia: String
)

