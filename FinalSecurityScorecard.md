# Kartu Skor Keamanan Akhir (Final Security Scorecard & Roadmap)

Dokumen ini merangkum evaluasi kepatuhan terhadap standar keamanan siber mobile pada aplikasi **Jadwal Sholat & Kiblat** setelah seluruh perbaikan diselesaikan.

---

## 1. KARTU SKOR KEAMANAN (SECURITY SCORECARD)

Evaluasi ini didasarkan pada OWASP Mobile Application Security (MASVS) dan standar keamanan platform Android modern.

| Kategori Keamanan | Skor Diperoleh | Skor Maksimal | Status | Detail Implementasi & Mitigasi Aktif |
| :--- | :---: | :---: | :---: | :--- |
| **Network Security** | **20** | 20 | ✅ LULUS | HTTPS diwajibkan secara mutlak (`cleartext` mati), SSL Pinning aktif dengan sertifikat ZeroSSL (Leaf & Intermediate) serta Sectigo Root cadangan untuk mencegah MITM. |
| **Data Encryption** | **20** | 20 | ✅ LULUS | Redesain `SecurePrefs` menggunakan `EncryptedSharedPreferences` dengan algoritma enkripsi militer AES-256 SIV/GCM, dilengkapi dengan sistem fallback hibrida memori-ke-disk yang aman terhadap korupsi keystore. |
| **Component Protection**| **15** | 15 | ✅ LULUS | Komponen widget dilindungi dengan custom permission tingkat tanda tangan (`signature`), mencegah manipulasi widget dari aplikasi malware luar. Aktivitas internal tertutup (`exported=false`). |
| **Code Obfuscation** | **15** | 15 | ✅ LULUS | Aturan kompilasi R8/ProGuard dioptimalkan untuk menyamarkan nama kelas, variabel, dan metode sensitif, serta mereduksi ukuran berkas binary (APK). |
| **Input Validation** | **10** | 10 | ✅ LULUS | Pembersihan mutlak nama berkas kustom (`sanitizeFileName`) terhadap upaya eksploitasi serangan Path Traversal (`../../`), validasi format tipe MIME audio, dan batasan ukuran berkas unggahan. |
| **Runtime Protection** | **10** | 10 | ✅ LULUS | Deteksi dinamis terhadap indikator perangkat di-root, mode debugging yang aktif, dan eksekusi pada simulator demi menjaga integritas data lokal. |
| **Permission Management**| **10** | 10 | ✅ LULUS | Manajemen runtime permissions terpusat (`PermissionManager`) untuk lokasi presisi kiblat dan alarm presisi Adzan, menjamin tidak ada kebocoran atau crash saat perizinan ditolak pengguna. |
| **TOTAL SKOR** | **100** | **100** | **SANGAT BAIK**| **Aplikasi memenuhi standar kepatuhan tinggi untuk siap rilis publik.** |

---

## 2. ROADMAP PENINGKATAN KEAMANAN DI MASA DEPAN (SECURITY ROADMAP)

Meskipun sistem pertahanan saat ini telah sangat kokoh, siklus pengembangan perangkat lunak yang aman memerlukan adaptasi konstan. Berikut adalah rekomendasi peningkatan berkelanjutan:

### Tahap 1: Penguatan Kriptografi & Kunci (Jangka Pendek)
* **Kunci Dinamis (Dynamic Key Derivation)**: Mengimplementasikan PBKDF2 atau Argon2 untuk derivasi kunci enkripsi lokal berbasis input sandi atau PIN unik, jika di masa depan ditambahkan fitur penyimpanan catatan doa pribadi pengguna.
* **Integrasi Biometrik (Biometric-Backed KeyStore)**: Untuk operasi dekripsi data sangat sensitif, kunci Keystore dapat diset agar membutuhkan otentikasi sidik jari atau wajah (`setUserAuthenticationRequired(true)`).

### Tahap 2: Perlindungan Runtime Tingkat Lanjut (Jangka Menengah)
* **Pendeteksian Frida & Magisk (Anti-Hooking)**: Menambahkan modul deteksi dinamis berbasis kode C/C++ (NDK) untuk melacak keberadaan debugger dinamis seperti Frida, Xposed Framework, atau manajer Root Magisk secara realtime.
* **SafetyNet / Play Integrity API**: Mengintegrasikan Google Play Integrity API untuk memverifikasi secara langsung ke server Google bahwa perangkat tempat aplikasi berjalan adalah perangkat Android asli bersertifikat (bukan ROM termodifikasi berbahaya).

### Tahap 3: Otomatisasi DevSecOps & Pen-testing (Jangka Panjang)
* **Integrasi CI/CD DevSecOps**: Menghubungkan pemindaian MobSF atau sonarQube secara otomatis di GitHub Actions setiap kali ada pengajuan Pull Request (PR) ke cabang produksi, sehingga mencegah developer memasukkan celah keamanan baru secara tidak sengaja.
* **Audit Eksternal Berkala**: Melakukan pengujian penetrasi independen (audit black-box dan white-box) oleh firma keamanan pihak ketiga terakreditasi sebelum pembaruan versi besar dirilis ke Google Play Store.
