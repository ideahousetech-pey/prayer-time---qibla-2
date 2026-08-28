# Panduan Pengujian Keamanan Manual (Manual Security Testing Checklist)

Dokumen ini berisi prosedur langkah-demi-langkah bagi tim QA / Pengembang untuk melakukan pengujian penetrasi (pen-testing) dan verifikasi keamanan manual pada aplikasi **Jadwal Sholat & Kiblat**.

---

## 1. KEAMANAN JARINGAN (NETWORK SECURITY)
Membuktikan ketahanan aplikasi terhadap serangan Man-in-the-Middle (MITM) dan pencegahan kebocoran data saat transit.

### □ Skenario 1.1: Intersepsi Lalu Lintas HTTPS (Charles / MITMProxy)
* **Tujuan**: Memastikan data tidak dapat dibaca oleh pihak ketiga saat transit.
* **Langkah Pengujian**:
  1. Hubungkan perangkat pengujian (Android device/emulator) ke WiFi yang sama dengan komputer penyerang.
  2. Konfigurasikan proxy manual pada pengaturan WiFi perangkat Android agar mengarah ke alamat IP komputer lokal (port `8888` untuk Charles Proxy).
  3. Buka aplikasi dan lakukan pencarian lokasi atau sinkronisasi jadwal sholat astronomis.
* **Hasil yang Diharapkan**:
  * Seluruh koneksi ke API `api.aladhan.com` wajib **gagal** (Network Error / SSL Error) karena proxy Charles tidak memiliki sertifikat SSL dari chain tepercaya ZeroSSL / Sectigo Root.
  * Aplikasi tidak boleh mentoleransi lalu lintas cleartext (HTTP biasa) maupun sertifikat proxy self-signed.

### □ Skenario 1.2: Pengujian Bypass SSL Pinning (Frida / TrustMeAlready)
* **Tujuan**: Mengukur ketahanan aplikasi jika penyerang melakukan instrumentasi runtime dinamis untuk menonaktifkan pemeriksaan pin sertifikat SSL.
* **Langkah Pengujian**:
  1. Siapkan perangkat Android yang telah di-root yang terpasang `frida-server`.
  2. Jalankan skrip bypass SSL pinning universal melalui terminal PC:
     ```bash
     frida -U -f id.ideahousetech.prayertime_qibla --no-pause -l bypass-ssl-pinning.js
     ```
  3. Monitor logcat perangkat selama runtime.
* **Hasil yang Diharapkan**:
  * Jika bypass berhasil, Charles Proxy akan mulai menampilkan muatan (payload) JSON API.
  * *Mitigasi di masa depan*: Deteksi instrumen Frida secara runtime di tingkat native (C/C++) untuk menghentikan proses aplikasi seketika jika bypass diidentifikasi.

---

## 2. KEAMANAN DATA LOKAL & BACKUP (DATA SECURITY)
Menguji apakah data sensitif (seperti koordinat GPS lokasi terakhir, bookmark Quran, dan konfigurasi) bocor saat pencadangan otomatis (cloud backup) atau transfer antar perangkat.

### □ Skenario 2.1: Audit Backup Aplikasi via ADB
* **Tujuan**: Memastikan data terenkripsi dan cache sensitif tidak diekspor ke cadangan eksternal.
* **Langkah Pengujian**:
  1. Hubungkan perangkat Android ke PC dengan USB Debugging aktif.
  2. Ekstrak data backup aplikasi lokal menggunakan perintah:
     ```bash
     adb backup -f app_security_backup.ab -noapk id.ideahousetech.prayertime_qibla
     ```
  3. Ekstrak berkas cadangan `.ab` menggunakan alat dekripsi `tar-extractor` atau `android-backup-extractor` (abe.jar):
     ```bash
     java -jar abe.jar unpack app_security_backup.ab decrypted_backup.tar
     tar -xf decrypted_backup.tar
     ```
  4. Periksa struktur folder hasil ekstraksi.
* **Hasil yang Diharapkan**:
  * Direktori `sharedpref` **tidak mengandung** berkas `adzan_secure_prefs.xml` (EncryptedSharedPreferences) maupun `user_location_cache.xml` (Koordinat GPS sensitif).
  * Direktori `database` **tidak mengandung** berkas basis data `prayer_qibla_db` (karena telah dieksklusi secara eksplisit di `backup_rules.xml` dan `data_extraction_rules.xml`).
  * Hanya berkas umum non-sensitif (seperti `quran_bookmarks.xml`) yang boleh muncul dalam cadangan.

