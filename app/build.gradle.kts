import java.net.URL
import java.io.File

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "id.ideahousetech.prayertime_qibla"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "id.ideahousetech.prayertime_qibla"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      buildConfigField("boolean", "FORCE_SHOW_RAMADHAN_FEATURE", "false")
      isCrunchPngs = false
      isMinifyEnabled = true          // ← WAJIB aktifkan
      isShrinkResources = true        // ← tambahkan ini
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      buildConfigField("boolean", "FORCE_SHOW_RAMADHAN_FEATURE", "true")
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth.ktx)
  implementation(libs.firebase.firestore.ktx)
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  // implementation(libs.androidx.compose.ui.text.googlefonts)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
  implementation(libs.androidx.security.crypto)
}

abstract class DownloadAssetsTask : DefaultTask() {
    @get:OutputDirectory
    abstract val assetsDir: DirectoryProperty

    @get:OutputDirectory
    abstract val fontDir: DirectoryProperty

    @TaskAction
    fun run() {
        val targetAssets = assetsDir.get().asFile
        targetAssets.mkdirs()

        // ── Audio Adzan ────────────────────────────────────────────────────
        // Beberapa URL fallback — dicoba berurutan jika yang pertama gagal
        val adzanUrls = listOf(
            "https://www.islamcan.com/audio/adhans/adhan1.mp3",
            "https://archive.org/download/adhan_202206/adhan.mp3",
            "https://islamicsounds.net/adhan/mecca-adhan.mp3"
        )
        val adzanFajrUrls = listOf(
            "https://www.islamcan.com/audio/adhans/adhan10.mp3",
            "https://archive.org/download/AzanMadinah_201712/azan_madinah.mp3",
            "https://archive.org/download/adhan_202206/adhan.mp3"
        )

        downloadWithFallback(
            urlList     = adzanUrls,
            dest        = File(targetAssets, "adzan.mp3"),
            minBytes    = 100_000L,
            description = "adzan.mp3 (Adzan Mekah)"
        )
        downloadWithFallback(
            urlList     = adzanFajrUrls,
            dest        = File(targetAssets, "adzan_fajr.mp3"),
            minBytes    = 100_000L,
            description = "adzan_fajr.mp3 (Adzan Madinah Fajr)"
        )

        // ── Font ───────────────────────────────────────────────────────────
        val targetFonts = fontDir.get().asFile
        targetFonts.mkdirs()

        val fonts = mapOf(
            "cinzel_regular.ttf"  to listOf(
                "https://fonts.gstatic.com/s/cinzel/v26/8vIU7ww63mVu7gtR-kwKxNvkNOjw-tbnTYo.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/cinzel/static/Cinzel-Regular.ttf"
            ),
            "cinzel_bold.ttf"     to listOf(
                "https://fonts.gstatic.com/s/cinzel/v26/8vIU7ww63mVu7gtR-kwKxNvkNOjw-jHgTYo.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/cinzel/static/Cinzel-Bold.ttf"
            ),
            "nunito_light.ttf"    to listOf(
                "https://fonts.gstatic.com/s/nunito/v32/XRXI3I6Li01BKofiOc5wtlZ2di8HDOUhRTM.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/nunito/static/Nunito-Light.ttf"
            ),
            "nunito_regular.ttf"  to listOf(
                "https://fonts.gstatic.com/s/nunito/v32/XRXI3I6Li01BKofiOc5wtlZ2di8HDLshRTM.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/nunito/static/Nunito-Regular.ttf"
            ),
            "nunito_semibold.ttf" to listOf(
                "https://fonts.gstatic.com/s/nunito/v32/XRXI3I6Li01BKofiOc5wtlZ2di8HDGUmRTM.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/nunito/static/Nunito-SemiBold.ttf"
            ),
            "nunito_bold.ttf"     to listOf(
                "https://fonts.gstatic.com/s/nunito/v32/XRXI3I6Li01BKofiOc5wtlZ2di8HDFwmRTM.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/nunito/static/Nunito-Bold.ttf"
            ),
            "amiri_regular.ttf"   to listOf(
                "https://fonts.gstatic.com/s/amiri/v26/J7aYCQ97_1_7-7yHLM0s8A.ttf",
                "https://raw.githubusercontent.com/google/fonts/main/ofl/amiri/Amiri-Regular.ttf"
            )
        )
        for ((name, urls) in fonts) {
            downloadWithFallback(
                urlList     = urls,
                dest        = File(targetFonts, name),
                minBytes    = 5_000L,
                description = "font $name"
            )
        }
    }

