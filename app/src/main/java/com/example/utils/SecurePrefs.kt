package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Utilitas helper untuk preferences berenkripsi (SecurePrefs) dengan Double-Checked Locking Thread-Safe.
 * Memisahkan penyimpanan data berdasarkan klasifikasi sensitivitasnya (CRITICAL, SENSITIVE, NON_SENSITIVE).
 */
object SecurePrefs {
    private const val TAG = "SecurePrefs"
    private const val PREFS_NAME = "adzan_secure_prefs"
    private const val PLAIN_PREFS_NAME = "adzan_secure_prefs_plain_fallback"

    @Volatile
    private var instance: SharedPreferences? = null

    @Volatile
    private var encryptionActive: Boolean = false

    // In-memory cache untuk data CRITICAL (seperti koordinat GPS) jika enkripsi mati.
    // Ini menjamin fungsionalitas berjalan normal selama runtime tanpa menyimpan data sensitif plaintext ke disk.
    private val criticalInMemoryCache = ConcurrentHashMap<String, Any>()

    /**
     * Mengembalikan status apakah enkripsi AES256 berbasis Android KeyStore aktif atau sedang dalam fallback.
     */
    fun isEncryptionActive(context: Context): Boolean {
        if (instance == null) {
            get(context) // Triger inisialisasi jika belum
        }
        return encryptionActive
    }

    /**
     * Mereset instance Singleton. Digunakan saat pemulihan korupsi KeyStore.
     */
    fun resetInstance() {
        synchronized(this) {
            instance = null
            encryptionActive = false
            criticalInMemoryCache.clear()
            Log.w(TAG, "Instance SecurePrefs di-reset.")
        }
    }

    /**
     * Mengambil instance SharedPreferences terbungkus kustom secara thread-safe menggunakan double-checked locking.
     */
    fun get(context: Context): SharedPreferences {
        return instance ?: synchronized(this) {
            instance ?: wrapSharedPreferences(context, initPrefs(context)).also { instance = it }
        }
    }

