package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Helper unduhan yang aman menggantikan HttpURLConnection rentan.
 * Menggunakan OkHttp untuk verifikasi sertifikat SSL bawaan secara ketat,
 * melakukan pengecekan domain whitelist, melacak progress, serta memverifikasi file MP3.
 */
object SecureDownloadHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Mengunduh berkas Adzan secara aman dari internet.
     * @param context Context Android
     * @param url URL asal unduhan
     * @param targetFile File tujuan akhir penyimpanan lokal
     * @param onProgress Callback untuk memberikan progress unduhan (0.0f hingga 1.0f atau -1.0f jika ukuran tidak diketahui)
     * @return Result berisi sukses atau kegagalan
     */
    suspend fun downloadAdzan(
        context: Context,
        url: String,
        targetFile: File,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Deteksi URL terpercaya (Domain Whitelist)
        if (!TrustedAdzanDomains.isUrlTrusted(url)) {
            return@withContext Result.failure(SecurityException("Domain '$url' diblokir demi keamanan! Hanya domain tepercaya yang diperbolehkan."))
        }

        val tmpFile = File(context.filesDir, "${targetFile.name}.tmp")
        try {
            if (tmpFile.exists()) tmpFile.delete()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AdzanApp-SecureDownloader/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error ${response.code} - ${response.message}"))
                }

                val body = response.body ?: return@withContext Result.failure(Exception("Konten respons kosong"))
                val contentLength = body.contentLength()

                // Proteksi awal jika ukuran Content-Length dari server melebihi batas
                if (contentLength > FileSecurityUtils.MAX_SIZE_BYTES) {
                    return@withContext Result.failure(Exception("Ukuran berkas di server melebihi batas maksimum 50MB."))
                }

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(tmpFile)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead

                    // Proteksi dinamis (mencegah payload tanpa Content-Length yang terus bertambah / Zip Bomb)
                    if (totalBytesRead > FileSecurityUtils.MAX_SIZE_BYTES) {
                        outputStream.close()
                        tmpFile.delete()
                        return@withContext Result.failure(Exception("Unduhan dibatalkan: Ukuran berkas melampaui batas 50MB."))
                    }

                    outputStream.write(buffer, 0, bytesRead)

                    if (contentLength > 0) {
                        onProgress(totalBytesRead.toFloat() / contentLength.toFloat())
                    } else {
                        // Kembalikan -1f untuk menandakan progress tidak dapat diukur
                        onProgress(-1f)
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // 2. Validasi keutuhan dan tipe berkas terunduh
                if (tmpFile.length() < FileSecurityUtils.MIN_SIZE_BYTES) {
                    tmpFile.delete()
                    return@withContext Result.failure(Exception("File unduhan terlalu kecil (minimal 10KB)"))
                }

                if (!FileSecurityUtils.isValidMp3File(tmpFile)) {
                    tmpFile.delete()
                    return@withContext Result.failure(Exception("Berkas terunduh tidak lolos verifikasi format MP3 (Gagal Magic Bytes)."))
                }

                // 3. Pindahkan file temp secara aman ke target akhir
                if (targetFile.exists()) targetFile.delete()
                if (!tmpFile.renameTo(targetFile)) {
                    tmpFile.copyTo(targetFile, overwrite = true)
                    tmpFile.delete()
                }

                Result.success(Unit)
            }
        } catch (e: Exception) {
            if (tmpFile.exists()) tmpFile.delete()
            Result.failure(e)
        }
    }
}
