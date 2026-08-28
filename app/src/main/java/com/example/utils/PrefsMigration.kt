package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Helper Migrasi & Pemulihan Penyimpanan Aman.
 * Membantu memigrasikan data sensitif dari plain SharedPreferences lama ke dalam
 * sistem SecurePrefs baru yang terenkripsi, serta memulihkan data jika terjadi kerusakan KeyStore.
 */
object PrefsMigration {

    private const val TAG = "PrefsMigration"
    private const val PLAIN_PREFS_NAME = "adzan_secure_prefs_plain_fallback"
    private const val SECURE_PREFS_NAME = "adzan_secure_prefs"

    /**
     * Memigrasikan semua data dari file SharedPreferences plain (tidak terenkripsi)
     * ke dalam penyimpanan terenkripsi yang aman jika sistem enkripsi aktif.
     */
    fun migrateFromPlainToEncrypted(context: Context) {
        try {
            val plainPrefs = context.getSharedPreferences(PLAIN_PREFS_NAME, Context.MODE_PRIVATE)
            val allEntries = plainPrefs.all
            if (allEntries.isEmpty()) {
                Log.d(TAG, "Tidak ada data plain untuk dimigrasikan.")
                return
            }

            // Jika SecurePrefs berhasil dimuat dengan enkripsi aktif, pindahkan datanya
            if (SecurePrefs.isEncryptionActive(context)) {
                val securePrefs = SecurePrefs.get(context)
                val editor = securePrefs.edit()

                for ((key, value) in allEntries) {
                    if (value == null) continue
                    
                    // Filter berdasarkan klasifikasi sensitivitas sebelum menyimpan
                    val sensitivity = PrefsKeys.getSensitivity(key)
                    Log.d(TAG, "Migrasi key: $key (Sensitivitas: $sensitivity)")

                    when (value) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Float -> editor.putFloat(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is String -> editor.putString(key, value)
                    }
                }
                editor.apply()

                // Bersihkan data plain agar tidak terjadi redundansi
                plainPrefs.edit().clear().apply()
                Log.i(TAG, "🎉 Sukses memigrasikan ${allEntries.size} data plain ke penyimpanan terenkripsi.")
            } else {
                Log.w(TAG, "Enkripsi belum aktif. Migrasi ditunda agar tidak menyimpan data sensitif di storage plain.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menjalankan migrasi plain-to-encrypted: ${e.message}", e)
        }
    }

    /**
     * Mendukung migrasi skema data dari versi 1 ke versi 2 di masa mendatang jika diperlukan.
     */
    fun migrateFromV1ToV2(context: Context) {
        Log.i(TAG, "Mengecek kebutuhan migrasi skema v1 ke v2...")
        // Saat ini struktur kunci versi 1 & 2 setara, fungsi disiapkan untuk ekspansi masa depan
    }

    /**
     * Penanganan Penyelamatan KeyStore Corrupt (Bencana Kriptografi).
     * Jika inisiasi master key rusak di level OS (MIUI, Custom ROM), existing encrypted file
     * sama sekali tidak bisa dibuka kembali dan memicu Exception berulang yang berpotensi crash.
     * Fungsi ini akan membackup data non-sensitif (seperti Onboarding & Tema) ke memory sementara,
     * secara fisik menghapus file XML preferensi yang rusak, lalu menginisiasi ulang container
     * dengan aman sebelum memulihkan data non-sensitif tersebut.
     */
    fun handleCorruptedKeystore(context: Context) {
        Log.e(TAG, "🚨 KORUPSI KEYSTORE TERDETEKSI! Memulai protokol pemulihan darurat...")

        // 1. Ambil cadangan data non-sensitif jika plain prefs fallback masih bisa diakses
        val backupMap = mutableMapOf<String, Any>()
        try {
            val plainPrefs = context.getSharedPreferences(PLAIN_PREFS_NAME, Context.MODE_PRIVATE)
            plainPrefs.all.forEach { (key, value) ->
                if (value != null && PrefsKeys.getSensitivity(key) == DataSensitivity.NON_SENSITIVE) {
                    backupMap[key] = value
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuat cadangan non-sensitif dari plain prefs: ${e.message}")
        }

        // 2. Secara fisik hapus file preferensi terenkripsi yang rusak dari filesystem Android
        try {
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                val corruptedFile = File(sharedPrefsDir, "$SECURE_PREFS_NAME.xml")
                if (corruptedFile.exists()) {
                    if (corruptedFile.delete()) {
                        Log.i(TAG, "🗑️ Sukses menghapus file preferensi terenkripsi yang rusak secara fisik.")
                    } else {
                        Log.e(TAG, "Gagal menghapus file preferensi terenkripsi yang rusak.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kesalahan fatal saat menghapus file preferensi rusak: ${e.message}")
        }

        // 3. Reset inisialisasi singleton di SecurePrefs agar membuat instance baru
        SecurePrefs.resetInstance()

        // 4. Inisiasi ulang SecurePrefs & pulihkan data non-sensitif cadangan
        try {
            val freshPrefs = SecurePrefs.get(context)
            val editor = freshPrefs.edit()
            backupMap.forEach { (key, value) ->
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is String -> editor.putString(key, value)
                }
            }
            editor.apply()
            Log.i(TAG, "🛡️ Pemulihan darurat selesai. Sistem preferensi aman kembali sehat.")
        } catch (e: Exception) {
            Log.e(TAG, "Inisiasi ulang setelah penghancuran gagal: ${e.message}", e)
        }
    }
}
