# Panduan Alat Keamanan (Security Tools Usage Guide)

Panduan praktis untuk melakukan analisis statis, analisis dinamis, dan pengujian penetrasi (pen-testing) pada aplikasi Android menggunakan perkakas keamanan standar industri.

---

## A. JADX (Decompiler & Static Analyzer)
Alat untuk mendekompilasi file APK kembali menjadi kode sumber Java/Kotlin yang dapat dibaca manusia.

* **Penggunaan Utama**:
  * Mengaudit obfuskasi kode (apakah nama variabel, fungsi, dan kelas sudah disamarkan).
  * Mencari kebocoran rahasia statis (API key, endpoint tersembunyi, kredensial ter-hardcode).
* **Langkah Menggunakan**:
  1. Unduh rilis terbaru JADX dari [GitHub JADX](https://github.com/skylot/jadx/releases).
  2. Jalankan antarmuka grafis:
     * **Windows**: Klik ganda `jadx-gui.bat` di folder `bin`.
     * **macOS/Linux**: Jalankan `./bin/jadx-gui` dari terminal.
  3. Seret dan letakkan (drag and drop) berkas `.apk` Anda ke jendela JADX.
  4. Gunakan kombinasi tombol pencarian global `Ctrl + Shift + F` (atau `Cmd + Shift + F` pada Mac) untuk melacak string sensitif seperti `api.aladhan.com` atau `app_name`.

---

## B. Apktool (APK Reverse Engineering)
Alat untuk membongkar berkas APK menjadi kode assembly bytecode Smali dan berkas sumber daya asli (resources), lalu merakitnya kembali setelah dimodifikasi.

* **Penggunaan Utama**:
  * Melakukan tampering (modifikasi perilaku aplikasi) tanpa memiliki kode sumber asli.
  * Menganalisis file konfigurasi biner seperti `AndroidManifest.xml` asli yang terkompilasi.
* **Langkah Menggunakan**:
  1. Pembongkaran APK (Decompile):
     ```bash
     apktool d nama-aplikasi.apk -o output_dir
     ```
  2. Buka folder `output_dir` untuk memeriksa berkas XML asli atau memodifikasi logika Smali jika diperlukan.
  3. Perakitan Ulang APK (Rebuild):
     ```bash
     apktool b output_dir -o aplikasi_termodifikasi.apk
     ```
  4. Lakukan penandatanganan ulang (resign) APK yang baru dirakit dengan key milik Anda sendiri menggunakan `apksigner` agar dapat dipasang di perangkat.

---

## C. MobSF (Mobile Security Framework)
Platform otomatis untuk analisis statis, analisis dinamis, dan malware analysis secara komprehensif pada aplikasi mobile.

* **Penggunaan Utama**:
  * Menemukan kerentanan umum secara cepat (OWASP Mobile Top 10).
  * Memindai izin Android, kesalahan konfigurasi keamanan jaringan, serta kelemahan kriptografi.
* **Langkah Menggunakan**:
  1. Jalankan MobSF melalui Docker untuk instalasi instan:
     ```bash
     docker run -it --rm -p 8000:8000 opensecurity/mobsf:latest
     ```
  2. Buka peramban (browser) dan akses alamat `http://localhost:8000`.
  3. Seret file APK Anda ke area unggah yang disediakan.
  4. Tunggu beberapa menit selagi MobSF memindai kode. Hasil pemindaian akan menyajikan laporan ringkas berupa tingkat kepatuhan keamanan dan skor kerentanan.

---

## D. Charles Proxy / MITMProxy (Network Interceptor)
Alat untuk merekam dan menganalisis lalu lintas data HTTP dan HTTPS antara perangkat Android Anda dan internet.

* **Penggunaan Utama**:
  * Memverifikasi muatan JSON data jadwal sholat.
  * Memastikan SSL Pinning menolak koneksi intercept secara dinamis.
* **Langkah Menggunakan**:
  1. Unduh dan jalankan Charles Proxy di komputer Anda.
  2. Ekspor Sertifikat SSL Charles: Pilih menu **Help > SSL Proxying > Install Charles Root Certificate on a Mobile Device or Remote Browser**.
  3. Pada perangkat Android:
     * Pasang sertifikat Charles Root (.pem/.cer) ke dalam system storage melalui **Settings > Security > Encryption & Credentials > Install a Certificate**.
     * Atur konfigurasi Proxy WiFi agar menunjuk ke IP komputer Anda (port default: `8888`).
  4. Aktifkan SSL Proxying di Charles untuk domain `api.aladhan.com` dengan klik kanan pada domain tersebut dan pilih **Enable SSL Proxying**.

---

## E. Android Debug Bridge (adb)
Alat baris perintah serbaguna yang menghubungkan komputer Anda dengan perangkat Android/Emulator untuk debugging, manipulasi data, dan audit komponen.

* **Penggunaan Utama**:
  * Mengambil salinan data lokal aplikasi (backup audit).
  * Memeriksa log sistem secara realtime menggunakan `logcat`.
* **Perintah Penting**:
  * Memeriksa daftar perangkat terhubung:
    ```bash
    adb devices
    ```
  * Menampilkan log error keamanan aplikasi secara realtime:
    ```bash
    adb logcat *:E | grep PrayerService
    ```
  * Mengirim intent siaran (broadcast) secara paksa:
    ```bash
    adb shell am broadcast -a id.ideahousetech.prayertime_qibla.action.UPDATE_WIDGET
    ```
  * Menjelajahi file sistem privat aplikasi (memerlukan hak akses Root/Emulator):
    ```bash
    adb shell "run-as id.ideahousetech.prayertime_qibla ls -l /data/data/id.ideahousetech.prayertime_qibla/shared_prefs"
    ```

---

## F. Frida (Dynamic Instrumentation Toolkit)
Kerangka kerja instrumentasi dinamis yang memungkinkan Anda menyuntikkan skrip JavaScript kustom ke dalam proses runtime aplikasi yang sedang berjalan.

* **Penggunaan Utama**:
  * Melakukan bypass SSL Pinning atau Root Detection secara realtime tanpa memodifikasi berkas APK fisik.
  * Melakukan hook pada fungsi verifikasi internal untuk memantau nilai argumen.
* **Langkah Menggunakan**:
  1. Pasang alat frida pada sistem komputer:
     ```bash
     pip install frida-tools
     ```
  2. Unduh berkas biner `frida-server` yang sesuai dengan arsitektur CPU perangkat Anda (x86/ARM) dari GitHub rilis Frida, lalu dorong ke perangkat:
     ```bash
     adb push frida-server /data/local/tmp/
     adb shell "chmod 755 /data/local/tmp/frida-server"
     adb shell "/data/local/tmp/frida-server &"
     ```
  3. Jalankan pemantauan runtime atau injeksi skrip kustom (misalnya memantau data yang disimpan ke SecurePrefs):
     ```bash
     frida -U -f id.ideahousetech.prayertime_qibla -l trace_crypto.js
     ```
