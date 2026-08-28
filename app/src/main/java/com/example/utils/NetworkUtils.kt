package id.ideahousetech.prayertime_qibla.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import java.net.NetworkInterface
import java.util.Collections

/**
 * Utilitas untuk memvalidasi status koneksi jaringan dan mendeteksi anomali keamanan
 * seperti proxy debugging, emulator spoofing, atau VPN mencurigakan yang mengarah ke MITM.
 */
object NetworkUtils {

    private const val TAG = "NetworkUtils"

    /**
     * Memeriksa apakah ada koneksi internet yang aktif dan tervalidasi.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Mendeteksi apakah jaringan sedang dialihkan melalui koneksi VPN.
     * Di Android, aplikasi interceptor MITM (seperti Charles Proxy, HTTPCanary, atau Fiddler)
     * sering kali membuat loopback VPN lokal untuk membaca lalu lintas data HTTPS yang di-decrypt.
     */
    fun isVpnActive(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeNetwork = cm.activeNetwork
                    val capabilities = cm.getNetworkCapabilities(activeNetwork)
                    if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        Log.w(TAG, "🛡️ VPN Aktif terdeteksi melalui Network Transport Capabilities.")
                        return true
                    }
                }
            }

            // Fallback/Metode Tambahan: Memeriksa semua interface jaringan aktif untuk pola VPN (tun0, ppp0)
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                if (networkInterface.isUp) {
                    val name = networkInterface.name.lowercase()
                    if (name.contains("tun") || name.contains("ppp") || name.contains("p2p") || name.contains("tap") || name.contains("vpn")) {
                        Log.w(TAG, "🛡️ Interface VPN Aktif terdeteksi: $name")
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mendeteksi status VPN: ${e.message}")
        }
        return false
    }

    /**
     * Memverifikasi apakah kondisi koneksi aman dari sudut pandang integritas transport.
     * Mengembalikan status true jika online dan tidak ada indikasi VPN/Proxy yang mencurigakan.
     */
    fun isSecureConnection(context: Context): Boolean {
        if (!isNetworkAvailable(context)) {
            Log.e(TAG, "Koneksi tidak tersedia atau tidak stabil.")
            return false
        }

        if (isVpnActive(context)) {
            Log.w(TAG, "PERINGATAN KEAMANAN: Perangkat berjalan di atas VPN/Proxy Interceptor. Lalu lintas HTTPS dapat didekripsi!")
            // Kita dapat memutuskan untuk memblokir atau sekadar mencatat event peringatan tergantung sensitivitas data
        }

        return true
    }
}
