# Sistem Manajemen Produksi dan Penjualan Konsinyasi Pengrajin Sofa Cipacing

Aplikasi desktop berbasis Java Swing untuk membantu UMKM pengrajin kursi sofa di Desa Cipacing, Jatinangor, dalam mengelola produksi, stok bahan, pesanan custom, dan penjualan konsinyasi lewat toko meubel.

> Proyek praktikum Pemrograman: Java Swing + integrasi database (MySQL). Dikerjakan berkelompok (4 orang).
Salomo Reinhard Kennedy Sihite 140810250034
Muhammad Miftah Rasyid 140810250076
Constantine Luke Hanindito 140810250073
Kian Islam Susanto 140810250079

## Latar Belakang

Cipacing dikenal sebagai desa pengrajin di Jatinangor, dan pengrajin kursi sofa di sana sudah ada sejak 1987. Berdasarkan penelitian tentang model bisnis pengrajin sofa Cipacing (e-Proceeding of Management, Telkom University, 2016), beberapa kondisi usaha mereka:

- Pendapatan hampir seluruhnya berasal dari sofa yang dititipkan ke toko meubel (B2B).
- Layanan custom design ada, tetapi belum menjadi sumber pendapatan yang berarti.
- Biaya terbesar adalah kain, busa, dan gaji karyawan.
- Belum ada manajemen hubungan pelanggan (CRM) maupun sistem insentif untuk toko mitra.

Aplikasi ini dirancang untuk menjawab kondisi tersebut lewat pencatatan yang rapi dan perhitungan otomatis.

## Fitur

### Fitur Utama
- Login admin
- Manajemen master data: toko meubel, produk sofa, bahan baku
- Penyetoran/konsinyasi: catat sofa yang dititipkan ke toko, status terjual atau retur
- Komisi bertingkat: persentase komisi toko dihitung otomatis berdasarkan jumlah sofa terjual
- Pesanan custom: spesifikasi desain, uang muka (DP), dan status pengerjaan (rangka, jahit, finishing, siap diambil)
- Stok bahan dan HPP: pemakaian kain/busa per model, harga pokok produksi per sofa

### Fitur Tambahan (jika waktu cukup)
- Laporan laba per sofa dan per toko
- Peringatan stok bahan menipis
- Diskon loyalitas untuk toko/pelanggan langganan
- Data pemasok dan riwayat pembelian bahan

## Teknologi

| Komponen | Keterangan |
|----------|------------|
| Bahasa | Java |
| Antarmuka | Java Swing |
| Koneksi database | JDBC (MySQL Connector/J) |
| Database | MySQL (via Laragon) |
| IDE | NetBeans |

## Struktur Database

Rancangan awal

| Tabel | Keterangan |
|-------|------------|
| toko_meubel | Data toko mitra dan tingkat komisi |
| produk_sofa | Model sofa, harga jual, kebutuhan bahan |
| bahan | Stok kain, busa, dan rangka |
| pesanan | Pesanan custom atau dari toko, status, dan DP |
| penyetoran | Sofa yang dititipkan ke toko, jumlah terjual dan retur |

Rencana tambahan: pemasok, pembelian_bahan, pelanggan, karyawan.

## Referensi

- Khodijah, I. S. & Ghina, A. (2016). *Analisis Pengembangan Usaha Mikro Kecil Menengah (UMKM) pada Pengrajin Kursi Sofa Cipacing, Jatinangor dengan Pendekatan Business Model Canvas.* e-Proceeding of Management, Vol. 3, No. 2, Universitas Telkom.
