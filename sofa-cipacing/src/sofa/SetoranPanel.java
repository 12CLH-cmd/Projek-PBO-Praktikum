package sofa;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Penyetoran (konsinyasi) sofa ke toko meubel + komisi bertingkat otomatis. */
public class SetoranPanel extends JPanel implements Ui.Refreshable {
    private final JComboBox<Ui.Item> cbToko = new JComboBox<>(), cbProduk = new JComboBox<>();
    private final JSpinner spSetor = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JSpinner spTerjual = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JSpinner spRetur = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JTable tbl = new JTable();
    private final JLabel lblInfo = new JLabel(" ");
    private int selected = -1;
    private boolean loading = false;

    public SetoranPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 6));
        form.add(new JLabel("Toko meubel")); form.add(cbToko);
        form.add(new JLabel("Produk sofa")); form.add(cbProduk);
        form.add(new JLabel("Jumlah dititipkan")); form.add(spSetor);
        form.add(new JLabel("Jumlah terjual")); form.add(spTerjual);
        form.add(new JLabel("Jumlah retur")); form.add(spRetur);

        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton simpan = new JButton("Simpan Setoran Baru"), ubah = new JButton("Perbarui Setoran Terpilih"),
                hapus = new JButton("Hapus"), bersih = new JButton("Bersihkan");
        btn.add(simpan); btn.add(ubah); btn.add(hapus); btn.add(bersih);
        lblInfo.setFont(lblInfo.getFont().deriveFont(Font.BOLD));

        JPanel north = new JPanel(new BorderLayout(4, 4));
        north.add(form, BorderLayout.NORTH);
        north.add(btn, BorderLayout.CENTER);
        north.add(lblInfo, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        cbToko.addActionListener(e -> { if (!loading) updateInfo(); });
        simpan.addActionListener(e -> simpan());
        ubah.addActionListener(e -> ubah());
        hapus.addActionListener(e -> hapus());
        bersih.addActionListener(e -> { tbl.clearSelection(); resetForm(); });
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) pick(); });
        refresh();
    }

    @Override public void refresh() {
        loading = true;
        int t = Ui.id(cbToko), p = Ui.id(cbProduk);
        Ui.fill(cbToko, "SELECT id_toko, nama_toko FROM toko_meubel ORDER BY nama_toko");
        Ui.fill(cbProduk, "SELECT id_produk, nama_model FROM produk_sofa ORDER BY nama_model");
        Ui.select(cbToko, t);
        Ui.select(cbProduk, p);
        loading = false;
        loadTable();
        updateInfo();
    }

    private void updateInfo() {
        int idt = Ui.id(cbToko);
        if (idt < 0) { lblInfo.setText(" "); return; }
        int tot = Komisi.totalTerjual(idt);
        lblInfo.setText("Total terjual toko ini: " + tot + " sofa  ->  komisi saat ini "
            + String.format("%.1f%%", Komisi.persen(tot)));
    }

    private void loadTable() {
        DefaultTableModel src = Db.query(
            "SELECT s.id_setoran, s.id_toko, t.nama_toko, p.nama_model, s.jumlah_setor, s.jumlah_terjual, "
            + "s.jumlah_retur, s.tanggal, s.jumlah_terjual*p.harga_jual "
            + "FROM penyetoran s JOIN toko_meubel t ON t.id_toko=s.id_toko "
            + "JOIN produk_sofa p ON p.id_produk=s.id_produk "
            + "ORDER BY s.tanggal DESC, s.id_setoran DESC");
        DefaultTableModel m = new DefaultTableModel(new Object[]{"ID", "Toko", "Produk", "Dititipkan",
                "Terjual", "Retur", "Sisa di toko", "Tanggal", "Total penjualan", "Komisi %", "Komisi toko",
                "Diterima pengrajin"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        Map<Integer, Double> pct = new HashMap<>();
        for (int r = 0; r < src.getRowCount(); r++) {
            int idToko = ((Number) src.getValueAt(r, 1)).intValue();
            if (!pct.containsKey(idToko)) pct.put(idToko, Komisi.persen(Komisi.totalTerjual(idToko)));
            double persen = pct.get(idToko);
            double total = Ui.num(src.getValueAt(r, 8));
            double komisi = total * persen / 100.0;
            int setor = (int) Ui.num(src.getValueAt(r, 4));
            int terjual = (int) Ui.num(src.getValueAt(r, 5));
            int retur = (int) Ui.num(src.getValueAt(r, 6));
            m.addRow(new Object[]{src.getValueAt(r, 0), src.getValueAt(r, 2), src.getValueAt(r, 3),
                setor, terjual, retur, setor - terjual - retur, src.getValueAt(r, 7), Ui.rp(total),
                String.format("%.1f%%", persen), Ui.rp(komisi), Ui.rp(total - komisi)});
        }
        tbl.setModel(m);
        selected = -1;
    }

    private void pick() {
        int r = tbl.getSelectedRow();
        if (r < 0) { selected = -1; return; }
        selected = ((Number) tbl.getValueAt(r, 0)).intValue();
        DefaultTableModel q = Db.query("SELECT id_toko,id_produk,jumlah_setor,jumlah_terjual,jumlah_retur "
            + "FROM penyetoran WHERE id_setoran=?", selected);
        if (q.getRowCount() == 0) return;
        loading = true;
        Ui.select(cbToko, ((Number) q.getValueAt(0, 0)).intValue());
        Ui.select(cbProduk, ((Number) q.getValueAt(0, 1)).intValue());
        spSetor.setValue(((Number) q.getValueAt(0, 2)).intValue());
        spTerjual.setValue(((Number) q.getValueAt(0, 3)).intValue());
        spRetur.setValue(((Number) q.getValueAt(0, 4)).intValue());
        loading = false;
        updateInfo();
    }

    private void resetForm() {
        spSetor.setValue(0); spTerjual.setValue(0); spRetur.setValue(0);
    }

    private boolean valid() {
        int s = (Integer) spSetor.getValue(), t = (Integer) spTerjual.getValue(), r = (Integer) spRetur.getValue();
        if (Ui.id(cbToko) < 0 || Ui.id(cbProduk) < 0) { Ui.warn(this, "Pilih toko dan produk dulu."); return false; }
        if (s <= 0) { Ui.warn(this, "Jumlah dititipkan harus lebih dari 0."); return false; }
        if (t + r > s) { Ui.warn(this, "Terjual + retur tidak boleh melebihi jumlah yang dititipkan."); return false; }
        return true;
    }

    private void afterSave() { loadTable(); updateInfo(); }

    private void simpan() {
        if (!valid()) return;
        if (Db.exec("INSERT INTO penyetoran(id_toko,id_produk,jumlah_setor,jumlah_terjual,jumlah_retur,tanggal) "
                + "VALUES(?,?,?,?,?,CURDATE())", Ui.id(cbToko), Ui.id(cbProduk),
                spSetor.getValue(), spTerjual.getValue(), spRetur.getValue())) {
            resetForm();
            afterSave();
        }
    }

    private void ubah() {
        if (selected < 0) { Ui.warn(this, "Pilih baris riwayat setoran dulu."); return; }
        if (!valid()) return;
        if (Db.exec("UPDATE penyetoran SET id_toko=?, id_produk=?, jumlah_setor=?, jumlah_terjual=?, "
                + "jumlah_retur=? WHERE id_setoran=?", Ui.id(cbToko), Ui.id(cbProduk),
                spSetor.getValue(), spTerjual.getValue(), spRetur.getValue(), selected))
            afterSave();
    }

    private void hapus() {
        if (selected < 0) { Ui.warn(this, "Pilih baris riwayat setoran dulu."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus setoran ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (Db.exec("DELETE FROM penyetoran WHERE id_setoran=?", selected)) afterSave();
    }
}
