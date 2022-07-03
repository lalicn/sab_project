package rs.etf.sab.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rs.etf.sab.operations.CityOperations;

public class ln180648_MyCityOperations implements CityOperations {

    private Connection conn;
    
    ln180648_MyCityOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public int insertCity(String string, String string1) {
        String query = "select count(*) from Grad where PostanskiBroj = ?";
        String query2 = "insert into Grad(Naziv, PostanskiBroj) values(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, string1);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) {
                ps2.setString(1, string);
                ps2.setString(2, string1);
                ps2.executeUpdate();
                ResultSet rs2 = ps2.getGeneratedKeys();
                rs2.next();
                return rs2.getInt(1);
            } else {
                return -1;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    @Override
    public int deleteCity(String... strings) {
        String query = "select count(*) from Grad where Naziv = ?";
        String query2 = "delete from Grad where Naziv = ?";
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
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public boolean deleteCity(int i) {
        String query = "select count(*) from Grad where IdGrad = ?";
        String query2 = "delete from Grad where IdGrad = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 1) {
                ps2.setInt(1, i);
                ps2.executeUpdate();
                return true;
            } else {
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public List<Integer> getAllCities() {
        String query = "select IdGrad from Grad";
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

}
