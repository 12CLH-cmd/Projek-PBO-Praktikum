package sofa;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/** CRUD produk sofa + daftar kebutuhan bahan per unit + perhitungan HPP. */
public class ProdukPanel extends JPanel implements Ui.Refreshable {
    private final CrudPanel crud = new CrudPanel("produk_sofa", "id_produk",
        new String[]{"nama_model", "harga_jual", "deskripsi"},
        new String[]{"Nama model", "Harga jual (Rp)", "Deskripsi"});
    private final JComboBox<Ui.Item> cbBahan = new JComboBox<>();
    private final JTextField tfJumlah = new JTextField(8);
    private final JTable tbl = new JTable();
    private final JLabel lblHpp = new JLabel(" ");

    public ProdukPanel() {
        setLayout(new BorderLayout());
        JPanel bawah = new JPanel(new BorderLayout(6, 6));
        bawah.setBorder(new TitledBorder("Kebutuhan bahan per unit & HPP"));
        JPanel in = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton simpan = new JButton("Simpan Kebutuhan"), hapus = new JButton("Hapus Bahan");
        in.add(new JLabel("Bahan")); in.add(cbBahan);
        in.add(new JLabel("Jumlah per unit")); in.add(tfJumlah);
        in.add(simpan); in.add(hapus);
        bawah.add(in, BorderLayout.NORTH);
        bawah.add(new JScrollPane(tbl), BorderLayout.CENTER);
        lblHpp.setFont(lblHpp.getFont().deriveFont(Font.BOLD));
        bawah.add(lblHpp, BorderLayout.SOUTH);

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, crud, bawah);
        sp.setResizeWeight(0.5);
        add(sp);

        crud.setOnSelect(this::loadKebutuhan);
        tbl.getSelectionModel().addListSelectionListener(e -> {
            int r = tbl.getSelectedRow();
            if (e.getValueIsAdjusting() || r < 0) return;
            Ui.select(cbBahan, ((Number) tbl.getValueAt(r, 0)).intValue());
            tfJumlah.setText(String.valueOf(tbl.getValueAt(r, 3)));
        });
        simpan.addActionListener(e -> simpanKebutuhan());
        hapus.addActionListener(e -> hapusKebutuhan());
        refresh();
    }

    @Override public void refresh() {
        crud.refresh();
        Ui.fill(cbBahan, "SELECT id_bahan, nama_bahan FROM bahan ORDER BY nama_bahan");
        loadKebutuhan();
    }

    private void loadKebutuhan() {
        int id = crud.selectedId();
        if (id < 0) {
            tbl.setModel(new DefaultTableModel());
            lblHpp.setText("Pilih produk sofa pada tabel di atas untuk mengatur kebutuhan bahannya.");
            return;
        }
        DefaultTableModel m = Db.query(
            "SELECT k.id_bahan AS `ID Bahan`, b.nama_bahan AS Bahan, b.satuan AS Satuan, "
            + "k.jumlah_per_unit AS `Jumlah/unit`, b.harga_satuan AS `Harga satuan`, "
            + "k.jumlah_per_unit*b.harga_satuan AS Subtotal "
            + "FROM kebutuhan_bahan k JOIN bahan b ON b.id_bahan=k.id_bahan "
            + "WHERE k.id_produk=? ORDER BY b.nama_bahan", id);
        tbl.setModel(m);
        double hpp = 0;
        for (int r = 0; r < m.getRowCount(); r++) hpp += Ui.num(m.getValueAt(r, 5));
        double jual = Ui.num(Db.scalar("SELECT harga_jual FROM produk_sofa WHERE id_produk=?", id));
        lblHpp.setText("HPP per sofa: " + Ui.rp(hpp) + "   |   Harga jual: " + Ui.rp(jual)
            + "   |   Laba kotor per sofa: " + Ui.rp(jual - hpp));
    }

    private void simpanKebutuhan() {
        int idp = crud.selectedId(), idb = Ui.id(cbBahan);
        if (idp < 0 || idb < 0) { Ui.warn(this, "Pilih produk (tabel atas) dan bahan dulu."); return; }
        double jml;
        try { jml = Ui.parse(tfJumlah.getText()); }
        catch (NumberFormatException ex) { Ui.warn(this, "Jumlah harus berupa angka."); return; }
        if (jml <= 0) { Ui.warn(this, "Jumlah per unit harus lebih dari 0."); return; }
        if (Db.exec("INSERT INTO kebutuhan_bahan(id_produk,id_bahan,jumlah_per_unit) VALUES(?,?,?) "
                + "ON DUPLICATE KEY UPDATE jumlah_per_unit=VALUES(jumlah_per_unit)", idp, idb, jml))
            loadKebutuhan();
    }

    private void hapusKebutuhan() {
        int r = tbl.getSelectedRow();
        if (r < 0) { Ui.warn(this, "Pilih bahan pada tabel kebutuhan dulu."); return; }
        if (Db.exec("DELETE FROM kebutuhan_bahan WHERE id_produk=? AND id_bahan=?",
                crud.selectedId(), ((Number) tbl.getValueAt(r, 0)).intValue()))
            loadKebutuhan();
    }
}
