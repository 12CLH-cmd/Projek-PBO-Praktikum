-- Sistem Manajemen Produksi & Penjualan Konsinyasi Pengrajin Sofa Cipacing
-- Jalankan di phpMyAdmin / HeidiSQL (Laragon) sebagai root.
DROP DATABASE IF EXISTS db_sofa_cipacing;
CREATE DATABASE db_sofa_cipacing CHARACTER SET utf8mb4;
USE db_sofa_cipacing;

CREATE TABLE users (
  id_user INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password CHAR(64) NOT NULL            -- SHA-256
);

CREATE TABLE toko_meubel (
  id_toko INT AUTO_INCREMENT PRIMARY KEY,
  nama_toko VARCHAR(100) NOT NULL,
  alamat VARCHAR(200),
  kontak VARCHAR(30)
);

-- Tingkat komisi bertingkat: dipilih berdasarkan TOTAL sofa terjual oleh toko tsb.
CREATE TABLE komisi_tier (
  id_tier INT AUTO_INCREMENT PRIMARY KEY,
  min_terjual INT NOT NULL,
  persen DECIMAL(5,2) NOT NULL,
  label VARCHAR(30)
);

CREATE TABLE produk_sofa (
  id_produk INT AUTO_INCREMENT PRIMARY KEY,
  nama_model VARCHAR(100) NOT NULL,
  harga_jual DECIMAL(12,2) NOT NULL DEFAULT 0,
  deskripsi VARCHAR(255)
);

CREATE TABLE bahan (
  id_bahan INT AUTO_INCREMENT PRIMARY KEY,
  nama_bahan VARCHAR(100) NOT NULL,
  satuan VARCHAR(20) NOT NULL,
  stok DECIMAL(12,2) NOT NULL DEFAULT 0,
  harga_satuan DECIMAL(12,2) NOT NULL DEFAULT 0,
  stok_minimum DECIMAL(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE kebutuhan_bahan (
  id_produk INT NOT NULL,
  id_bahan INT NOT NULL,
  jumlah_per_unit DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id_produk, id_bahan),
  FOREIGN KEY (id_produk) REFERENCES produk_sofa(id_produk) ON DELETE CASCADE,
  FOREIGN KEY (id_bahan) REFERENCES bahan(id_bahan) ON DELETE CASCADE
);

CREATE TABLE penyetoran (
  id_setoran INT AUTO_INCREMENT PRIMARY KEY,
  id_toko INT NOT NULL,
  id_produk INT NOT NULL,
  jumlah_setor INT NOT NULL,
  jumlah_terjual INT NOT NULL DEFAULT 0,
  jumlah_retur INT NOT NULL DEFAULT 0,
  tanggal DATE NOT NULL,
  FOREIGN KEY (id_toko) REFERENCES toko_meubel(id_toko),
  FOREIGN KEY (id_produk) REFERENCES produk_sofa(id_produk)
);

CREATE TABLE pesanan (
  id_pesanan INT AUTO_INCREMENT PRIMARY KEY,
  nama_pelanggan VARCHAR(100) NOT NULL,
  kontak VARCHAR(30),
  id_produk INT NULL,                   -- NULL = desain custom tanpa model katalog
  jenis ENUM('custom','toko') NOT NULL DEFAULT 'custom',
  spesifikasi VARCHAR(255),
  tanggal DATE NOT NULL,
  harga DECIMAL(12,2) NOT NULL DEFAULT 0,
  dp DECIMAL(12,2) NOT NULL DEFAULT 0,
  status ENUM('rangka','jahit','finishing','siap_diambil','selesai') NOT NULL DEFAULT 'rangka',
  lunas TINYINT(1) NOT NULL DEFAULT 0,
  tanggal_lunas DATE NULL,
  FOREIGN KEY (id_produk) REFERENCES produk_sofa(id_produk) ON DELETE SET NULL
);

-- ===== Data awal =====
INSERT INTO users(username,password) VALUES ('admin', SHA2('admin123',256));
INSERT INTO komisi_tier(min_terjual,persen,label) VALUES (0,10,'Reguler'),(10,12,'Silver'),(25,15,'Gold');
INSERT INTO toko_meubel(nama_toko,alamat,kontak) VALUES
 ('Meubel Jaya Abadi','Jl. Raya Bandung-Sumedang','081200000001'),
 ('Toko Sinar Furniture','Jl. Cileunyi','081200000002'),
 ('Graha Meubel','Jl. Cibiru','081200000003');
INSERT INTO produk_sofa(nama_model,harga_jual,deskripsi) VALUES
 ('Sofa Minimalis 2 Dudukan',1800000,'Rangka kayu, busa D23'),
 ('Sofa L Klasik',3500000,'Sudut L, kain oscar'),
 ('Sofa Santai 1 Dudukan',900000,'Kursi tunggal');
INSERT INTO bahan(nama_bahan,satuan,stok,harga_satuan,stok_minimum) VALUES
 ('Kain Oscar','meter',120,45000,30),
 ('Busa D23','lembar',40,150000,10),
 ('Rangka Kayu Mahoni','set',25,350000,5),
 ('Kaki Sofa','buah',100,15000,20),
 ('Benang & Paku','paket',50,10000,10);
INSERT INTO kebutuhan_bahan VALUES
 (1,1,6),(1,2,2),(1,3,1),(1,4,4),(1,5,1),
 (2,1,12),(2,2,4),(2,3,2),(2,4,6),(2,5,2),
 (3,1,3),(3,2,1),(3,3,1),(3,4,4),(3,5,1);
