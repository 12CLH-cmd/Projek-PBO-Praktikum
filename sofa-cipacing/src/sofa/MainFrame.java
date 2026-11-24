package sofa;

import java.awt.Component;
import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame(String user) {
        setTitle("Sistem Manajemen Produksi & Konsinyasi Sofa Cipacing  |  " + user);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Master Data", new MasterPanel());
        tabs.addTab("Penyetoran", new SetoranPanel());
        tabs.addTab("Pesanan", new PesananPanel());
        tabs.addTab("Bahan", new BahanPanel());
        tabs.addTab("Laporan", new LaporanPanel());
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c instanceof Ui.Refreshable) ((Ui.Refreshable) c).refresh();
        });
        add(tabs);

        JMenuBar bar = new JMenuBar();
        JMenu akun = new JMenu("Akun");
        JMenuItem keluar = new JMenuItem("Keluar");
        keluar.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
        akun.add(keluar);
        bar.add(akun);
        setJMenuBar(bar);
    }
}
