# ─── Obfuscation Agresif & Optimasi R8 ──────────────────────────
-repackageclasses 'o'
-allowaccessmodification
-optimizationpasses 5
-dontusemixedcaseclassnames

# ─── Hapus Log Debug (Log.d, Log.v, Log.i) di Release Build ────
# Catatan: Log.e() dan Log.w() sengaja tidak dimasukkan agar tetap aktif untuk crash reporting/troubleshooting.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ─── Jaga Kotlin & Coroutines ─────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembers class kotlin.Metadata { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ─── Jaga Moshi (JSON Serialization) ──────────────────────────
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# ─── Jaga Retrofit & OkHttp ───────────────────────────────────
-keepattributes Signature, InnerClasses, AnnotationDefault, EnclosingMethod
-keep interface * { @retrofit2.http.* <methods>; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# ─── Jaga Android Jetpack & Lifecycle / ViewModel ─────────────
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Jaga Room Database ───────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ─── Jaga EncryptedSharedPreferences (Security Crypto) ─────────
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# ─── Jaga Play Services Location & Google Maps / Places ────────
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ─── Jaga Komponen Manifest (Activity, Service, Receiver, Widget) 
-keep public class * extends android.app.Activity { *; }
-keep public class * extends android.app.Service { *; }
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.app.Application { *; }
-keep public class * extends android.appwidget.AppWidgetProvider { *; }

# Jaga spesifik widget provider dan service aplikasi kita agar tidak rusak
-keep class id.ideahousetech.prayertime_qibla.widget.** { *; }
-keep class id.ideahousetech.prayertime_qibla.service.** { *; }

# ─── Jaga Model Data Serialization (Pencegahan Gagal Parse JSON) 
-keep class id.ideahousetech.prayertime_qibla.model.** { *; }

# ─── Sembunyikan Nama File Source & Stack Trace Obfuscation ────
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

