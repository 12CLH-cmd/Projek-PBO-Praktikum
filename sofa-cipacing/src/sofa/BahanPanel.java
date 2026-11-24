package sofa;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Stok bahan: CRUD + tambah stok + catat produksi (stok berkurang) + peringatan stok menipis. */
public class BahanPanel extends CrudPanel {
    public BahanPanel() {
        super("bahan", "id_bahan",
            new String[]{"nama_bahan", "satuan", "stok", "harga_satuan", "stok_minimum"},
            new String[]{"Nama bahan", "Satuan", "Stok", "Harga satuan (Rp)", "Stok minimum"});
        JButton tambahStok = new JButton("Tambah Stok"), produksi = new JButton("Catat Produksi");
        buttons.add(tambahStok);
        buttons.add(produksi);
        tambahStok.addActionListener(e -> tambahStok());
        produksi.addActionListener(e -> produksi());

        // baris merah = stok <= stok minimum
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                boolean low = Ui.num(t.getValueAt(r, 3)) <= Ui.num(t.getValueAt(r, 5));
                if (!sel) comp.setBackground(low ? new Color(255, 215, 215) : Color.WHITE);
                return comp;
            }
        });
    }

    private void tambahStok() {
        if (selectedId() < 0) { Ui.warn(this, "Pilih bahan di tabel dulu."); return; }
        String s = JOptionPane.showInputDialog(this, "Jumlah stok yang ditambahkan:");
        if (s == null) return;
        double q;
        try { q = Ui.parse(s); }
        catch (NumberFormatException ex) { Ui.warn(this, "Harus berupa angka."); return; }
        if (q <= 0) { Ui.warn(this, "Jumlah harus lebih dari 0."); return; }
        if (Db.exec("UPDATE bahan SET stok=stok+? WHERE id_bahan=?", q, selectedId())) refresh();
    }

    private void produksi() {
        JComboBox<Ui.Item> cb = new JComboBox<>();
        Ui.fill(cb, "SELECT id_produk, nama_model FROM produk_sofa ORDER BY nama_model");
        JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Model sofa")); p.add(cb);
        p.add(new JLabel("Jumlah diproduksi")); p.add(sp);
        if (JOptionPane.showConfirmDialog(this, p, "Catat Produksi",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        int idp = Ui.id(cb), qty = (Integer) sp.getValue();
        DefaultTableModel need = Db.query(
            "SELECT b.id_bahan, b.nama_bahan, b.stok, k.jumlah_per_unit*? AS butuh "
            + "FROM kebutuhan_bahan k JOIN bahan b ON b.id_bahan=k.id_bahan WHERE k.id_produk=?", qty, idp);
        if (need.getRowCount() == 0) { Ui.warn(this, "Model ini belum punya daftar kebutuhan bahan."); return; }

        StringBuilder kurang = new StringBuilder();
        for (int r = 0; r < need.getRowCount(); r++)
            if (Ui.num(need.getValueAt(r, 2)) < Ui.num(need.getValueAt(r, 3)))
                kurang.append("\n- ").append(need.getValueAt(r, 1)).append(": stok ")
                      .append(need.getValueAt(r, 2)).append(", butuh ").append(need.getValueAt(r, 3));
        if (kurang.length() > 0) { Ui.warn(this, "Stok bahan tidak cukup:" + kurang); return; }

        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement("UPDATE bahan SET stok=stok-? WHERE id_bahan=?")) {
                for (int r = 0; r < need.getRowCount(); r++) {
                    ps.setObject(1, need.getValueAt(r, 3));
                    ps.setObject(2, need.getValueAt(r, 0));
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw ex; }
        } catch (SQLException ex) { Db.error(ex); return; }
        refresh();
        Ui.info(this, "Produksi " + qty + " sofa dicatat, stok bahan sudah dikurangi.");
        peringatan(this);
    }

    /** Dialog peringatan bila ada bahan yang stoknya <= stok minimum. */
    public static void peringatan(Component parent) {
        DefaultTableModel m = Db.query(
            "SELECT nama_bahan, stok, satuan FROM bahan WHERE stok <= stok_minimum ORDER BY nama_bahan");
        if (m.getRowCount() == 0) return;
        StringBuilder sb = new StringBuilder("Stok bahan berikut menipis:\n");
        for (int r = 0; r < m.getRowCount(); r++)
            sb.append("\n- ").append(m.getValueAt(r, 0)).append(": sisa ")
              .append(m.getValueAt(r, 1)).append(' ').append(m.getValueAt(r, 2));
        Ui.warn(parent, sb.toString());
    }
}
