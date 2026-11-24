package sofa;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel CRUD generik: satu tabel, kolom pertama (pk) auto-increment,
 * kolom lain berupa isian teks. Kolom pertama di {cols} wajib diisi.
 */
public class CrudPanel extends JPanel implements Ui.Refreshable {
    protected final String table, pk;
    protected final String[] cols, labels;
    protected final JTextField[] f;
    public final JTable tbl = new JTable();
    public final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private Runnable onSelect = null;
    private int selectedId = -1;

    public CrudPanel(String table, String pk, String[] cols, String[] labels) {
        this.table = table; this.pk = pk; this.cols = cols; this.labels = labels;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        f = new JTextField[cols.length];
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < cols.length; i++) {
            g.gridx = (i % 2) * 2; g.gridy = i / 2;
            form.add(new JLabel(labels[i]), g);
            f[i] = new JTextField(22);
            g.gridx++;
            form.add(f[i], g);
        }

        JButton add = new JButton("Tambah"), upd = new JButton("Ubah"),
                del = new JButton("Hapus"), clr = new JButton("Bersihkan");
        buttons.add(add); buttons.add(upd); buttons.add(del); buttons.add(clr);
        add.addActionListener(e -> tambah());
        upd.addActionListener(e -> ubah());
        del.addActionListener(e -> hapus());
        clr.addActionListener(e -> { tbl.clearSelection(); bersih(); });

        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.NORTH);
        north.add(buttons, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) pick(); });
        refresh();
    }

    public void setOnSelect(Runnable r) { onSelect = r; }
    public int selectedId() { return selectedId; }
    public String value(int i) { return f[i].getText().trim(); }

    @Override public void refresh() {
        DefaultTableModel m = Db.query("SELECT " + pk + "," + String.join(",", cols)
                + " FROM " + table + " ORDER BY " + pk);
        String[] head = new String[labels.length + 1];
        head[0] = "ID";
        System.arraycopy(labels, 0, head, 1, labels.length);
        m.setColumnIdentifiers(head);
        tbl.setModel(m);
        selectedId = -1;
        bersih();
        if (onSelect != null) onSelect.run();
    }

    private void pick() {
        int r = tbl.getSelectedRow();
        if (r < 0) { selectedId = -1; if (onSelect != null) onSelect.run(); return; }
        selectedId = ((Number) tbl.getValueAt(r, 0)).intValue();
        for (int i = 0; i < f.length; i++) {
            Object v = tbl.getValueAt(r, i + 1);
            f[i].setText(v == null ? "" : v.toString());
        }
        if (onSelect != null) onSelect.run();
    }

    private void bersih() { for (JTextField t : f) t.setText(""); }

    private Object[] values() {
        Object[] v = new Object[f.length];
        for (int i = 0; i < f.length; i++) {
            String s = f[i].getText().trim();
            v[i] = s.isEmpty() ? null : s;
        }
        return v;
    }

    private boolean valid() {
        if (f[0].getText().trim().isEmpty()) {
            Ui.warn(this, labels[0] + " wajib diisi.");
            return false;
        }
        return true;
    }

    private void tambah() {
        if (!valid()) return;
        StringBuilder q = new StringBuilder();
        for (int i = 0; i < cols.length; i++) q.append(i == 0 ? "?" : ",?");
        if (Db.exec("INSERT INTO " + table + "(" + String.join(",", cols) + ") VALUES(" + q + ")", values()))
            refresh();
    }

    private void ubah() {
        if (selectedId < 0) { Ui.warn(this, "Pilih baris di tabel dulu."); return; }
        if (!valid()) return;
        Object[] v = values();
        Object[] p = new Object[v.length + 1];
        System.arraycopy(v, 0, p, 0, v.length);
        p[v.length] = selectedId;
        if (Db.exec("UPDATE " + table + " SET " + String.join("=?,", cols) + "=? WHERE " + pk + "=?", p))
            refresh();
    }

    private void hapus() {
        if (selectedId < 0) { Ui.warn(this, "Pilih baris di tabel dulu."); return; }
        if (JOptionPane.showConfirmDialog(this, "Hapus data ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (Db.exec("DELETE FROM " + table + " WHERE " + pk + "=?", selectedId)) refresh();
    }
}
