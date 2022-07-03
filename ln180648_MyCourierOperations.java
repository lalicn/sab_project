package rs.etf.sab.student;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CourierOperations;

public class ln180648_MyCourierOperations implements CourierOperations {

    private Connection conn;
    
    ln180648_MyCourierOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean insertCourier(String string, String string1) {
//        System.out.println("rs.etf.sab.student.ln180648_MyCourierOperations.insertCourier() " + string);
        String query = "select KorisnickoIme from Korisnik where KorisnickoIme = ?";
        String query2 = "select count(*) from Kurir where KorisnickoIme = ? or BrVozackeDozvole = ?";
        String query3 = "insert into Kurir(KorisnickoIme, BrVozackeDozvole) values(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji korisnik sa datim korisnickim imenom
                ps2.setString(1, string);
                ps2.setString(2, string1);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int count = rs2.getInt(1);
                if (count == 0) {   //Nije vec kurir i nema niko isti broj vozacke
                    ps3.setString(1, string);
                    ps3.setString(2, string1);
                    ps3.executeUpdate();
                    return true;
                } else {    //Vec je kurir ili neko ima isti broj vozacke
                    return false;
                }
            } else {  //Ne postoji korisnik sa datim korisnickim imenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean deleteCourier(String string) {
        String query = "select count(*) from Kurir where KorisnickoIme = ?";
        String query2 = "delete from Kurir where KorisnickoIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji kurir sa datim korisnickim imenom
                ps2.setString(1, string);
                ps2.executeUpdate();
                return true;
            } else {    //Ne postoji kurir sa datim korisnickim imenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public List<String> getCouriersWithStatus(int i) {
        String query = "select KorisnickoIme from Kurir where Status = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            List<String> listaKurira = new ArrayList<>();
            while (rs.next()) {
                listaKurira.add(rs.getString(1));
//                System.out.println("Kurir koji " + i + ": " + rs.getString(1));
            }
            return listaKurira;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<String> getAllCouriers() {
        String query = "select KorisnickoIme from Kurir order by Profit desc";
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            List<String> listaKurira = new ArrayList<>();
            while (rs.next()) {
                listaKurira.add(rs.getString(1));
            }
            return listaKurira;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public BigDecimal getAverageCourierProfit(int i) {
//        System.out.println("rs.etf.sab.student.ln180648_MyCourierOperations.getAverageCourierProfit()");
        String query = "select avg(Profit) from Kurir where BrojIsporucenihPaketa = ?";
        String query2 = "select avg(Profit) from Kurir";
        try (PreparedStatement ps = conn.prepareStatement(query);
                Statement s2 = conn.createStatement()) {
            if (i != -1) { //Prosjecan profit kurira sa odredjenim brojem isporucenih paketa
                ps.setInt(1, i);
                ResultSet rs = ps.executeQuery();
                rs.next();
//                System.out.println("Prosjecan profit " + rs.getBigDecimal(1));
                return rs.getBigDecimal(1);
            } else {    //Prosjecan profit svih kurira
                ResultSet rs2 = s2.executeQuery(query2);
                rs2.next();
                return rs2.getBigDecimal(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