### □ Skenario 2.2: Verifikasi Hak Akses File Sistem (Sandbox Permissions)
* **Tujuan**: Memastikan aplikasi lain dalam sistem operasi Android tidak dapat membaca berkas lokal aplikasi secara langsung.
* **Langkah Pengujian**:
  1. Hubungkan perangkat pengujian (Rooted Device atau Emulator) via ADB.
  2. Buka shell ADB dan masuk sebagai superuser:
     ```bash
     adb shell
     su
     ```
  3. Navigasikan ke direktori penyimpanan internal privat aplikasi:
     ```bash
     cd /data/data/id.ideahousetech.prayertime_qibla/
     ls -la
     ```
* **Hasil yang Diharapkan**:
  * Hak akses untuk folder `shared_prefs`, `databases`, dan `files` harus bernilai `drwx------` (milik UID aplikasi saja).
  * Tidak ada izin baca global/publik seperti `drwxrwxrwx` yang terpasang pada berkas internal.

---

## 3. KEAMANAN KOMPONEN (COMPONENT PROTECTION)
Memverifikasi bahwa komponen internal aplikasi (Activity, Service, BroadcastReceiver, ContentProvider) aman dari serangan eksploitasi eksternal atau pembajakan intent (intent hijacking).

### □ Skenario 3.1: Eksploitasi Widget via Intent Eksternal
* **Tujuan**: Menghindari pembaruan widget palsu atau manipulasi tampilan dari malware di perangkat.
* **Langkah Pengujian**:
  1. Gunakan aplikasi terminal ADB di PC untuk mengirim Intent modifikasi widget dari luar aplikasi:
     ```bash
     adb shell am broadcast -a id.ideahousetech.prayertime_qibla.action.UPDATE_WIDGET --es "test_payload" "malicious"
     ```
* **Hasil yang Diharapkan**:
  * Pengiriman broadcast intent eksternal wajib **ditolak** oleh sistem operasi Android atau diabaikan karena receiver di-protect oleh custom signature permission, sehingga memunculkan log error keamanan di Logcat: `Permission Denial: broadcasting Intent requires custom permission`.

### □ Skenario 3.2: Membuka Aktivitas yang Diekspor (Exported Activity Audit)
* **Tujuan**: Memastikan tidak ada halaman pengaturan atau layar fungsionalitas internal yang dapat di-bypass oleh aplikasi jahat.
* **Langkah Pengujian**:
  1. Jalankan perintah ADB untuk memulai aktivitas secara paksa dari luar:
     ```bash
     adb shell am start -n id.ideahousetech.prayertime_qibla/.ui.DailyScheduleScreen
     ```
* **Hasil yang Diharapkan**:
  * Aktivitas harus menghasilkan exception `SecurityException: Permission Denial` karena halaman tersebut tidak di-ekspor secara publik dalam `AndroidManifest.xml` (hanya `MainActivity` saja yang bertindak sebagai gerbang masuk berstatus `android:exported="true"`).

---

## 4. KEAMANAN KODE & BINARI (CODE SECURITY)
Memeriksa integritas berkas biner (APK) terhadap rekayasa balik (reverse engineering) dan memverifikasi kerahasiaan data statis.

### □ Skenario 4.1: Audit Obfuskasi Kode dengan JADX
* **Tujuan**: Memastikan logika pemrograman dan struktur algoritma tidak dapat dipahami dengan mudah jika didekompilasi.
* **Langkah Pengujian**:
  1. Dekompilasi rilis build APK menggunakan JADX-GUI.
  2. Lakukan pencarian nama kelas internal seperti `PrayerViewModel`, `SecurityReporter`, atau `AdzanForegroundService`.
* **Hasil yang Diharapkan**:
  * Sebagian besar nama kelas dan metode pembantu yang tidak dikecualikan dalam aturan ProGuard harus terobfuskasi menjadi karakter tunggal acak (misal: `a`, `b.c()`).
  * Kode sumber harus sangat sulit dibaca secara logis oleh mata manusia.

### □ Skenario 4.2: Pemindaian API Key dan Kredensial Keras (Hardcoded Secrets)
* **Tujuan**: Memastikan tidak ada rahasia developer (seperti kunci token/sandi) yang tersimpan secara statis di dalam kode.
* **Langkah Pengujian**:
  1. Gunakan fitur pencarian teks global pada JADX decompiler dengan kata kunci: `api_key`, `secret`, `password`, `bearer`, `token`.
* **Hasil yang Diharapkan**:
  * Hasil pencarian tidak boleh memunculkan nilai string sensitif statis berisikan token API riil. Semua konfigurasi harus diambil dari server secara dinamis atau disembunyikan menggunakan skema enkripsi/perhitungan dinamis.
