package id.ideahousetech.prayertime_qibla.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Status pemutaran audio murottal per ayat.
 */
enum class VersePlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    ERROR
}

/**
 * Menghasilkan URL audio tilawah dari EveryAyah.com (Syaikh Mishary Rashid Alafasy).
 * Format URL: https://everyayah.com/data/Alafasy_128kbps/{SSSAAA}.mp3
 * @param surahNumber Nomor surah (1 - 114)
 * @param verseNumber Nomor ayat (1 - n)
 */
fun getVerseAudioUrl(surahNumber: Int, verseNumber: Int): String {
    val sss = "%03d".format(surahNumber)
    val aaa = "%03d".format(verseNumber)
    return "https://everyayah.com/data/Alafasy_128kbps/$sss$aaa.mp3"
}

/**
 * Pengelola pemutar audio murottal per ayat menggunakan Android MediaPlayer.
 * Menjamin hanya ada 1 audio yang diputar dalam satu waktu.
 */
class VerseAudioPlayerManager {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null

    var currentSurah: Int? = null
        private set
    var currentVerse: Int? = null
        private set

    /**
     * Memutar audio ayat dari surah dan nomor ayat yang ditentukan.
     * Jika sebelumnya ada audio yang sedang aktif, akan dihentikan dan di-release terlebih dahulu.
     */
    fun play(
        surahNumber: Int,
        verseNumber: Int,
        onStateChanged: (VersePlaybackState) -> Unit,
        onError: (String) -> Unit
    ) {
        stop()

        currentSurah = surahNumber
        currentVerse = verseNumber
        dispatchState(onStateChanged, VersePlaybackState.LOADING)

        val audioUrl = getVerseAudioUrl(surahNumber, verseNumber)

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioUrl)
                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                        dispatchState(onStateChanged, VersePlaybackState.PLAYING)
                    } catch (e: Exception) {
                        Log.e("VerseAudioPlayer", "Gagal memulai playback audio: ${e.message}", e)
                        stop()
                        dispatchState(onStateChanged, VersePlaybackState.ERROR)
                        dispatchError(onError, "Gagal memutar audio ayat")
                    }
                }
                setOnCompletionListener {
                    stop()
                    dispatchState(onStateChanged, VersePlaybackState.IDLE)
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("VerseAudioPlayer", "MediaPlayer error: what=$what, extra=$extra untuk URL $audioUrl")
                    stop()
                    dispatchState(onStateChanged, VersePlaybackState.ERROR)
                    dispatchError(onError, "Gagal memutar audio ayat, periksa koneksi internet")
                    true
                }
            }

            mediaPlayer = player
            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("VerseAudioPlayer", "Gagal inisialisasi MediaPlayer untuk $audioUrl: ${e.message}", e)
            stop()
            dispatchState(onStateChanged, VersePlaybackState.ERROR)
            dispatchError(onError, "Gagal memuat audio ayat, periksa koneksi internet")
        }
    }

    /**
     * Menghentikan audio yang sedang berjalan dan merilis MediaPlayer.
     */
    fun stop() {
        val player = mediaPlayer
        mediaPlayer = null
        currentSurah = null
        currentVerse = null

        if (player != null) {
            try {
                if (player.isPlaying) {
                    player.stop()
                }
            } catch (e: Exception) {
                Log.w("VerseAudioPlayer", "Error saat memanggil player.stop(): ${e.message}")
            } finally {
                try {
                    player.reset()
                    player.release()
                } catch (e: Exception) {
                    Log.w("VerseAudioPlayer", "Error saat me-release player: ${e.message}")
                }
            }
        }
    }

    /**
     * Merilis seluruh resource audio ketika composable di-dispose.
     */
    fun release() {
        stop()
    }

    private fun dispatchState(callback: (VersePlaybackState) -> Unit, state: VersePlaybackState) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback(state)
        } else {
            mainHandler.post { callback(state) }
        }
    }

    private fun dispatchError(callback: (String) -> Unit, message: String) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            callback(message)
        } else {
            mainHandler.post { callback(message) }
        }
    }
}
