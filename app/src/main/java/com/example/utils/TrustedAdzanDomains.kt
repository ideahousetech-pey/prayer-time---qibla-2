package id.ideahousetech.prayertime_qibla.utils

import android.net.Uri

/**
 * Whitelist domain terpercaya untuk mengunduh audio Adzan.
 * Melindungi dari SSRF, redirect nakal, dan spoofing server jahat.
 */
object TrustedAdzanDomains {
    val whitelist = setOf(
        "islamcan.com",
        "mp3quran.net",
        "islamicfinder.org",
        "raw.githubusercontent.com"
    )

    /**
     * Memeriksa apakah URL yang diberikan berasal dari domain terpercaya dalam whitelist.
     */
    fun isUrlTrusted(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase() ?: return false
            if (scheme != "http" && scheme != "https") return false
            
            val host = uri.host?.lowercase() ?: return false
            whitelist.any { whitelisted ->
                host == whitelisted || host.endsWith(".$whitelisted")
            }
        } catch (e: Exception) {
            false
        }
    }
}
