package sofa;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Pesanan custom: spesifikasi, DP, status pengerjaan, pelunasan. */
public class PesananPanel extends JPanel implements Ui.Refreshable {
    private final JTextField tfNama = new JTextField(), tfKontak = new JTextField(),
            tfSpek = new JTextField(), tfHarga = new JTextField(), tfDp = new JTextField();
    private final JComboBox<Ui.Item> cbProduk = new JComboBox<>();
    private final JComboBox<String> cbJenis = new JComboBox<>(new String[]{"custom", "toko"});
    private final JComboBox<String> cbStatus =
        new JComboBox<>(new String[]{"rangka", "jahit", "finishing", "siap_diambil", "selesai"});
    private final JTable tbl = new JTable();
    private int selected = -1;

    public PesananPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 6));
        form.add(new JLabel("Nama pelanggan")); form.add(tfNama);
        form.add(new JLabel("Kontak")); form.add(tfKontak);
        form.add(new JLabel("Model dasar")); form.add(cbProduk);
        form.add(new JLabel("Jenis pesanan")); form.add(cbJenis);
        form.add(new JLabel("Spesifikasi desain")); form.add(tfSpek);
        form.add(new JLabel("Status pengerjaan")); form.add(cbStatus);
        form.add(new JLabel("Harga total (Rp)")); form.add(tfHarga);
        form.add(new JLabel("Uang muka / DP (Rp)")); form.add(tfDp);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton tambah = new JButton("Tambah Pesanan"), ubah = new JButton("Ubah / Update Status"),
                lunas = new JButton("Catat Pelunasan"), hapus = new JButton("Hapus"),
                bersih = new JButton("Bersihkan");
        btn.add(tambah); btn.add(ubah); btn.add(lunas); btn.add(hapus); btn.add(bersih);

        JPanel north = new JPanel(new BorderLayout(4, 4));
        north.add(form, BorderLayout.NORTH);
        north.add(btn, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        tambah.addActionListener(e -> tambah());
        ubah.addActionListener(e -> ubah());
        lunas.addActionListener(e -> lunasi());
        hapus.addActionListener(e -> hapus());
        bersih.addActionListener(e -> { tbl.clearSelection(); resetForm(); });
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) pick(); });
        refresh();
    }

    @Override public void refresh() {
        int p = Ui.id(cbProduk);
        Ui.fill(cbProduk, "SELECT id_produk, nama_model FROM produk_sofa ORDER BY nama_model");
        cbProduk.insertItemAt(new Ui.Item(0, "(Desain custom / tanpa model)"), 0);
        cbProduk.setSelectedIndex(0);
        if (p > 0) Ui.select(cbProduk, p);
        loadTable();
    }

    private void loadTable() {
        DefaultTableModel m = Db.query(
            "SELECT o.id_pesanan AS ID, o.nama_pelanggan AS Pelanggan, o.kontak AS Kontak, "
            + "COALESCE(p.nama_model,'Custom') AS Model, o.jenis AS Jenis, o.spesifikasi AS Spesifikasi, "
            + "o.tanggal AS Tanggal, o.harga AS Harga, o.dp AS DP, "
            + "IF(o.lunas=1,0,o.harga-o.dp) AS `Sisa bayar`, o.status AS Status, "
            + "IF(o.lunas=1,'Lunas','Belum') AS Pembayaran "
            + "FROM pesanan o LEFT JOIN produk_sofa p ON p.id_produk=o.id_produk "
            + "ORDER BY o.id_pesanan DESC");
        tbl.setModel(m);
        selected = -1;
    }

    private void pick() {
        int r = tbl.getSelectedRow();
        if (r < 0) { selected = -1; return; }
        selected = ((Number) tbl.getValueAt(r, 0)).intValue();
        DefaultTableModel q = Db.query("SELECT nama_pelanggan,kontak,id_produk,jenis,spesifikasi,harga,dp,status "
            + "FROM pesanan WHERE id_pesanan=?", selected);
        if (q.getRowCount() == 0) return;
        tfNama.setText(String.valueOf(q.getValueAt(0, 0)));
        tfKontak.setText(q.getValueAt(0, 1) == null ? "" : q.getValueAt(0, 1).toString());
        cbProduk.setSelectedIndex(0);
        if (q.getValueAt(0, 2) != null) Ui.select(cbProduk, ((Number) q.getValueAt(0, 2)).intValue());
        cbJenis.setSelectedItem(q.getValueAt(0, 3));
        tfSpek.setText(q.getValueAt(0, 4) == null ? "" : q.getValueAt(0, 4).toString());
        tfHarga.setText(String.valueOf(q.getValueAt(0, 5)));
        tfDp.setText(String.valueOf(q.getValueAt(0, 6)));
        cbStatus.setSelectedItem(q.getValueAt(0, 7));
    }

    private void resetForm() {
        tfNama.setText(""); tfKontak.setText(""); tfSpek.setText(""); tfHarga.setText(""); tfDp.setText("");
        if (cbProduk.getItemCount() > 0) cbProduk.setSelectedIndex(0);
        cbJenis.setSelectedIndex(0); cbStatus.setSelectedIndex(0);
    }

    /** Hasil validasi: {harga, dp} atau null jika tidak valid. */
    private double[] validasi() {
        if (tfNama.getText().trim().isEmpty()) { Ui.warn(this, "Nama pelanggan wajib diisi."); return null; }
        double harga, dp;
        try { harga = Ui.parse(tfHarga.getText()); dp = Ui.parse(tfDp.getText()); }
        catch (NumberFormatException ex) { Ui.warn(this, "Harga dan DP harus berupa angka."); return null; }
        if (harga < 0 || dp < 0) { Ui.warn(this, "Harga dan DP tidak boleh negatif."); return null; }
        if (dp > harga) { Ui.warn(this, "DP tidak boleh melebihi harga total."); return null; }
        return new double[]{harga, dp};
    }

    private Object idProduk() {
        int id = Ui.id(cbProduk);
        return id > 0 ? (Object) id : null;
    }

    private void tambah() {
        double[] v = validasi();
        if (v == null) return;
        if (Db.exec("INSERT INTO pesanan(nama_pelanggan,kontak,id_produk,jenis,spesifikasi,tanggal,harga,dp,status) "
                + "VALUES(?,?,?,?,?,CURDATE(),?,?,?)", tfNama.getText().trim(), tfKontak.getText().trim(),
                idProduk(), cbJenis.getSelectedItem(), tfSpek.getText().trim(), v[0], v[1],
                cbStatus.getSelectedItem())) {
            resetForm();
            loadTable();
        }
    }

    private void ubah() {
        if (selected < 0) { Ui.warn(this, "Pilih pesanan di tabel dulu."); return; }
        double[] v = validasi();
        if (v == null) return;
        if (Db.exec("UPDATE pesanan SET nama_pelanggan=?, kontak=?, id_produk=?, jenis=?, spesifikasi=?, "
                + "harga=?, dp=?, status=? WHERE id_pesanan=?", tfNama.getText().trim(),
                tfKontak.getText().trim(), idProduk(), cbJenis.getSelectedItem(), tfSpek.getText().trim(),
                v[0], v[1], cbStatus.getSelectedItem(), selected))
            loadTable();
    }

    private void lunasi() {
        if (selected < 0) { Ui.warn(this, "Pilih pesanan di tabel dulu."); return; }
        Object st = Db.scalar("SELECT status FROM pesanan WHERE id_pesanan=?", selected);
        Object lunas = Db.scalar("SELECT lunas FROM pesanan WHERE id_pesanan=?", selected);
        if (lunas != null && ((Number) lunas).intValue() == 1) { Ui.info(this, "Pesanan ini sudah lunas."); return; }
        if (!"siap_diambil".equals(st)) {
            Ui.warn(this, "Pelunasan dicatat setelah pesanan berstatus 'siap_diambil'.");
            return;
        }
        if (Db.exec("UPDATE pesanan SET lunas=1, tanggal_lunas=CURDATE(), status='selesai' "
                + "WHERE id_pesanan=?", selected))
            loadTable();
    }

    private void hapus() {
        if (selected < 0) { Ui.warn(this, "Pilih pesanan di tabel dulu."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus pesanan ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (Db.exec("DELETE FROM pesanan WHERE id_pesanan=?", selected)) loadTable();
    }
}
