package sofa;

import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/** Laporan laba per sofa (per model) dan per toko. */
public class LaporanPanel extends JPanel implements Ui.Refreshable {
    private static final String HPP_SUB =
        "(SELECT k.id_produk, SUM(k.jumlah_per_unit*b.harga_satuan) AS hpp "
        + "FROM kebutuhan_bahan k JOIN bahan b ON b.id_bahan=k.id_bahan GROUP BY k.id_produk)";

    private final JTable tSofa = new JTable(), tToko = new JTable();

    public LaporanPanel() {
        setLayout(new GridLayout(2, 1, 8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane a = new JScrollPane(tSofa), b = new JScrollPane(tToko);
        a.setBorder(new TitledBorder("Laba kotor per sofa (harga jual - HPP)"));
        b.setBorder(new TitledBorder("Laba per toko (dari sofa terjual)"));
        add(a);
        add(b);
        refresh();
    }

    @Override public void refresh() {
        tSofa.setModel(Db.query(
            "SELECT p.nama_model AS Model, p.harga_jual AS `Harga jual`, COALESCE(h.hpp,0) AS HPP, "
            + "p.harga_jual-COALESCE(h.hpp,0) AS `Laba kotor/unit`, "
            + "ROUND((p.harga_jual-COALESCE(h.hpp,0))/NULLIF(p.harga_jual,0)*100,1) AS `Margin %` "
            + "FROM produk_sofa p LEFT JOIN " + HPP_SUB + " h ON h.id_produk=p.id_produk "
            + "ORDER BY p.nama_model"));

        DefaultTableModel src = Db.query(
            "SELECT t.id_toko, t.nama_toko, COALESCE(SUM(s.jumlah_terjual),0), "
            + "COALESCE(SUM(s.jumlah_terjual*p.harga_jual),0), COALESCE(SUM(s.jumlah_terjual*h.hpp),0) "
            + "FROM toko_meubel t LEFT JOIN penyetoran s ON s.id_toko=t.id_toko "
            + "LEFT JOIN produk_sofa p ON p.id_produk=s.id_produk "
            + "LEFT JOIN " + HPP_SUB + " h ON h.id_produk=p.id_produk "
            + "GROUP BY t.id_toko, t.nama_toko ORDER BY t.nama_toko");
        DefaultTableModel m = new DefaultTableModel(new Object[]{"Toko", "Sofa terjual", "Omzet",
                "Komisi %", "Komisi toko", "HPP sofa terjual", "Laba pengrajin"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int r = 0; r < src.getRowCount(); r++) {
            int qty = (int) Ui.num(src.getValueAt(r, 2));
            double omzet = Ui.num(src.getValueAt(r, 3)), hpp = Ui.num(src.getValueAt(r, 4));
            double pct = Komisi.persen(qty), komisi = omzet * pct / 100.0;
            m.addRow(new Object[]{src.getValueAt(r, 1), qty, Ui.rp(omzet), String.format("%.1f%%", pct),
                Ui.rp(komisi), Ui.rp(hpp), Ui.rp(omzet - komisi - hpp)});
        }
        tToko.setModel(m);
    }
}
