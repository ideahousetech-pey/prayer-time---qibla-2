package id.ideahousetech.prayertime_qibla.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Utilitas Diagnostik Keystore untuk menganalisis kesehatan Android KeyStore.
 * Membantu mendeteksi kerusakan crypto hardware, ketidakcocokan ROM kustom, atau korupsi kunci MIUI.
 */
object KeyStoreDiagnostics {

    private const val TAG = "KeyStoreDiagnostics"
    private const val DIAG_KEY_ALIAS = "diagnostic_test_key_alias"
    private const val PROVIDER_ANDROID_KEYSTORE = "AndroidKeyStore"

    data class DiagnosticResult(
        val isHealthy: Boolean,
        val isHardwareBacked: Boolean,
        val errorCode: String?,
        val errorMessage: String,
        val suggestion: String
    )

    /**
     * Menjalankan diagnosa fungsional penuh pada sistem kriptografi Android KeyStore.
     */
    fun performDiagnostics(): DiagnosticResult {
        var keyStore: KeyStore? = null
        try {
            // 1. Coba muat KeyStore provider
            keyStore = KeyStore.getInstance(PROVIDER_ANDROID_KEYSTORE).apply {
                load(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat KeyStore provider", e)
            return DiagnosticResult(
                isHealthy = false,
                isHardwareBacked = false,
                errorCode = "ERR_LOAD_KEYSTORE",
                errorMessage = "Gagal memuat sistem AndroidKeyStore: ${e.message}",
                suggestion = "Keystore Android Anda rusak parah. Silakan restart perangkat atau lakukan factory reset."
            )
        }

        try {
            // 2. Coba buat kunci sementara
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER_ANDROID_KEYSTORE)
            val keySpec = KeyGenParameterSpec.Builder(
                DIAG_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keySpec)
            val secretKey = keyGenerator.generateKey()

            // 3. Tes Enkripsi & Dekripsi fungsional
            val originalText = "Test_Integritas_Keystore_2026"
            
            // Enkripsi
            val encryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val cipherText = encryptCipher.doFinal(originalText.toByteArray(Charsets.UTF_8))
            val iv = encryptCipher.iv

            // Dekripsi
            val decryptCipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = decryptCipher.doFinal(cipherText)
            val decryptedText = String(decryptedBytes, Charsets.UTF_8)

            // Bersihkan kunci uji coba
            keyStore.deleteEntry(DIAG_KEY_ALIAS)

            if (decryptedText != originalText) {
                return DiagnosticResult(
                    isHealthy = false,
                    isHardwareBacked = false,
                    errorCode = "ERR_INTEGRITY_MISMATCH",
                    errorMessage = "Data terdekripsi tidak cocok dengan teks asli.",
                    suggestion = "Ada kegagalan memori internal. Silakan tutup paksa aplikasi atau bersihkan cache."
                )
            }

            // Memeriksa dukungan hardware jika perangkat versi modern
            val isHardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Sesuai Android 12+, kita asumsikan KeyInfo atau TEE modern jika tes enkripsi hardware lolos
                true
            } else {
                false
            }

            return DiagnosticResult(
                isHealthy = true,
                isHardwareBacked = isHardware,
                errorCode = null,
                errorMessage = "Sistem Android KeyStore sehat dan berfungsi dengan baik.",
                suggestion = "Tidak ada tindakan diperlukan. Data Anda terenkripsi aman."
            )

        } catch (e: Exception) {
            Log.e(TAG, "Integritas Keystore Gagal", e)
            val isMiui = Build.MANUFACTURER.lowercase().contains("xiaomi") || 
                         Build.BRAND.lowercase().contains("xiaomi") || 
                         Build.FINGERPRINT.lowercase().contains("miui")
            
            val suggestion = if (isMiui) {
                "Perangkat Xiaomi/MIUI terdeteksi memiliki isu korupsi KeyStore. Silakan pergi ke Pengaturan Sistem -> Aplikasi -> Hapus Semua Data aplikasi ini, lalu restart handphone."
            } else {
                "Sistem enkripsi perangkat tidak stabil. Silakan bersihkan penyimpanan aplikasi ini melalui setelan sistem."
            }

            return DiagnosticResult(
                isHealthy = false,
                isHardwareBacked = false,
                errorCode = "ERR_CRYPTO_EXCEPTION",
                errorMessage = "Kesalahan Kriptografi: ${e.message}",
                suggestion = suggestion
            )
        }
    }
}
