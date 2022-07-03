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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rs.etf.sab.operations.UserOperations;

public class ln180648_MyUserOperations implements UserOperations {

    private Connection conn;

    ln180648_MyUserOperations(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean insertUser(String string, String string1, String string2, String string3, int i) {
        String regex = "[A-Z].*";
        Pattern p = Pattern.compile(regex);
        Matcher m1 = p.matcher(string1);
        Matcher m2 = p.matcher(string2);
        if (!m1.matches() | !m2.matches()) {
            return false;
        }
        String regex2 = "(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*(\\W|_)).*";
        Pattern p2 = Pattern.compile(regex2);
        Matcher m3 = p2.matcher(string3);
        if (!m3.matches() || string3.length() < 8) {
            return false;
        }
        String query = "select count(*) from Korisnik where KorisnickoIme = ?";
        String query2 = "select count(*) from Adresa where IdAdr = ?";
        String query3 = "insert into Korisnik(KorisnickoIme, Ime, Prezime, Sifra, IdAdr) values(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if (count == 0) { //Korisnicko ime nije zauzeto
                ps2.setInt(1, i);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int count2 = rs2.getInt(1);
                if (count2 == 1) {  //Postoji adresa
                    ps3.setString(1, string);
                    ps3.setString(2, string1);
                    ps3.setString(3, string2);
                    ps3.setString(4, string3);
                    ps3.setInt(5, i);
                    ps3.executeUpdate();
                    return true;
                } else {    //Ne postoji adresa
                    return false;
                }

            } else {    //Korisnicko ime je zauzeto
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyUserOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public boolean declareAdmin(String string) {
        String query = "select KorisnickoIme from Korisnik where KorisnickoIme = ?";
        String query2 = "select count(*) from Administrator where KorisnickoIme = ?";
        String query3 = "insert into Administrator(KorisnickoIme) values(?)";
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2);
                PreparedStatement ps3 = conn.prepareStatement(query3)) {
            ps.setString(1, string);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    //Postoji korisnik sa zadatim korisnickim imenom
                ps2.setString(1, string);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                int count2 = rs2.getInt(1);
                if (count2 == 0) {    //Trenutno nije admin
                    ps3.setString(1, string);
                    ps3.executeUpdate();
                    return true;
                } else {    //Vec je admin
                    return false;
                }
            } else {  //Ne postoji korisnik sa zadatim korisnickim imenom
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyUserOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public int getSentPackages(String... strings) { //OVO POPRAVITI !!!!
        String query = "select KorisnickoIme from Korisnik where KorisnickoIme = ?";
        String query2 = "select BrPoslatihPosiljki from Korisnik where KorisnickoIme = ?";
//        System.out.println("rs.etf.sab.student.ln180648_MyUserOperations.getSentPackages()");
        try (PreparedStatement ps = conn.prepareStatement(query);
                PreparedStatement ps2 = conn.prepareStatement(query2)) {
            for (int i = 0; i < strings.length; i++) {
                ps.setString(1, strings[i]);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return -1;
                }

            }
            int brojac = 0;
            for (int i = 0; i < strings.length; i++) {
                ps2.setString(1, strings[i]);
                ResultSet rs2 = ps2.executeQuery();
                rs2.next();
                brojac += rs2.getInt(1);
            }
            return brojac;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyUserOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

    }

    @Override
    public int deleteUsers(String... strings) {
        String query = "select count(*) from Korisnik where KorisnickoIme = ?";
        String query2 = "delete from Korisnik where KorisnickoIme = ?";
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
            Logger.getLogger(ln180648_MyUserOperations.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    @Override
    public List<String> getAllUsers() {
        String query = "select KorisnickoIme from Korisnik";
        try (Statement s = conn.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            List<String> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
            return lista;
        } catch (SQLException ex) {
            Logger.getLogger(ln180648_MyAddressOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
