package sofa;

import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/** Koneksi JDBC + helper query. Ubah URL/USER/PASS sesuai Laragon kamu. */
public class Db {
    private static final String URL = "jdbc:mysql://localhost:3306/db_sofa_cipacing"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static void bind(PreparedStatement ps, Object[] p) throws SQLException {
        for (int i = 0; i < p.length; i++) ps.setObject(i + 1, p[i]);
    }

    /** SELECT -> model tabel (tidak bisa diedit). */
    public static DefaultTableModel query(String sql, Object... p) {
        DefaultTableModel m = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                for (int i = 1; i <= n; i++) m.addColumn(md.getColumnLabel(i));
                while (rs.next()) {
                    Object[] row = new Object[n];
                    for (int i = 0; i < n; i++) row[i] = rs.getObject(i + 1);
                    m.addRow(row);
                }
            }
        } catch (SQLException e) {
            error(e);
        }
        return m;
    }

    /** INSERT/UPDATE/DELETE. true jika berhasil. */
    public static boolean exec(String sql, Object... p) {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            error(e);
            return false;
        }
    }

    /** Nilai kolom pertama baris pertama (null jika kosong/gagal). */
    public static Object scalar(String sql, Object... p) {
        try (Connection c = get(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getObject(1) : null;
            }
        } catch (SQLException e) {
            error(e);
            return null;
        }
    }

    public static void error(Exception e) {
        JOptionPane.showMessageDialog(null, "Kesalahan database:\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
