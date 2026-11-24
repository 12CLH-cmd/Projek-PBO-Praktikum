package sofa;

import java.awt.Component;
import javax.swing.JTabbedPane;

/** Tab Master Data: toko meubel, produk sofa (+ kebutuhan bahan), tingkat komisi. */
public class MasterPanel extends JTabbedPane implements Ui.Refreshable {
    public MasterPanel() {
        addTab("Toko Meubel", new CrudPanel("toko_meubel", "id_toko",
            new String[]{"nama_toko", "alamat", "kontak"},
            new String[]{"Nama toko", "Alamat", "Kontak"}));
        addTab("Produk Sofa", new ProdukPanel());
        addTab("Tingkat Komisi", new CrudPanel("komisi_tier", "id_tier",
            new String[]{"min_terjual", "persen", "label"},
            new String[]{"Min. total terjual (sofa)", "Komisi (%)", "Label"}));
        addChangeListener(e -> refresh());
    }

    @Override public void refresh() {
        Component c = getSelectedComponent();
        if (c instanceof Ui.Refreshable) ((Ui.Refreshable) c).refresh();
    }
}
