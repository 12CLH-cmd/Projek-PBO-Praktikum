package sofa;

/** Komisi bertingkat: persen ditentukan dari total sofa terjual sebuah toko. */
public class Komisi {
    public static double persen(int totalTerjual) {
        return Ui.num(Db.scalar(
            "SELECT persen FROM komisi_tier WHERE min_terjual <= ? ORDER BY min_terjual DESC LIMIT 1",
            totalTerjual));
    }

    public static int totalTerjual(int idToko) {
        return (int) Ui.num(Db.scalar(
            "SELECT COALESCE(SUM(jumlah_terjual),0) FROM penyetoran WHERE id_toko=?", idToko));
    }
}
