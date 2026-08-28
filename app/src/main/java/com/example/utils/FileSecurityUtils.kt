package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Hasil validasi file.
 */
sealed class FileValidationResult {
    object Valid : FileValidationResult()
    data class Invalid(val reason: String) : FileValidationResult()
}

/**
 * Utilitas keamanan siber untuk memvalidasi dan mensanitasi berkas audio
 * guna mencegah serangan Path Traversal, Malicious File Upload, dan Storage Exhaustion.
 */
object FileSecurityUtils {

    const val MAX_SIZE_BYTES = 50 * 1024 * 1024 // Batas ukuran file 50 MB
    const val MIN_SIZE_BYTES = 10 * 1024        // Batas ukuran file minimal 10 KB (mencegah payload kosong)

    /**
     * Sanitasi nama berkas guna menangkal eksploitasi Path Traversal (e.g. ../../)
     */
    fun sanitizeFileName(rawName: String): String {
        // Ganti backslash dengan slash untuk standardisasi path separator di semua platform
        val standardized = rawName.replace('\\', '/')
        
        // Ekstrak nama file murni dari path
        val rawFileName = File(standardized).name
        
        // Trim leading and trailing whitespace
        var cleanName = rawFileName.trim()
        
        // Hanya izinkan karakter alfanumerik, titik, strip, dan underscore.
        // Karakter berbahaya diganti dengan underscore.
        cleanName = cleanName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        
        // Cegah nama berkas kosong atau hanya titik-titik berbahaya
        while (cleanName.contains("..")) {
            cleanName = cleanName.replace("..", ".")
        }
        
        if (cleanName.isBlank() || cleanName == "." || cleanName == "..") {
            cleanName = "adzan_kustom.mp3"
        }
        return cleanName
    }

    /**
     * Memvalidasi apakah input stream adalah format MP3 asli dengan memeriksa Magic Bytes.
     */
    fun isValidMp3(inputStream: InputStream): Boolean {
        val header = ByteArray(10)
        // Kita tidak bisa berasumsi mark/reset didukung oleh stream ini,
        // sehingga pembacaan harus dilakukan sekali oleh verifikator file temporer.
        val readBytes = inputStream.read(header)
        if (readBytes < 3) return false

        // 1. Cek Header ID3v2 (biasanya "ID3" atau bytes: 0x49, 0x44, 0x33)
        if (header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()) {
            return true
        }

        // 2. Cek Header Frame MP3 mentah (11-bit syncword: byte pertama 0xFF dan byte kedua memiliki top 3 bit set)
        if (readBytes >= 2) {
            val byte0 = header[0].toInt() and 0xFF
            val byte1 = header[1].toInt() and 0xFF
            if (byte0 == 0xFF && (byte1 and 0xE0) == 0xE0) {
                return true
            }
        }
        return false
    }

    /**
     * Memeriksa keaslian MP3 dari suatu berkas fisik lokal.
     */
    fun isValidMp3File(file: File): Boolean {
        if (!file.exists() || file.length() == 0L) return false
        return try {
            FileInputStream(file).use { fis ->
                isValidMp3(fis)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Memeriksa apakah ukuran file dalam batas wajar sebelum melakukan pembacaan stream.
     */
    fun isFileSizeAcceptable(context: Context, uri: Uri, maxMb: Int = 50): Boolean {
        val maxBytes = maxMb * 1024L * 1024L
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                val length = afd.length
                if (length != -1L) {
                    length in MIN_SIZE_BYTES..maxBytes
                } else {
                    true // Validasi pasca-copy jika length tidak diketahui
                }
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Memvalidasi totalitas input berkas dari Uri untuk memproteksi storage.
     */
    fun validateAudioFile(context: Context, uri: Uri): FileValidationResult {
        // 1. Validasi tipe MIME
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null && mimeType != "audio/mpeg" && mimeType != "audio/mp3" && mimeType != "application/octet-stream") {
            return FileValidationResult.Invalid("MIME type tidak didukung: $mimeType. Gunakan file MP3.")
        }

        // 2. Validasi ukuran sebelum penyalinan
        if (!isFileSizeAcceptable(context, uri, 50)) {
            return FileValidationResult.Invalid("Ukuran file tidak diperbolehkan (maks 50MB, min 10KB).")
        }

        // 3. Salin ke temp file untuk memvalidasi payload & magic bytes
        val tempFile = File(context.cacheDir, "temp_validate_${System.currentTimeMillis()}.tmp")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesCopied = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        bytesCopied += bytesRead
                        if (bytesCopied > MAX_SIZE_BYTES) {
                            return FileValidationResult.Invalid("File melebihi batas ukuran maksimal 50MB.")
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            } ?: return FileValidationResult.Invalid("Gagal membuka aliran file.")

            if (tempFile.length() < MIN_SIZE_BYTES) {
                return FileValidationResult.Invalid("File terlalu kecil (minimal 10KB).")
            }

            // Verifikasi tipe asli (Magic Bytes)
            if (!isValidMp3File(tempFile)) {
                return FileValidationResult.Invalid("File bukan audio MP3 yang valid (Gagal verifikasi Magic Bytes).")
            }

            return FileValidationResult.Valid
        } catch (e: Exception) {
            return FileValidationResult.Invalid("Gagal memproses validasi file: ${e.message}")
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
}
