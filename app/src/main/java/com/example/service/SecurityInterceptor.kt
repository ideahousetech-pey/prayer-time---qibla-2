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

        // 2. Tambahkan Security Headers ke Request
        val securedRequest = originalRequest.newBuilder()
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("X-XSS-Protection", "1; mode=block")
            .header("Content-Security-Policy", "default-src 'none'")
            .header("Strict-Transport-Security", "max-age=31536000; includeSubdomains")
            // Menyertakan User-Agent yang aman dan standar untuk otentikasi
            .header("User-Agent", "JadwalSholatKiblatSecureAndroidClient/2026")
            .build()

        logSecurityEvent("Melakukan request aman ke: ${securedRequest.url.host}")

        val response: Response
        try {
            response = chain.proceed(securedRequest)
        } catch (e: Exception) {
            logSecurityError("Koneksi gagal atau terindikasi SSL Handshake Tampering: ${e.message}")
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
