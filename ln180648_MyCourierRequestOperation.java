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
import rs.etf.sab.operations.CourierRequestOperation;

public class ln180648_MyCourierRequestOperation implements CourierRequestOperation {

    private Connection conn;

    ln180648_MyCourierRequestOperation(Connection conn) {
        this.conn = conn;
    }

    public void ispisiZahtjeve() {

    }

    public void ispisiKorisnike() {
        String pom = "select KorisnickoIme from Korisnik";
        try (PreparedStatement pomocna = conn.prepareStatement(pom)) {
            ResultSet pomocars = pomocna.executeQuery();
            while (pomocars.next()) {
//                System.out.println(pomocars.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierRequestOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean insertCourierRequest(String string, String string1) {
        String query = "select KorisnickoIme from Korisnik where KorisnickoIme = ?";
        String query2 = "if object_id('tempdb..#Zahtjev','U') is null create table #Zahtjev(KorisnickoIme varchar(100) primary key, BrojVozacke varchar(100) unique)";
        String query3 = "select KorisnickoIme from Kurir where KorisnickoIme = ?";
        String query4 = "select KorisnickoIme from #Zahtjev where KorisnickoIme = ?";
        String query5 = "select KorisnickoIme from #Zahtjev where BrojVozacke = ?";
        String query6 = "insert into #Zahtjev(KorisnickoIme, BrojVozacke) values(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                Statement s2 = conn.createStatement();
                PreparedStatement ps3 = conn.prepareStatement(query3);
                PreparedStatement ps4 = conn.prepareStatement(query4);
                PreparedStatement ps5 = conn.prepareStatement(query5);
                PreparedStatement ps6 = conn.prepareStatement(query6)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji korisnik sa datim korImenom
                s2.executeUpdate(query2);
                ps3.setString(1, string);
                ResultSet rs3 = ps3.executeQuery();
                if (!rs3.next()) {    //Trenutno nije kurir
                    ps4.setString(1, string);
                    ResultSet rs4 = ps4.executeQuery();
                    if (!rs4.next()) {    //Ne postoji zahtjev od korisnika sa istim korImenom
                        ps5.setString(1, string1);
                        ResultSet rs5 = ps5.executeQuery();
                        if (!rs5.next()) {    //Ne postoji zahtjev od drugog korisnika sa istim brojem dozvole
                            ps6.setString(1, string);
                            ps6.setString(2, string1);
                            ps6.executeUpdate();
                            return true;
                        } else {  //Postoji zahtjev od drugog korisnika sa istim brojem dozvole
                            return false;
                        }
                    } else {  //Postoji zahtjev od korisnika sa istim korImenom
                        return false;
                    }
                } else {  //Vec je kurir
                    return false;
                }
            } else {  //Ne postoji korisnik sa datim korImenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierRequestOperation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean deleteCourierRequest(String string) {
        String query = "select KorisnickoIme from #Zahtjev where KorisnickoIme = ?";
        String query2 = "delete from #Zahtjev where KorisnickoIme = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji zahtjev od korisnika sa datim korImenom
                ps2.setString(1, string);
                ps2.executeUpdate();
                return true;
            } else {  //Ne postoji zahtjev od korisnika sa datim korImenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierRequestOperation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean changeDriverLicenceNumberInCourierRequest(String string, String string1) {
        String query = "select KorisnickoIme from #Zahtjev where KorisnickoIme = ?";
        String query2 = "select BrojVozacke from #Zahtjev where  KorisnickoIme != ?";
        String query3 = "select BrVozackeDozvole from Kurir";
        String query4 = "update #Zahtjev set BrojVozacke = ?";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                Statement s3 = conn.createStatement();
                PreparedStatement ps4 = conn.prepareStatement(query4)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {  //Postoji zahtjev od korisnika sa datim korImenom
                ps2.setString(1, string);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    if (string1.equals(rs2.getString(1))) {   //Postoji zahtjev od korisnika sa istim brojem vozacke
                        return false;
                    }
                }
                ResultSet rs3 = s3.executeQuery(query3);
                while (rs3.next()) {
                    if (string1.equals(rs3.getString(1))) {   //Postoji kurir sa istim brojem vozacke
                        return false;
                    }
                }
                ps4.setString(1, string1);
                ps4.executeUpdate();
                return true;
            } else {  //Ne postoji zahtjev od korisnika sa datim korImenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierRequestOperation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public List<String> getAllCourierRequests() {
        List<String> listaZahtjeva = new ArrayList<>();
        String query = "if object_id('tempdb..#Zahtjev','U') is not null select KorisnickoIme from #Zahtjev";
        try (Statement s = conn.createStatement()) {
            if (s.execute(query)) {
                ResultSet rs = s.executeQuery(query);
                while (rs.next()) {
                    listaZahtjeva.add(rs.getString(1));
                }
            }
            return listaZahtjeva;

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierRequestOperation.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public boolean grantRequest(String string) {
        String query = "if object_id('tempdb..#Zahtjev','U') is not null select BrojVozacke from #Zahtjev where KorisnickoIme = ?";
        String query2 = "delete from #Zahtjev where KorisnickoIme = ?";
        String query3 = "insert into Kurir(KorisnickoIme, BrVozackeDozvole) values(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setString(1, string);
            if (ps.execute()) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {  ////Postoji zahtjev od korisnika sa datim korisnickim imenom
                    String brVozacke = rs.getString(1);
                    ps2.setString(1, string);
                    ps2.executeUpdate();
                    ps3.setString(1, string);
                    ps3.setString(2, brVozacke);
                    ps3.executeUpdate();
                    return true;
                } else {  //Ne postoji zahtjev od korisnika sa datim korisnickim imenom
                    return false;
                }
            } else {
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyCourierRequestOperation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
