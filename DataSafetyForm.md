# Formulir Keamanan Data Google Play (Google Play Data Safety Form Guide)

Dokumen ini memandu Anda mengisi kuesioner **Keamanan Data (Data Safety)** di Google Play Console secara akurat untuk aplikasi **Jadwal Sholat & Kiblat**.

---

## 1. Ringkasan Kebijakan Privasi & Enkripsi
* **Enkripsi dalam Transit**: Seluruh data yang dikirimkan oleh aplikasi dienkripsi menggunakan protokol HTTPS yang aman (dengan SSL Pinning aktif untuk mencegah intersepsi).
* **Penghapusan Data**: Pengguna dapat menghapus seluruh data pribadi mereka (seperti riwayat koordinat GPS lokasi terakhir, bookmark, dan pengaturan suara) kapan saja dengan menghapus penyimpanan data aplikasi melalui menu pengaturan Android (Clear App Data). Tidak ada data yang tersimpan di cloud eksternal.

---

## 2. Isian Formulir Tanya-Jawab Keamanan Data

### Pertanyaan Umum (General Questions)
1. **Apakah aplikasi Anda mengumpulkan atau membagikan salah satu tipe data pengguna yang diperlukan?**
   * Jawaban: **Ya (Yes)**.
2. **Apakah semua data pengguna yang dikumpulkan oleh aplikasi Anda dienkripsi saat dikirim (transit)?**
   * Jawaban: **Ya (Yes)**. Seluruh lalu lintas jaringan dipaksa menggunakan HTTPS dengan SSL Pinning.
3. **Apakah Anda menyediakan cara bagi pengguna untuk meminta agar data mereka dihapus?**
   * Jawaban: **Ya (Yes)**. Pengguna dapat menghapus cache, database, dan lokasi dari pengaturan aplikasi atau system settings (Clear Storage).

---

## 3. Rincian Pengumpulan & Pembagian Data (Data Types Collected)

Di bawah ini adalah rincian data spesifik yang dikumpulkan dan tujuannya:

### A. Lokasi (Location)
* **Tipe Data 1**: Lokasi Perkiraan (Approximate Location / Coarse Location)
  * **Dikumpulkan (Collected)**: Ya.
  * **Dibagikan (Shared)**: Tidak.
  * **Tujuan Penggunaan**: Fungsionalitas Aplikasi (App Functionality) – Digunakan untuk mengalkulasi zona waktu astronomis dan jadwal sholat setempat.
  * **Apakah diproses secara ephemeral?**: Ya. Koordinat hanya dikirimkan ke endpoint API terenkripsi untuk mendapatkan jadwal sholat dan tidak disimpan di server eksternal mana pun.
* **Tipe Data 2**: Lokasi Akurat (Precise Location / Fine Location)
  * **Dikumpulkan (Collected)**: Ya.
  * **Dibagikan (Shared)**: Tidak.
  * **Tujuan Penggunaan**: Fungsionalitas Aplikasi (App Functionality) – Digunakan untuk kalkulasi sudut arah kompas kiblat yang sangat presisi secara lokal menggunakan sensor magnetik perangkat.
  * **Apakah diproses secara ephemeral?**: Ya. Koordinat diproses secara lokal di perangkat dan hanya disimpan sebagai cache privat lokal terenkripsi di dalam sandboxed storage aplikasi.

### B. Aktivitas Aplikasi (App Activity)
* **Tipe Data 1**: Interaksi Aplikasi (App Interactions)
  * **Dikumpulkan (Collected)**: Ya.
  * **Dibagikan (Shared)**: Tidak.
  * **Tujuan Penggunaan**: Analisis Internal (Analytics) – Mengukur pemakaian fitur penghitung Tasbih digital dan bookmark Quran untuk pengoptimalan UX (jika menggunakan library analitik lokal murni).

---

## 4. Pembagian Data dengan Pihak Ketiga (Third-Party Sharing)
* **Apakah ada data yang dibagikan ke pihak ketiga?**
  * Jawaban: **Tidak ada (No)**. Aplikasi ini berkomitmen penuh menjaga privasi ibadah pengguna. Seluruh kalkulasi astronomis dilakukan secara ephemeral melalui request API langsung tanpa perantara periklanan, pelacakan pihak ketiga (no 3rd-party trackers), atau makelar data.
