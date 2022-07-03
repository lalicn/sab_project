package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.GeneralOperations;

public class ln180648_MyGeneralOperations implements GeneralOperations {

    private Connection conn;

    ln180648_MyGeneralOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void eraseAll() {
        String admin = "delete from Administrator";
        String kurir = "delete from Kurir";
        String korisnik = "delete from Korisnik";
        String uVozilu = "delete from UVozilu";
        String vozi = "delete from Vozi";
        String vozilo = "delete from Vozilo";
        String magacin = "delete from Magacin";
        String paket = "delete from Paket";
        String adresa = "delete from Adresa";
        String grad = "delete from Grad";
        String kupac = "delete from Kupac";
        String planPreuzimanja = "delete from PlanPreuzimanja";
        String planIsporuke = "delete from PlanIsporuke";
        String ponuda = "delete from Ponuda";
        String voznja = "delete from Voznja";
        String isporuceno = "delete from Isporuceno";
        String zahtjev = "drop table if exists #Zahtjev";

        try (Statement s = conn.createStatement()) {
            s.executeUpdate(admin);
            s.executeUpdate(isporuceno);
            s.executeUpdate(vozi);
            s.executeUpdate(planPreuzimanja);
            s.executeUpdate(planIsporuke);
            s.executeUpdate(voznja);
            s.executeUpdate(kurir);
            s.executeUpdate(uVozilu);
            s.executeUpdate(ponuda);
            s.executeUpdate(paket);
            s.executeUpdate(korisnik);
            s.executeUpdate(vozilo);
            s.executeUpdate(magacin);
            s.executeUpdate(adresa);
            s.executeUpdate(grad);
            s.executeUpdate(kupac);
            s.execute(zahtjev);
            

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyGeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