    /**
     * Mengunduh file dengan beberapa URL fallback.
     * Jika semua URL gagal → throw GradleException (build GAGAL dengan pesan jelas).
     * Jika file sudah valid → skip.
     */
    private fun downloadWithFallback(
        urlList     : List<String>,
        dest        : File,
        minBytes    : Long,
        description : String
    ) {
        // Skip jika sudah valid
        if (dest.exists() && dest.length() >= minBytes) {
            println("✓ $description sudah ada (${dest.length()} bytes), skip.")
            return
        }

        // Hapus file lama yang tidak valid
        if (dest.exists()) dest.delete()

        val isAudio  = description.contains(".mp3")
        val timeout  = if (isAudio) 45_000 else 20_000  // audio butuh timeout lebih lama

        var lastError: String = "Tidak ada error"

        for ((idx, url) in urlList.withIndex()) {
            println("⏳ Mencoba unduh $description (percobaan ${idx + 1}/${urlList.size}): $url")
            try {
                val conn = URL(url).openConnection()
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
                conn.connectTimeout = timeout
                conn.readTimeout    = timeout

                conn.getInputStream().use { input ->
                    dest.outputStream().use { out -> input.copyTo(out) }
                }
                if (dest.exists() && dest.length() >= minBytes) {
                    println("✓ $description berhasil (${dest.length()} bytes).")
                    return
                } else {
                    lastError = "Ukuran terlalu kecil: ${dest.length()} bytes (min $minBytes)"
                    dest.delete()
                    println("✗ $description ukuran tidak valid, coba URL berikutnya...")
                }
            } catch (e: Exception) {
                lastError = e.message ?: "Unknown error"
                println("✗ Gagal dari $url: $lastError")
                if (dest.exists()) dest.delete()
                if (idx < urlList.size - 1) Thread.sleep(1500)
            }
        }

        // Semua URL gagal
        if (!isAudio) {
            throw org.gradle.api.GradleException(
                """
                ════════════════════════════════════════════════════════════
                ✗ BUILD GAGAL: Tidak dapat mengunduh $description
                Error terakhir: $lastError
                
                Solusi:
                1. Periksa koneksi internet, lalu jalankan: ./gradlew downloadAssets
                2. Atau unduh manual dan simpan ke:
                   ${dest.absolutePath}
                ════════════════════════════════════════════════════════════
                """.trimIndent()
            )
        } else {
            println("⚠️ PERINGATAN: Gagal mengunduh file adzan $description karena ($lastError). Pemutar adzan akan fallback menggunakan ringtone alarm sistem secara otomatis.")
        }
    }
}

val downloadAssets = tasks.register<DownloadAssetsTask>("downloadAssets") {
    assetsDir.set(layout.projectDirectory.dir("src/main/assets"))
    fontDir.set(layout.projectDirectory.dir("src/main/res/font"))
}

tasks.register("checkFonts") {
    doLast {
        val fDir = file("src/main/res/font")
        if (fDir.exists() && fDir.isDirectory) {
            fDir.listFiles()?.forEach { file ->
                println("FONT_DIAGNOSTIC: Name: ${file.name}, Size: ${file.length()} bytes")
                if (file.length() > 0) {
                    val bytes = file.readBytes().take(100)
                    val sampleString = bytes.map { it.toInt().toChar() }.joinToString("")
                    val isHtml = sampleString.contains("DOCTYPE") || sampleString.contains("<html") || sampleString.contains("<HTML")
                    println("  Is HTML: $isHtml, Sample: ${sampleString.replace('\n', ' ').replace('\r', ' ').take(60)}")
                }
            }
        } else {
            println("FONT_DIAGNOSTIC: src/main/res/font directory does not exist!")
        }
    }
}

tasks.named("preBuild") {
    dependsOn(downloadAssets)
}





