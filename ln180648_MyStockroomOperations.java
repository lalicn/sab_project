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
import rs.etf.sab.operations.StockroomOperations;

public class ln180648_MyStockroomOperations implements StockroomOperations {

    private Connection conn;
    
    ln180648_MyStockroomOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public int insertStockroom(int i) {
        String query = "select IdGrad from Adresa where IdAdr = ?";
        String query2 = "select IdAdr from Adresa where IdGrad = ?";
        String query3 = "select IdMag from Magacin where IdAdr = ?";
        String query4 = "insert into Magacin(IdAdr) values(?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji adresa
                int IdGrad = rs.getInt(1);
                ps2.setInt(1, IdGrad);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {    //Prolazak kroz adrese istog grada kao i zadata adresa
                    ps3.setInt(1, rs2.getInt(1));
                    ResultSet rs3 = ps3.executeQuery();
                    if (rs3.next()) { //Vec postoji magacin u gradu
                        return -1;
                    }
                }
                //Ne postoji magacin u gradu
                ps4.setInt(1, i);
                ps4.executeUpdate();
                ResultSet rs4 = ps4.getGeneratedKeys();
                rs4.next();
                return rs4.getInt(1);
            } else {    //Ne postoji adresa
                return -1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyStockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    @Override
    public boolean deleteStockroom(int i) {
        String query = "select IdMag from Magacin where IdMag = ?";
        String query2 = "delete from Magacin where IdMag = ? and Status = 0";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Magacin postoji
                int idMag = rs.getInt(1);
                ps2.setInt(1, idMag);
                ps2.executeUpdate();
                return true;
            } else {   //Magacin ne postoji
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyStockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public int deleteStockroomFromCity(int i) {
        String query = "select IdAdr from Adresa where IdGrad = ?";
        String query2 = "select IdMag from Magacin where IdAdr = ?";
        String query3 = "delete from Magacin where IdMag = ? and Status = 0";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setInt(1, i);
            ResultSet rs = ps.executeQuery();
            int brojac = 0;
            int idMag = 0;
            while (rs.next()) {  //Prolazak kroz sve adrese zadatog grada
                int idAdr = rs.getInt(1);
                ps2.setInt(1, idAdr);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) { //Postoji magacin u gradu
                    brojac++;
                    idMag = rs2.getInt(1);
                    break;
                }
            }
            if (brojac == 0) {    //Ne postoji magacin u gradu
                return -1;
            } else {    //Postoji magacin u gradu
                ps3.setInt(1, idMag);
                ps3.executeUpdate();
                return idMag;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyStockroomOperations.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    @Override
    public List<Integer> getAllStockrooms() {
        String query = "select IdMag from Magacin";
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
