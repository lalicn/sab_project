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
import rs.etf.sab.operations.VehicleOperations;

public class ln180648_MyVehicleOperations implements VehicleOperations {

    private Connection conn;
    
    ln180648_MyVehicleOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean insertVehicle(String string, int i, BigDecimal bd, BigDecimal bd1) {
        String query = "select count(*) from Vozilo where RegistracioniBroj = ?";
        String query2 = "insert into Vozilo(RegistracioniBroj, TipGoriva, Potrosnja, Nosivost) values(?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {   //Ne postoji vozilo sa tim regBrojem
                ps2.setString(1, string);
                ps2.setInt(2, i);
                ps2.setBigDecimal(3, bd);
                ps2.setBigDecimal(4, bd1);
                ps2.executeUpdate();
                return true;
            } else {    //Postoji vozilo sa tim regBrojem
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public int deleteVehicles(String... strings) {
        String query = "select count(*) from Vozilo where RegistracioniBroj = ?";
        String query2 = "delete from Vozilo where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            for (int i = 0; i < strings.length; i++) {
                ps.setString(1, strings[i]);
                ResultSet rs = ps.executeQuery();
                rs.next();
                int count = rs.getInt(1);
                if (count == 0) {
                    return 0;
                }
            }
            int brojac = 0;
            for (int i = 0; i < strings.length; i++) {
                ps2.setString(1, strings[i]);
                brojac += ps2.executeUpdate();
            }
            return brojac;

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public List<String> getAllVehichles() {
        String query = "select RegistracioniBroj from Vozilo";
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            List<String> listaVozila = new ArrayList<>();
            while (rs.next()) {
                listaVozila.add(rs.getString(1));
            }
            return listaVozila;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public boolean changeFuelType(String string, int i) {
        String query = "select RegistracioniBroj from Vozilo where RegistracioniBroj = ? and IdMag is not null";
        String query2 = "update Vozilo set TipGoriva = ? where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji vozilo i u magacinu je
                ps2.setInt(1, i);
                ps2.setString(2, string);
                ps2.executeUpdate();
                return true;
            } else {    //Ne postoji vozilo ili nije u magacinu
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean changeConsumption(String string, BigDecimal bd) {
        String query = "select RegistracioniBroj from Vozilo where RegistracioniBroj = ? and IdMag is not null";
        String query2 = "update Vozilo set Potrosnja = ? where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji vozilo i u magacinu je
                ps2.setBigDecimal(1, bd);
                ps2.setString(2, string);
                ps2.executeUpdate();
                return true;
            } else {    //Ne postoji vozilo ili nije u magacinu
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean changeCapacity(String string, BigDecimal bd) {
        String query = "select RegistracioniBroj from Vozilo where RegistracioniBroj = ? and IdMag is not null";
        String query2 = "update Vozilo set Nosivost = ? where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji vozilo i u magacinu je
                ps2.setBigDecimal(1, bd);
                ps2.setString(2, string);
                ps2.executeUpdate();
                return true;
            } else {    //Ne postoji vozilo ili nije u magacinu
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean parkVehicle(String string, int i) {;
        String query = "select RegistracioniBroj from Vozilo where RegistracioniBroj = ?";
        String query2 = "select IdMag from Magacin where IdMag = ?";
        String query3 = "select RegistracioniBroj from Vozi where RegistracioniBroj = ?";
        String query4 = "update Vozilo set IdMag = ? where RegistracioniBroj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji vozilo sa datim regBrojem
                ps2.setInt(1, i);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) { //Postoji magacin sa datim id
                    ps3.setString(1, string);
                    ResultSet rs3 = ps3.executeQuery();
                    if (!rs3.next()) {   //Voznja koja koristi vozilo nije u toku ili je vozilo bez voznje
                        ps4.setInt(1, i);
                        ps4.setString(2, string);
                        ps4.executeUpdate();    
                        return true;
                    } else {    //Voznja koja koristi vozilo je u toku
                        return false;
                    }
                } else {  //Ne postoji magacin sa datim id
                    return false;
                }
            } else {  //Ne postoji vozilo sa datim regBrojem
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyVehicleOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
