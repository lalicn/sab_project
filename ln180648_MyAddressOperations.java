package rs.etf.sab.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.AddressOperations;

public class ln180648_MyAddressOperations implements AddressOperations {

    private Connection conn;
    
    ln180648_MyAddressOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public int insertAddress(String string, int i, int i1, int i2, int i3) {
        String query = "select count(*) from Grad where IdGrad = ?";
        String query2 = "select count(*) from Adresa where XKoordinata = ? and YKoordinata = ?";
        String query3 = "insert into Adresa(Ulica, Broj, IdGrad, XKoordinata, YKoordinata) values(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i1);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 1) {   //Postoji grad
                ps2.setInt(1, i2);
                ps2.setInt(2, i3);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int count2 = rs2.getInt(1);
                if (count2 == 0) {  //Ne postoji adresa u bazi sa istim koordinatama
                    ps3.setString(1, string);
                    ps3.setInt(2, i);
                    ps3.setInt(3, i1);
                    ps3.setInt(4, i2);
                    ps3.setInt(5, i3);
                    ps3.executeUpdate();
                    ResultSet rs3 = ps3.getGeneratedKeys();
                    rs3.next();
                    return rs3.getInt(1);
                } else {    //Postoji adresa u bazi sa istim koordinatama
                    return -1;
                }
            } else {    //Ne postoji grad
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

    }

    @Override
    public int deleteAddresses(String string, int i) {
        String query = "select count(*) from Adresa where Ulica = ? and Broj = ?";
        String query2 = "delete from Adresa where Ulica = ? and Broj = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ps.setInt(2, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {   //Postoji bar jedna adresa sa datom ulicom i brojem
                ps2.setString(1, string);
                ps2.setInt(2, i);
                return ps2.executeUpdate();
            } else {    //Ne postoji nijedna adresa sa datom ulicom i brojem
                return 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public boolean deleteAdress(int i) {
        String query = "select count(*) from Adresa where IdAdr = ?";
        String query2 = "delete from Adresa where IdAdr = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 1) {   //Postoji adresa 
                ps2.setInt(1, i);
                ps2.executeUpdate();
                return true;
            } else {    //Ne postoji adresa
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public int deleteAllAddressesFromCity(int i) {
        String query = "select count(*) from Grad where IdGrad = ?";
        String query2 = "delete from Adresa where IdGrad = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 1) { //Postoji grad
                ps2.setInt(1, i);
                return ps2.executeUpdate();
            } else {  //Ne postoji grad
                return 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public List<Integer> getAllAddresses() {
        String query = "select IdAdr from Adresa";
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            List<Integer> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(rs.getInt(1));
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public List<Integer> getAllAddressesFromCity(int i) {
        String query = "select count(*) from Grad where IdGrad = ?";
        String query2 = "select IdAdr from Adresa where IdGrad = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 1) { //Postoji grad
                List<Integer> lista = new ArrayList<>();
                ps2.setInt(1, i);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    lista.add(rs2.getInt(1));
                }
                return lista;
            } else {    //Ne postoji grad
                return null;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
