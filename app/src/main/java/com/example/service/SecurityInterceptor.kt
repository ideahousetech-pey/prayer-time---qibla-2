package id.ideahousetech.prayertime_qibla.service

import android.util.Log
import id.ideahousetech.prayertime_qibla.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor untuk memperketat keamanan jaringan di tingkat HTTP.
 * Menegakkan HTTPS wajib, menyuntikkan header keamanan, memvalidasi integritas response,
 * dan mencatat aktivitas keamanan secara rahasia demi melacak anomali.
 */
class SecurityInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. Validasi Wajib HTTPS
        if (!originalRequest.url.isHttps) {
            val errorMsg = "Pelanggaran Keamanan: Koneksi non-HTTPS (Cleartext) dilarang! URL: ${originalRequest.url}"
            logSecurityError(errorMsg)
            throw SecurityException(errorMsg)
        }

        // 2. Tambahkan Request Headers yang sesuai (User-Agent standar)
        val securedRequest = originalRequest.newBuilder()
            .header("User-Agent", "JadwalSholatKiblatSecureAndroidClient/2026")
            .build()

        logSecurityEvent("Melakukan request aman ke: ${securedRequest.url.host}")

        val response: Response
        try {
            response = chain.proceed(securedRequest)
        } catch (e: Exception) {
            when (e) {
                is java.net.SocketTimeoutException -> {
                    if (BuildConfig.DEBUG) {
                        Log.w("SecurityInterceptor", "⏱️ [TIMEOUT] Koneksi ke ${securedRequest.url.host} timeout: ${e.message}")
                    }
                }
                is java.net.ConnectException, is java.net.UnknownHostException -> {
                    if (BuildConfig.DEBUG) {
                        Log.w("SecurityInterceptor", "🔌 [OFFLINE/UNREACHABLE] Host ${securedRequest.url.host} tidak dapat dihubungi: ${e.message}")
                    }
                }
                is javax.net.ssl.SSLException -> {
                    logSecurityError("Koneksi gagal atau terindikasi masalah SSL/TLS: ${e.message}")
                }
                else -> {
                    if (BuildConfig.DEBUG) {
                        Log.w("SecurityInterceptor", "Koneksi terputus ke ${securedRequest.url.host}: ${e.message}")
                    }
                }
            }
            throw e
        }

        // 3. Validasi Integritas Response & Protokol Handshake SSL
        val handshake = response.handshake
        if (handshake == null) {
            val errorMsg = "Krisis Keamanan: Tidak ada Handshake SSL aktif! Koneksi dibatalkan."
            logSecurityError(errorMsg)
            throw IOException(errorMsg)
        } else {
            logSecurityEvent("SSL Handshake Sukses. Cipher Suite: ${handshake.cipherSuite}, Protocol: ${handshake.tlsVersion}")
        }

        // 4. Deteksi status response yang mencurigakan (Anomali MITM)
        // Misal, response sukses tetapi tipe konten tidak sesuai atau kosong secara janggal
        if (response.isSuccessful) {
            val contentType = response.header("Content-Type")
            if (contentType != null && contentType.contains("text/html") && securedRequest.url.host.contains("api.aladhan.com")) {
                val alertMsg = "Anomali MITM Terdeteksi: API Aladhan mengembalikan HTML, bukan JSON! Potensi pembajakan jaringan."
                logSecurityError(alertMsg)
                throw IOException(alertMsg)
            }
        }

        return response
    }

    private fun logSecurityEvent(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("SecurityInterceptor", "🛡️ [SECURE EVENT] $message")
        }
    }

    private fun logSecurityError(message: String) {
        Log.e("SecurityInterceptor", "🚨 [SECURE ERROR] $message")
    }
}
