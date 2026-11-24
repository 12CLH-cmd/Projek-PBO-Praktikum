package sofa;

import java.awt.Component;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Helper kecil untuk tampilan. */
public class Ui {
    public interface Refreshable { void refresh(); }

    /** Item combo box: id + teks. */
    public static class Item {
        public final int id;
        public final String label;
        public Item(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    /** Isi combo dari query (kolom 1 = id, kolom 2 = teks). */
    public static void fill(JComboBox<Item> cb, String sql) {
        cb.removeAllItems();
        DefaultTableModel m = Db.query(sql);
        for (int r = 0; r < m.getRowCount(); r++)
            cb.addItem(new Item(((Number) m.getValueAt(r, 0)).intValue(), String.valueOf(m.getValueAt(r, 1))));
    }

    public static int id(JComboBox<Item> cb) {
        Item i = (Item) cb.getSelectedItem();
        return i == null ? -1 : i.id;
    }

    public static void select(JComboBox<Item> cb, int id) {
        for (int i = 0; i < cb.getItemCount(); i++)
            if (cb.getItemAt(i).id == id) { cb.setSelectedIndex(i); return; }
    }

    public static double num(Object o) { return o instanceof Number ? ((Number) o).doubleValue() : 0; }

    /** Parse angka dari teks (kosong = 0). Bisa melempar NumberFormatException. */
    public static double parse(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return Double.parseDouble(s.trim());
    }

    public static String rp(double v) {
        return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(v);
    }

    public static void info(Component p, String m) { JOptionPane.showMessageDialog(p, m); }
    public static void warn(Component p, String m) {
        JOptionPane.showMessageDialog(p, m, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }
}