    private fun initPrefs(context: Context): SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val eps = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            encryptionActive = true
            Log.i(TAG, "🛡️ EncryptedSharedPreferences sukses diinisiasi.")
            return eps
        } catch (e: Exception) {
            Log.e(TAG, "🚨 Gagal inisiasi EncryptedSharedPreferences: ${e.message}. Memulai penanganan korupsi...", e)
            
            // Bersihkan file XML lama yang rusak agar tidak crash berulang
            deleteCorruptedPrefsFile(context)

            try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val eps = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                encryptionActive = true
                Log.i(TAG, "🎉 Pemulihan berhasil. EncryptedSharedPreferences siap digunakan.")
                return eps
            } catch (re: Exception) {
                Log.e(TAG, "🚨 Pemulihan gagal total. Mengaktifkan Plain Fallback untuk menjaga stabilitas sistem.", re)
                encryptionActive = false
                return context.getSharedPreferences(PLAIN_PREFS_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    private fun deleteCorruptedPrefsFile(context: Context) {
        try {
            val sharedPrefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                val corruptedFile = File(sharedPrefsDir, "$PREFS_NAME.xml")
                if (corruptedFile.exists()) {
                    corruptedFile.delete()
                    Log.w(TAG, "File XML preferensi terenkripsi yang rusak berhasil dibuang secara fisik.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuang file preferensi terkorupsi: ${e.message}")
        }
    }

    private fun wrapSharedPreferences(context: Context, delegate: SharedPreferences): SharedPreferences {
        return SecureSharedPreferencesWrapper(delegate, encryptionActive)
    }

    /**
     * Wrapper kustom SharedPreferences untuk memisahkan perlakuan data CRITICAL vs SENSITIVE vs NON_SENSITIVE.
     */
    private class SecureSharedPreferencesWrapper(
        private val delegate: SharedPreferences,
        private val isEncrypted: Boolean
    ) : SharedPreferences {

        override fun getAll(): Map<String, *> {
            if (isEncrypted) return delegate.all
            
            // Jika enkripsi mati, sembunyikan data CRITICAL dari disk, gunakan data dari in-memory cache saja
            val filtered = delegate.all.filterKeys { 
                PrefsKeys.getSensitivity(it) != DataSensitivity.CRITICAL 
            }.toMutableMap()
            filtered.putAll(criticalInMemoryCache)
            return filtered
        }

        override fun getString(key: String?, defValue: String?): String? {
            if (key == null) return defValue
            if (isEncrypted) return delegate.getString(key, defValue)

            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                val cached = criticalInMemoryCache[key] as? String
                if (cached != null) {
                    Log.d(TAG, "Membaca $key dari in-memory cache (enkripsi tidak aktif)")
                    cached
                } else {
                    Log.w(TAG, "Akses Terblokir: Menolak membaca $key dari plain text storage.")
                    defValue
                }
            } else {
                delegate.getString(key, defValue)
            }
        }

        override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? {
            if (key == null) return defValues
            if (isEncrypted) return delegate.getStringSet(key, defValues)

            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                @Suppress("UNCHECKED_CAST")
                val cached = criticalInMemoryCache[key] as? Set<String>
                cached ?: defValues
            } else {
                delegate.getStringSet(key, defValues)
            }
        }

        override fun getInt(key: String?, defValue: Int): Int {
            if (key == null) return defValue
            if (isEncrypted) return delegate.getInt(key, defValue)

            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                (criticalInMemoryCache[key] as? Int) ?: defValue
            } else {
                delegate.getInt(key, defValue)
            }
        }

        override fun getLong(key: String?, defValue: Long): Long {
            if (key == null) return defValue
            if (isEncrypted) return delegate.getLong(key, defValue)

            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                (criticalInMemoryCache[key] as? Long) ?: defValue
            } else {
                delegate.getLong(key, defValue)
            }
        }

        override fun getFloat(key: String?, defValue: Float): Float {
            if (key == null) return defValue
            if (isEncrypted) return delegate.getFloat(key, defValue)

            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                (criticalInMemoryCache[key] as? Float) ?: defValue
            } else {
                delegate.getFloat(key, defValue)
            }
        }

        override fun getBoolean(key: String?, defValue: Boolean): Boolean {
            if (key == null) return defValue
            if (isEncrypted) return delegate.getBoolean(key, defValue)

            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                (criticalInMemoryCache[key] as? Boolean) ?: defValue
            } else {
                delegate.getBoolean(key, defValue)
            }
        }

        override fun contains(key: String?): Boolean {
            if (key == null) return false
            if (isEncrypted) return delegate.contains(key)
            return if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                criticalInMemoryCache.containsKey(key)
            } else {
                delegate.contains(key)
            }
        }

        override fun edit(): SharedPreferences.Editor {
            return SecureEditorWrapper(delegate.edit(), isEncrypted)
        }

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            delegate.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            delegate.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    /**
     * Editor kustom SharedPreferences untuk melacak penyimpanan data sensitif.
     */
    private class SecureEditorWrapper(
        private val delegateEditor: SharedPreferences.Editor,
        private val isEncrypted: Boolean
    ) : SharedPreferences.Editor {

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.putString(key, value)
            } else {
                if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                    if (value != null) {
                        criticalInMemoryCache[key] = value
                        Log.i(TAG, "🛡️ Keamanan: Menyimpan $key ke in-memory cache karena enkripsi mati.")
                    } else {
                        criticalInMemoryCache.remove(key)
                    }
                } else {
                    delegateEditor.putString(key, value)
                }
            }
            return this
        }

        override fun putStringSet(key: String?, values: Set<String>?): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.putStringSet(key, values)
            } else {
                if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                    if (values != null) {
                        criticalInMemoryCache[key] = values
                    } else {
                        criticalInMemoryCache.remove(key)
                    }
                } else {
                    delegateEditor.putStringSet(key, values)
                }
            }
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.putInt(key, value)
            } else {
                if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                    criticalInMemoryCache[key] = value
                } else {
                    delegateEditor.putInt(key, value)
                }
            }
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.putLong(key, value)
            } else {
                if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                    criticalInMemoryCache[key] = value
                } else {
                    delegateEditor.putLong(key, value)
                }
            }
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.putFloat(key, value)
            } else {
                if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                    criticalInMemoryCache[key] = value
                } else {
                    delegateEditor.putFloat(key, value)
                }
            }
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.putBoolean(key, value)
            } else {
                if (PrefsKeys.getSensitivity(key) == DataSensitivity.CRITICAL) {
                    criticalInMemoryCache[key] = value
                } else {
                    delegateEditor.putBoolean(key, value)
                }
            }
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            if (key == null) return this
            if (isEncrypted) {
                delegateEditor.remove(key)
            } else {
                criticalInMemoryCache.remove(key)
                delegateEditor.remove(key)
            }
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            criticalInMemoryCache.clear()
            delegateEditor.clear()
            return this
        }

        override fun commit(): Boolean {
            return delegateEditor.commit()
        }

        override fun apply() {
            delegateEditor.apply()
        }
    }
}
