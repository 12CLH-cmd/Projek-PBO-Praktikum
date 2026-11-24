package sofa;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginForm extends JFrame {
    private final JTextField tfUser = new JTextField(16);
    private final JPasswordField pfPass = new JPasswordField(16);

    public LoginForm() {
        setTitle("Login - Sofa Cipacing");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 28, 20, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Sofa Cipacing", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2; p.add(title, g);
        JLabel sub = new JLabel("Manajemen Produksi & Konsinyasi", SwingConstants.CENTER);
        g.gridy = 1; p.add(sub, g);

        g.gridwidth = 1;
        g.gridy = 2; g.gridx = 0; p.add(new JLabel("Username"), g);
        g.gridx = 1; p.add(tfUser, g);
        g.gridy = 3; g.gridx = 0; p.add(new JLabel("Password"), g);
        g.gridx = 1; p.add(pfPass, g);

        JButton btn = new JButton("Masuk");
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2; p.add(btn, g);
        btn.addActionListener(e -> masuk());
        getRootPane().setDefaultButton(btn);

        setContentPane(p);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void masuk() {
        String u = tfUser.getText().trim();
        String pw = new String(pfPass.getPassword());
        if (u.isEmpty() || pw.isEmpty()) { Ui.warn(this, "Username dan password wajib diisi."); return; }
        Object r = Db.scalar("SELECT COUNT(*) FROM users WHERE username=? AND password=SHA2(?,256)", u, pw);
        if (r == null) return; // error koneksi sudah ditampilkan
        if (((Number) r).intValue() > 0) {
            MainFrame f = new MainFrame(u);
            f.setVisible(true);
            dispose();
            BahanPanel.peringatan(f);
        } else {
            Ui.warn(this, "Username atau password salah.");
            pfPass.setText("");
        }
    }
}
